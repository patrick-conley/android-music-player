package pconley.vamp;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Display all the tags for a single track.
 */
public class TrackViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_view);

		new LoadTrackTask().execute(getIntent().getLongExtra(
				LibraryActivity.ID_NAME, -1));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.track_view, menu);
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
	 * Load the given track from the database, and display it and its tags in
	 * this activity's text field.
	 *
	 * Work is done in a background thread.
	 */
	private class LoadTrackTask extends AsyncTask<Long, Void, Track> {

		private ProgressDialog progress;

		public LoadTrackTask() {
			progress = new ProgressDialog(TrackViewActivity.this);
		}

		@Override
		protected Track doInBackground(Long... params) {
			return new TrackDAO(TrackViewActivity.this).getTrack(params[0]);
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage("Loading track");
			progress.show();
		}

		protected void onPostExecute(Track track) {
			if (progress.isShowing()) {
				progress.dismiss();
			}

			((TextView) findViewById(R.id.track_view_item)).setText(track
					.toString());
		}

	}
}
