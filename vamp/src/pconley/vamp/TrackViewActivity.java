package pconley.vamp;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Display all the tags for a single track.
 */
public class TrackViewActivity extends Activity {

	public static final String EXTRA_ID = "pconley.vamp.TrackViewActivity.track_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_view);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		new LoadTrackTask().execute(getIntent().getLongExtra(
				EXTRA_ID, -1));
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
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
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
			return new TrackDAO(TrackViewActivity.this).getTrack(params[0]);
		}

		protected void onPostExecute(Track track) {
			((TextView) findViewById(R.id.track_view_uri)).setText(track
					.getUri());
			((TextView) findViewById(R.id.track_view_tags)).setText(track
					.tagsToString());
		}

	}
}
