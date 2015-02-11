package pconley.vamp;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;
import pconley.vamp.player.PlayerEvents;
import pconley.vamp.player.PlayerService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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

	public static final String EXTRA_ID = "pconley.vamp.CurrentTrackActivity.track_id";

	/*
	 * Progress/seek bar. Updates automatically as long as a track is playing,
	 * and a user isn't touching it.
	 */
	private SeekBar progress;
	private boolean allowProgressUpdates = true;

	/*
	 * Receive status messages from the player
	 */
	private BroadcastReceiver playerReceiver;

	/*
	 * Bound connection to the player to allow it to be controlled
	 */
	private PlayerService player;
	private ServiceConnection playerConnection;

	private CountDownTimer progressTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Create a receiver to listen for errors from the player service.
		playerReceiver = new PlayerEventReceiver();

		// Begin playing a track, and display its metadata.
		// FIXME: what happens if the player is destroyed?
		if (getIntent().hasExtra(EXTRA_ID)) {
			new LoadTrackTask().execute(getIntent().getLongExtra(EXTRA_ID, -1));
		}

		// Initialize the progress bar
		progress = (SeekBar) findViewById(R.id.playback_progress);
		progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener());

		// Set up a connection to the music player
		playerConnection = new PlayerServiceConnection();
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

		LocalBroadcastManager.getInstance(this).registerReceiver(
				playerReceiver,
				new IntentFilter(PlayerEvents.FILTER_PLAYER_EVENT));

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
				if (allowProgressUpdates) {
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

	// Abort the countdown on the progress bar
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

	private final class PlayerServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = ((PlayerService.PlayerBinder) service).getService();

			if (player.isPlaying()) {
				startCountdown();
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
			allowProgressUpdates = false;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			allowProgressUpdates = true;
		}
	}

	/*
	 * Receiver for error messages from the player service
	 */
	private class PlayerEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.hasExtra(PlayerEvents.EXTRA_STATE)) {
				Log.e("Active track", "Unspecified event received");
			} else if (intent.getBooleanExtra(PlayerEvents.EXTRA_STATE, false)) {
				startCountdown();
			} else {

				cancelCountdown();

				if (intent.hasExtra(PlayerEvents.EXTRA_MESSAGE)) {
					Toast.makeText(PlayerActivity.this,
							intent.getStringExtra(PlayerEvents.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	/*
	 * Load the given track from the database, and display it and its tags in
	 * this activity's text field.
	 *
	 * Work is done in a background thread.
	 */
	private class LoadTrackTask extends AsyncTask<Long, Void, Track> {

		@Override
		protected Track doInBackground(Long... params) {
			return new TrackDAO(PlayerActivity.this).getTrack(params[0]);
		}

		protected void onPostExecute(Track track) {
			Intent intent = new Intent(PlayerActivity.this, PlayerService.class)
					.setAction(PlayerService.ACTION_PLAY).setData(track.getUri());
			startService(intent);

			((TextView) findViewById(R.id.view_uri)).setText(track.getUri()
					.toString());
			((TextView) findViewById(R.id.view_tags)).setText(track
					.tagsToString());
		}

	}
}
