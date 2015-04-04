package pconley.vamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import pconley.vamp.model.Track;
import pconley.vamp.player.PlayerEvent;
import pconley.vamp.player.PlayerService;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.util.BroadcastConstants;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends Activity {

	private static final int SEC = 1000;

	SimpleDateFormat dateFormat;

	/*
	 * Progress/seek bar. Updates automatically as long as a track is playing,
	 * and a user isn't touching it.
	 */
	private SeekBar progressBar;
	private CountDownTimer progressTimer;

	private TextView positionView;
	private TextView durationView;

	// Countdown timer can advance the progress bar only if a user isn't
	// dragging it
	private boolean canTimerCountDown = true;

	/*
	 * Receive status (state changes, error messages) from the player.
	 */
	private BroadcastReceiver playerReceiver;

	/*
	 * Bound connection to the player to allow it to be controlled.
	 */
	private PlayerService player;
	private ServiceConnection playerConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Format for track duration/position
		dateFormat = new SimpleDateFormat("m:ss", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Set up a connection to the music player
		playerConnection = new PlayerServiceConnection();

		// Create a receiver to listen for status events from the player.
		playerReceiver = new PlayerEventReceiver();

		// Initialize the progress bar
		progressBar = (SeekBar) findViewById(R.id.progress_bar);
		progressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.player, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Bind to the running instance of the player.
	 *
	 * The player's broadcast receiver would normally be registered from here,
	 * but to guarantee synchrony it must be registered from
	 * PlayerServiceConnection.onServiceConnected.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		bindService(new Intent(this, PlayerService.class), playerConnection,
				Context.BIND_AUTO_CREATE);
	}

	/**
	 * Unregister and unbind from the player service.
	 */
	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				playerReceiver);

		// Player may be null if the activity is paused before the service has
		// been bound. This seems to happen when restarting after a crash.
		if (player != null) {
			stopCountdown();
		}

		unbindService(playerConnection);

		super.onPause();
	}

	/**
	 * Callback for the play/pause button.
	 * 
	 * Start/resume playback if in the "prepared" or "paused" states; pause
	 * playback if in the "started" state. Does nothing if in any other state.
	 *
	 * @param view
	 */
	public void onPlayPauseClick(View view) {

		// Player may be null if an attentive user presses play/pause before the
		// service is bound.
		if (player != null) {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
		}
	}

	/**
	 * Callback for the Previous Track button.
	 *
	 * Go to the beginning of the previous track, if possible. Does nothing if
	 * the player is not prepared.
	 *
	 * @param view
	 */
	public void onPrevClick(View view) {

		if (player != null) {
			player.previous();
		}
	}

	/**
	 * Callback for the Next Track button.
	 * 
	 * Go to the beginning of the next track, if possible. Does nothing if the
	 * player is not prepared.
	 *
	 * @param view
	 */
	public void onNextClick(View view) {

		if (player != null) {
			player.next();
		}
	}

	/*
	 * Update values for progress & duration timers, and position in the
	 * progress bar.
	 * 
	 * If no track is playing, times are set to 0:00 and the progress bar is
	 * locked to the beginning.
	 */
	private void displayPosition() {

		final int position = player.getPosition();
		final int duration = player.getDuration();

		positionView = (TextView) findViewById(R.id.position);
		durationView = (TextView) findViewById(R.id.duration);

		if (position != -1) {
			progressBar.setIndeterminate(duration == -1);
			progressBar.setProgress(position / SEC);
			progressBar.setMax(duration / SEC);

			positionView.setText(dateFormat.format(new Date(position)));
			durationView.setText(dateFormat.format(new Date(duration)));
		} else {
			progressBar.setIndeterminate(false);
			progressBar.setProgress(0);
			progressBar.setMax(0);

			positionView.setText(getString(R.string.blank_time));
			durationView.setText(getString(R.string.blank_time));
		}

	}

	/*
	 * Start the progress bar countdown. Call this method after a seek operation
	 * to restart counting at the correct place.
	 */
	private void startCountdown() {
		if (progressTimer != null) {
			progressTimer.cancel();
		}

		displayPosition();

		final int position = player.getPosition();
		final int duration = player.getDuration();

		Log.i("Active track", String.format("Starting timer at %d of %d",
				position / SEC, duration / SEC));

		progressTimer = new CountDownTimer(duration - position, SEC) {

			@Override
			public void onTick(long remaining) {
				if (canTimerCountDown) {
					int position = duration - (int) remaining;
					progressBar.setProgress(position / SEC);
					positionView.setText(dateFormat.format(new Date(position)));
					Log.v("Active track", String.format("Progress is %d of %d",
							(duration - (int) remaining) / SEC, duration / SEC));
				}
			}

			@Override
			public void onFinish() {
				progressBar.setProgress(0);
				progressBar.setMax(0);
			}
		}.start();
	}

	/*
	 * Stop the progress bar countdown.
	 */
	private void stopCountdown() {
		if (progressTimer != null) {
			progressTimer.cancel();
			progressTimer = null;
		}

		Log.i("Active track", "Stopping timer");

		displayPosition();
		progressBar.setIndeterminate(false);
	}

	private void clearCountdown() {
		if (progressTimer != null) {
			stopCountdown();
		}

		Log.i("Active track", "Clearing timer & times");
		displayPosition();
	}

	/*
	 * Display the tags for the current track.
	 */
	private void displayTrackDetails() {

		Track track = player.getCurrentTrack();

		((TextView) findViewById(R.id.view_uri)).setText(track.getUri()
				.toString());
		((TextView) findViewById(R.id.view_tags)).setText(track.tagsToString());

	}

	private final class PlayerServiceConnection implements ServiceConnection {

		/**
		 * Get the running service's instance. Register the broadcast receiver,
		 * and update the UI.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.PlayerBinder) service).getService();

			if (player.isPlaying()) {
				startCountdown();
				displayTrackDetails();
			}

			LocalBroadcastManager.getInstance(PlayerActivity.this)
					.registerReceiver(playerReceiver,
							new IntentFilter(BroadcastConstants.FILTER_PLAYER_EVENT));
		}

		/**
		 * Clean up if the Player has been lost unexpectedly.
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			stopCountdown();
			player = null;
		}
	}

	/*
	 * Seek within the track if the user moves the seek bar.
	 */
	private final class OnSeekBarChangeListener implements
			SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				player.seekTo(progress * SEC);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			canTimerCountDown = false;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			canTimerCountDown = true;
		}
	}

	/*
	 * Receiver for error messages from the player service
	 */
	private class PlayerEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.hasExtra(BroadcastConstants.EXTRA_EVENT)) {
				Log.e("Active track",
						"Broadcast from Player missing required event.");
				finish();
			}

			Log.i("Active track",
					"Received player event "
							+ (PlayerEvent) intent
									.getSerializableExtra(BroadcastConstants.EXTRA_EVENT));

			switch ((PlayerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT)) {
			case NEW_TRACK:
				displayTrackDetails();

				break;
			case PAUSE:
				stopCountdown();

				if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
					Toast.makeText(PlayerActivity.this,
							intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();
				}

				break;

			case PLAY:
				startCountdown();

				break;

			case STOP:
				clearCountdown();

				if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
					Toast.makeText(PlayerActivity.this,
							intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();
				}

				((TextView) findViewById(R.id.view_uri)).setText("");
				((TextView) findViewById(R.id.view_tags)).setText("");

				break;

			}

		}
	}

}
