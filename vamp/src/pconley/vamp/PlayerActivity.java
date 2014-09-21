package pconley.vamp;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PlayerActivity extends Activity {

	private static final String SAMPLE_NAME = "sample_1.ogg";

	// private SeekBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		// progress = (SeekBar) findViewById(R.id.playback_progress);

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

	//
	// @Override
	// public void onPrepared(MediaPlayer mp) {
	// progress.setProgress(0);
	// progress.setMax(player.getDuration());
	// progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar seekBar) {
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekBar seekBar) {
	// // TODO: disable periodic progress bar updates - they'll reset
	// // the seek bar before it can perform the seek.
	// }
	//
	// @Override
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	// if (fromUser) {
	// player.seekTo(progress);
	// }
	// }
	// });
	//
	// state = "prepared";
	// playPause(null);
	// }

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

	// /*
	// * Update the progress bar periodically
	// */
	// private void followProgress() {
	//
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// while (state.equals("started") && player != null) {
	// progress.setProgress(player.getCurrentPosition());
	//
	// try {
	// Thread.sleep(200);
	// } catch (InterruptedException e) {
	// Log.d("Player", "Interrupt received (not important)");
	// }
	// }
	// }
	// }).start();
	// }

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

}
