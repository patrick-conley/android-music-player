package pconley.vamp;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

public class PlayerActivity extends Activity {

	private static final String SAMPLE_NAME = "sample_1.ogg";

		private static final int SEC = 1000;

	/*
	 * Progress/seek bar. Updates automatically as long as a track is playing,
	 * and a user isn't touching it.
	 */
	private SeekBar progress;
	private boolean allowProgressUpdates = true;

	/*
	 * Receive progress updates from the player
	 */
	private BroadcastReceiver progressReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		progressReceiver = new ProgressBroadcastReceiver();

		getActionBar().setDisplayHomeAsUpEnabled(true);

		progress = (SeekBar) findViewById(R.id.playback_progress);
		progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					Intent intent = new Intent(PlayerActivity.this,
							PlayerService.class).setAction(
							PlayerService.ACTION_SEEK).putExtra(
							PlayerService.EXTRA_SEEK_POSITION, progress * SEC);

					startService(intent);
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

		File track = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
				SAMPLE_NAME);

		if (!track.exists()) {
			displayMissingTrackDialog(track);
			return;
		}

		Intent intent = new Intent(this, PlayerService.class).setAction(
				PlayerService.ACTION_PLAY).setData(Uri.fromFile(track));
		startService(intent);
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

	@Override
	protected void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				progressReceiver,
				new IntentFilter(PlayerService.FILTER_PROGRESS));
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				progressReceiver);

		super.onPause();
	}

	/**
	 * Start/resume playback if in the "prepared" or "paused" states; pause
	 * playback if in the "started" state. Does nothing if in any other state.
	 *
	 * @param view
	 */
	public void playPause(View view) {
		Intent intent = new Intent(this, PlayerService.class)
				.setAction(PlayerService.ACTION_PLAY_PAUSE);
		startService(intent);
	}

	/*
	 * Display an alert dialog (to go back to the library) if the sample track
	 * doesn't exist
	 */
	private void displayMissingTrackDialog(File track) {
		Log.e("Player", "Sample track doesn't exist");
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle(R.string.title_activity_player)
				.setMessage(
						"The toy music player expects to play the file "
								+ track.toString() + ", which doesn't exist.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})

				.create().show();
		return;
	}

	/*
	 * Receiver for broadcasts about current track progress. Updates the
	 * progress bar.
	 */
	private class ProgressBroadcastReceiver extends BroadcastReceiver {

		private CountDownTimer progressTimer;

		@Override
		public void onReceive(Context context, Intent intent) {

			final int position = intent.getIntExtra(
					PlayerService.EXTRA_PROGRESS_POSITION, 0);
			final int duration = intent.getIntExtra(
					PlayerService.EXTRA_PROGRESS_DURATION, 100 * SEC);

			switch (intent.getIntExtra(PlayerService.EXTRA_PROGRESS_STATE, 0)) {
			case PlayerService.PROGRESS_STATE_PLAYING:

				progress.setIndeterminate(duration == -1);
				progress.setMax(duration / SEC);

				if (progressTimer != null) {
					progressTimer.cancel();
				}

				Log.w("Player", String.format("Starting timer at %d of %d",
							position / SEC, duration / SEC));
				progressTimer = new CountDownTimer(duration - position, SEC) {

					@Override
					public void onTick(long remaining) {
						if (allowProgressUpdates) {
							progress.setProgress((duration - (int) remaining)
									/ SEC);
							Log.v("Player", String.format(
									"Progress is %d of %d",
									(duration - (int) remaining) / SEC,
									duration / SEC));
						}
					}

					@Override
					public void onFinish() {
						// Do nothing
					}
				}.start();

				break;
			case PlayerService.PROGRESS_STATE_PAUSED:
				progress.setProgress(position / SEC);
				progress.setMax(duration / SEC);
				progress.setIndeterminate(false);
				if (progressTimer != null) {
					progressTimer.cancel();
					progressTimer = null;
				}
				break;
			default:
				progress.setProgress(0);
				progress.setMax(0);
				if (progressTimer != null) {
					progressTimer.cancel();
					progressTimer = null;
				}

			}

		}
	}

}
