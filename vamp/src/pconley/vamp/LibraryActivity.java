package pconley.vamp;

import java.util.List;

import pconley.vamp.db.TrackDAO;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

	public static final String ID_NAME = "pconley.vamp.track_id";

	private ListView trackListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		trackListView = (ListView) findViewById(R.id.track_list);

		new LoadTrackListTask().execute();

		trackListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(LibraryActivity.this,
						TrackViewActivity.class);
				intent.putExtra(ID_NAME,
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread; the task displays a progress bar wihle
	 * working.
	 */
	private class LoadTrackListTask extends AsyncTask<Void, Void, List<Long>> {

		@Override
		protected List<Long> doInBackground(Void... params) {
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
