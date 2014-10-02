package pconley.vamp;

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.db.TrackDAO;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity {

	private ListView trackListView;

	private static ArrayList<String> tracks;

	static {
		tracks = new ArrayList<String>();
		tracks.add("sample_1.m4a");
		tracks.add("sample_1.ogg");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		trackListView = (ListView) findViewById(R.id.track_list);

		new LoadTrackListTask().execute(false);

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
			Intent intent = new Intent(this, PlayerActivity.class);
			intent.setAction(PlayerActivity.ACTION_PLAY);
			intent.putStringArrayListExtra(PlayerActivity.EXTRA_TRACKS, tracks);
			startActivity(intent);
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
