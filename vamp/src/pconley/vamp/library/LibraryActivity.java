package pconley.vamp.library;

import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.library.model.Track;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.player.PlayerService;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.scanner.FilesystemScanner;
import pconley.vamp.scanner.ScannerProgressDialogFragment;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.BroadcastConstants;
import pconley.vamp.util.Playlist;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity {

	private ListView trackListView;

	private ScannerProgressDialogFragment scanningDialog;

	private LocalBroadcastManager broadcastManager;

	/*
	 * Receive status messages from the player. Only necessary to show errors.
	 */
	private BroadcastReceiver playerEventReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		broadcastManager = LocalBroadcastManager.getInstance(this);

		playerEventReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
					Toast.makeText(
							LibraryActivity.this,
							intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE),
							Toast.LENGTH_LONG).show();
				}

			}
		};

		// Get the list of tracks in the library. If one is clicked, play it and
		// open the Now Playing screen.
		loadLibrary();

		trackListView = (ListView) findViewById(R.id.track_list);
		trackListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(LibraryActivity.this,
						PlayerService.class);
				intent.setAction(PlayerService.ACTION_PLAY).putExtra(
						PlayerService.EXTRA_START_POSITION, position);
				startService(intent);

				startActivity(new Intent(LibraryActivity.this,
						PlayerActivity.class));
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Check if the filesystem is being scanned and add its dialog if so.
		if (FilesystemScanner.isScanInProgress()) {
			scanningDialog = new ScannerProgressDialogFragment();
			scanningDialog.show(getFragmentManager(), "scan progress");
		}

		broadcastManager.registerReceiver(playerEventReceiver,
				new IntentFilter(BroadcastConstants.FILTER_PLAYER_EVENT));
	}

	@Override
	protected void onPause() {
		broadcastManager.unregisterReceiver(playerEventReceiver);

		if (scanningDialog != null && scanningDialog.isAdded()) {
			scanningDialog.dismiss();
		}

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
			startActivity(new Intent(this, PlayerActivity.class));
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_rescan:
			scanningDialog = new ScannerProgressDialogFragment();
			scanningDialog.show(getFragmentManager(), "scan progress");

			startService(new Intent(this, ScannerService.class));

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Read the contents of the library and fill the library View. Work is done
	 * in a background thread.
	 */
	public void loadLibrary() {
		new LoadTrackListTask().execute();
	}

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread.
	 */
	private class LoadTrackListTask extends AsyncTask<Void, Void, List<Track>> {

		private ProgressBar progress;
		private TrackDAO dao;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress = ((ProgressBar) findViewById(R.id.track_list_progress));
			progress.setVisibility(ProgressBar.VISIBLE);
			dao = new TrackDAO(LibraryActivity.this);
		}

		@Override
		protected List<Track> doInBackground(Void... params) {
			return dao.openReadableDatabase().getTracks();
		}

		protected void onPostExecute(List<Track> tracks) {
			ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(
					LibraryActivity.this, R.layout.track_list_item,
					R.id.track_list_item, tracks);

			trackListView.setAdapter(adapter);
			Playlist.setInstance(new Playlist(tracks));

			dao.close();
			progress.setVisibility(ProgressBar.INVISIBLE);
		}

	}

}
