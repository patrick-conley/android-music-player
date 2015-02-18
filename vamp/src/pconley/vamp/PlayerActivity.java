package pconley.vamp;

import pconley.vamp.model.Track;
import pconley.vamp.player.PlayerService;
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

	/*
	 * Progress/seek bar. Updates automatically as long as a track is playing,
	 * and a user isn't touching it.
	 */
	private SeekBar progress;
	private CountDownTimer progressTimer;

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

		// Set up a connection to the music player
		playerConnection = new PlayerServiceConnection();

		// Create a receiver to listen for status events from the player.
		playerReceiver = new PlayerEventReceiver();

		// Initialize the progress bar
		progress = (SeekBar) findViewById(R.id.playback_progress);
		progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.player, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Register receivers for broadcasts from the player service and bind to its
	 * running instance.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		bindService(new Intent(this, PlayerService.class), playerConnection,
				Context.BIND_AUTO_CREATE);

		IntentFilter filter = new IntentFilter(
				PlayerService.FILTER_PLAYER_EVENT);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				playerReceiver, filter);

	}

	/**
	 * Unregister and unbind from the player service.
	 */
	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				playerReceiver);

		cancelCountdown();
		unbindService(playerConnection);

		super.onPause();
	}

	/**
	 * Start/resume playback if in the "prepared" or "paused" states; pause
	 * playback if in the "started" state. Does nothing if in any other state.
	 *
	 * @param view
	 */
	public void onPlayPauseClick(View view) {
		if (player.isPlaying()) {
			player.pause();
		} else {
			player.play();
		}
	}

	/*
	 * Start the progress bar countdown. Call this method after a seek operation
	 * to restart counting at the correct place.
	 */
	// Start (or reset) the countdown on the progress bar
	private void startCountdown() {
		if (progressTimer != null) {
			progressTimer.cancel();
		}

		final int position = player.getCurrentPosition();
		final int duration = player.getDuration();

		progress.setIndeterminate(duration == -1);
		progress.setMax(duration / SEC);

		Log.i("Active track", String.format("Starting timer at %d of %d",
				position / SEC, duration / SEC));

		progressTimer = new CountDownTimer(duration - position, SEC) {

			@Override
			public void onTick(long remaining) {
				if (canTimerCountDown) {
					progress.setProgress((duration - (int) remaining) / SEC);
					Log.v("Active track", String.format("Progress is %d of %d",
							(duration - (int) remaining) / SEC, duration / SEC));
				}
			}

			@Override
			public void onFinish() {
				progress.setProgress(0);
				progress.setMax(0);
			}
		}.start();
	}

	/*
	 * Stop the progress bar countdown.
	 */
	private void cancelCountdown() {
		if (progressTimer != null) {
			progressTimer.cancel();
			progressTimer = null;
		}

		Log.i("Active track", "Stopping timer");

		int position = player.getCurrentPosition();
		int duration = player.getDuration();

		progress.setIndeterminate(false);
		progress.setProgress(position / SEC);
		progress.setMax(duration / SEC);
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
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.PlayerBinder) service).getService();

			if (player.isPlaying()) {
				startCountdown();
				displayTrackDetails();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			cancelCountdown();
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

			if (!intent.hasExtra(PlayerService.EXTRA_EVENT)) {
				Log.e("Active track",
						"Broadcast from Player missing required event.");
				finish();
			}

			Log.i("Active track",
					"Received player event "
							+ intent.getStringExtra(PlayerService.EXTRA_EVENT));

			switch (intent.getStringExtra(PlayerService.EXTRA_EVENT)) {
			case PlayerService.EVENT_NEW_TRACK:
				displayTrackDetails();

				break;
			case PlayerService.EVENT_PAUSE:
				cancelCountdown();

				if (intent.hasExtra(PlayerService.EXTRA_MESSAGE)) {
					Toast.makeText(PlayerActivity.this,
							intent.getStringExtra(PlayerService.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();
				}

				break;

			case PlayerService.EVENT_PLAY:
				startCountdown();

				break;

			case PlayerService.EVENT_STOP:
				cancelCountdown();

				if (intent.hasExtra(PlayerService.EXTRA_MESSAGE)) {
					Toast.makeText(PlayerActivity.this,
							intent.getStringExtra(PlayerService.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();
				}

				((TextView) findViewById(R.id.view_uri)).setText("");
				((TextView) findViewById(R.id.view_tags)).setText("");

				break;

			}

		}
	}

}
