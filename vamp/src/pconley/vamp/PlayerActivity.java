package pconley.vamp;

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

/*
 * FIXME: player must broadcast pause events
 */

public class PlayerActivity extends Activity {

	private static final int SEC = 1000;

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

		playerReceiver = new PlayerService.StatusReceiver(this);

		// initialize the progress bar
		progress = (SeekBar) findViewById(R.id.playback_progress);
		progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					player.seekTo(progress * SEC);
					if (player.isPlaying()) {
						startCountdown();
					}
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
		});

		// Define a connection to the music player
		playerConnection = new ServiceConnection() {

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
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.player, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
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

		LocalBroadcastManager.getInstance(this).registerReceiver(
				playerReceiver,
				new IntentFilter(PlayerService.FILTER_PLAYER_STATUS));

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
	public void playPause(View view) {
		if (player.playPause()) {
			startCountdown();
		} else {
			cancelCountdown();
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

}
