package pconley.vamp.player;

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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import pconley.vamp.R;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.util.BroadcastConstants;

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
	private TextView uriView;
	private TextView tagsView;
	private ImageButton playPauseButton;

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
		progressBar = (SeekBar) findViewById(R.id.player_seek);
		progressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener());

		positionView = (TextView) findViewById(R.id.player_view_position);
		durationView = (TextView) findViewById(R.id.player_view_duration);
		uriView = (TextView) findViewById(R.id.player_view_uri);
		tagsView = (TextView) findViewById(R.id.player_view_tags);
		playPauseButton = (ImageButton) findViewById(
				R.id.player_button_playpause);
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
	 * <p/>
	 * The player's broadcast receiver would normally be registered from here,
	 * but to guarantee synchrony it must be registered from
	 * PlayerServiceConnection.onServiceConnected.
	 */
	@Override
	protected void onStart() {
		super.onStart();

		bindService(new Intent(this, PlayerService.class), playerConnection,
		            Context.BIND_AUTO_CREATE);
	}

	/**
	 * Unregister and unbind from the player service.
	 */
	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				playerReceiver);

		// Player may be null if the activity is paused before the service has
		// been bound. This seems to happen when restarting after a crash.
		if (player != null) {
			drawPauseState();
		}

		unbindService(playerConnection);

		new LibraryOpenHelper(this).close();

		super.onStop();
	}

	/**
	 * Callback for the play/pause button.
	 * <p/>
	 * Start/resume playback if in the "prepared" or "paused" states; pause
	 * playback if in the "started" state. Does nothing if in any other state.
	 *
	 * @param view
	 */
	public void onClickPlayPause(View view) {

		// Player may be null if an attentive user presses play/pause before
		// the service is bound.
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
	 * <p/>
	 * Go to the beginning of the previous track, if possible. Does nothing if
	 * the player is not prepared.
	 *
	 * @param view
	 */
	public void onClickPrev(View view) {
		if (player != null) {
			player.previous();
		}
	}

	/**
	 * Callback for the Next Track button.
	 * <p/>
	 * Go to the beginning of the next track, if possible. Does nothing if the
	 * player is not prepared.
	 *
	 * @param view
	 */
	public void onClickNext(View view) {
		if (player != null) {
			player.next();
		}
	}

	/*
	 * Display the tags for the current track.
	 */
	private void drawTrackDetails() {
		Track track = player.getCurrentTrack();

		uriView.setText(track.getUri().toString());
		tagsView.setText(track.tagsToString());

		drawTime();
	}

	/*
	 * Update values for progress & duration timers, and position in the
	 * progress bar.
	 *
	 * If no track is playing, times are set to 0:00 and the progress bar is
	 * locked to the beginning.
	 */
	private void drawTime() {

		final int position = player.getPosition();
		final int duration = player.getDuration();

		if (position != -1) {
			progressBar.setProgress(position / SEC);
			progressBar.setMax(duration / SEC);

			positionView.setText(dateFormat.format(new Date(position)));
			durationView.setText(dateFormat.format(new Date(duration)));
		} else {
			progressBar.setProgress(0);
			progressBar.setMax(0);

			positionView.setText(R.string.player_blank_time);
			durationView.setText(R.string.player_blank_time);
		}

	}

	/*
	 * Start the progress bar countdown. Call this method after a seek
	 * operation to restart counting at the correct place.
	 */
	private void drawPlayState() {
		playPauseButton.setBackground(
				ContextCompat.getDrawable(PlayerActivity.this,
				                          android.R.drawable.ic_media_pause));

		if (progressTimer != null) {
			progressTimer.cancel();
		}

		drawTime();

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
					positionView.setText(
							dateFormat.format(new Date(position)));
					Log.v("Active track",
					      String.format("Progress is %d of %d",
					                    (duration - (int) remaining) / SEC,
					                    duration / SEC));
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
	private void drawPauseState() {
		drawPauseState(null);
	}

	private void drawPauseState(String message) {
		playPauseButton.setBackground(
				ContextCompat.getDrawable(this,
				                          android.R.drawable.ic_media_play));

		if (progressTimer != null) {
			progressTimer.cancel();
			progressTimer = null;
		}

		if (message != null) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}

		Log.i("Active track", "Stopping timer");
		drawTime();
	}

	private final class PlayerServiceConnection implements ServiceConnection {

		/**
		 * Get the running service's instance. Register the broadcast receiver,
		 * and update the UI.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.PlayerBinder) service).getService();

			if (player.getCurrentTrack() != null) {
				drawTrackDetails();
			}

			if (player.isPlaying()) {
				drawPlayState();
			}

			IntentFilter filter
					= new IntentFilter(BroadcastConstants.FILTER_PLAYER_EVENT);
			LocalBroadcastManager.getInstance(PlayerActivity.this)
			                     .registerReceiver(playerReceiver, filter);
		}

		/**
		 * Clean up if the Player has been lost unexpectedly.
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			drawPauseState();
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
				if (player.isPlaying()) {
					drawPlayState();
				} else {
					drawPauseState();
				}
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

			PlayerEvent event = (PlayerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT);

			Log.i("Active track", "Received player event " + event);

			switch (event) {
				case NEW_TRACK:
					drawTrackDetails();
					break;

				case PAUSE:
					drawPauseState(intent.getStringExtra(
							BroadcastConstants.EXTRA_MESSAGE));
					break;

				case PLAY:
					drawPlayState();
					break;

				case STOP:
					drawPauseState(intent.getStringExtra(
							BroadcastConstants.EXTRA_MESSAGE));

					uriView.setText("");
					tagsView.setText("");

					finish();
					break;
			}

		}
	}

}
