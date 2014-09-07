package pconley.vamp;

import java.util.List;

import pconley.vamp.db.TrackDAO;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity {

	private ListView trackListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		trackListView = (ListView) findViewById(R.id.track_list);

		new LoadTrackListTask().execute();
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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread; the task displays a progress bar wihle
	 * working.
	 */
	private class LoadTrackListTask extends
			AsyncTask<Void, Void, List<Long>> {

		private ProgressDialog progress;

		public LoadTrackListTask() {
			progress = new ProgressDialog(LibraryActivity.this);
		}

		@Override
		protected List<Long> doInBackground(Void... params) {
			return new TrackDAO(LibraryActivity.this).getIds();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.setMessage("Loading library");
			progress.show();
		}

		protected void onPostExecute(List<Long> ids) {
			if (progress.isShowing()) {
				progress.dismiss();
			}

			ArrayAdapter<Long> adapter = new ArrayAdapter<Long>(
					LibraryActivity.this, R.layout.track_list_item,
					R.id.track_list_item, ids);

			trackListView.setAdapter(adapter);
		}

	}
}
