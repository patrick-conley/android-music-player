package pconley.vamp;

import java.io.File;
import java.util.List;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.player.PlayerService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity {

	private ListView trackListView;

	/*
	 * Receive status messages from the player
	 */
	private BroadcastReceiver playerReceiver;

	private static final String trackName = "sample_1.m4a";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		playerReceiver = new PlayerService.StatusReceiver(this);

		new LoadTrackListTask().execute(false);

		trackListView = (ListView) findViewById(R.id.track_list);
		trackListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(LibraryActivity.this,
						TrackViewActivity.class);
				intent.putExtra(TrackViewActivity.EXTRA_ID,
						(long) parent.getItemAtPosition(position));
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				playerReceiver,
				new IntentFilter(PlayerService.FILTER_PLAYER_STATUS));

	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				playerReceiver);

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.library, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_player:
			if (launchPlayer()) {
				startActivity(new Intent(this, PlayerActivity.class));
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Delete the contents of the library and replace it with sample contents.
	 *
	 * TODO: delete this (and the contents of the library) when I can read the
	 * real thing.
	 *
	 * @param view
	 *            The origin of the rebuild request
	 */
	public void createLibrary(View view) {
		new LoadTrackListTask().execute(true);
	}

	/*
	 * Start the music player with a single track. Returns false if the track
	 * didn't exist.
	 */
	private boolean launchPlayer() {
		File track = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
				trackName);

		if (!track.exists()) {
			Log.w("Library", "Missing track " + track);
			Toast.makeText(this, "Missing track " + track, Toast.LENGTH_LONG)
					.show();
			return false;
		}

		Intent intent = new Intent(this, PlayerService.class).setAction(
				PlayerService.ACTION_PLAY).setData(Uri.fromFile(track));
		startService(intent);

		return true;
	}

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread; the task displays a progress bar while
	 * working.
	 * 
	 * The library is first deleted and rebuilt if `LoadTrackListTask.execute`
	 * is called with a true parameter.
	 */
	private class LoadTrackListTask extends
			AsyncTask<Boolean, Void, List<Long>> {

		@Override
		protected List<Long> doInBackground(Boolean... params) {
			// Create a sample library.
			if (params.length > 0 && params[0] == true) {
				try {
					TrackDAO.createSampleLibrary(LibraryActivity.this);
				} catch (Exception e) {
					Log.w("Library", e.getMessage());
				}
			}

			return new TrackDAO(LibraryActivity.this).getIds();
		}

		protected void onPostExecute(List<Long> ids) {
			ArrayAdapter<Long> adapter = new ArrayAdapter<Long>(
					LibraryActivity.this, R.layout.track_list_item,
					R.id.track_list_item, ids);

			trackListView.setAdapter(adapter);
		}

	}

}
