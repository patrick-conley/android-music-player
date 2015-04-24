package pconley.vamp.library;

import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.player.PlayerService;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.scanner.ScannerEvent;
import pconley.vamp.scanner.ScannerProgressDialogFragment;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.BroadcastConstants;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.Toast;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity {

	private ListView trackListView;
	private long[] trackIds;

	private ScannerProgressDialogFragment scanningDialog;

	private LocalBroadcastManager broadcastManager;

	/*
	 * Receive status messages from the player. Only necessary to show errors.
	 */
	private BroadcastReceiver playerEventReceiver;

	/*
	 * Receiver completion notice from the media scanner.
	 */
	private ScannerBroadcastReceiver scannerReceiver;

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

		scannerReceiver = new ScannerBroadcastReceiver();

		// Get the list of tracks in the library. If one is clicked, play it and
		// open the Now Playing screen.
		new LoadTrackListTask().execute();

		trackListView = (ListView) findViewById(R.id.track_list);
		trackListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent(LibraryActivity.this,
						PlayerService.class);
				intent.setAction(PlayerService.ACTION_PLAY)
						.putExtra(PlayerService.EXTRA_TRACKS, trackIds)
						.putExtra(PlayerService.EXTRA_START_POSITION, position);
				startService(intent);

				startActivity(new Intent(LibraryActivity.this,
						PlayerActivity.class));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		broadcastManager.registerReceiver(playerEventReceiver,
				new IntentFilter(BroadcastConstants.FILTER_PLAYER_EVENT));
		broadcastManager.registerReceiver(scannerReceiver, new IntentFilter(
				BroadcastConstants.FILTER_SCANNER));

	}

	@Override
	protected void onPause() {
		broadcastManager.unregisterReceiver(playerEventReceiver);
		broadcastManager.unregisterReceiver(scannerReceiver);

		if (scanningDialog != null) {
			scanningDialog.dismiss();
			scanningDialog = null;
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

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread.
	 */
	private class LoadTrackListTask extends AsyncTask<Void, Void, List<Long>> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			dialog = new ProgressDialog(LibraryActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.show();
		}

		@Override
		protected List<Long> doInBackground(Void... params) {
			return new TrackDAO(LibraryActivity.this).openReadableDatabase()
					.getIds();
		}

		protected void onPostExecute(List<Long> ids) {
			ArrayAdapter<Long> adapter = new ArrayAdapter<Long>(
					LibraryActivity.this, R.layout.track_list_item,
					R.id.track_list_item, ids);

			trackListView.setAdapter(adapter);

			// Get a list of the track IDs as required for intent extras:
			// there's no built-in means of converting these to primitives.
			trackIds = new long[ids.size()];

			for (int i = 0; i < trackIds.length; i++) {
				trackIds[i] = ids.get(i);
			}

			dialog.dismiss();
		}

	}

	private class ScannerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			switch ((ScannerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT)) {
			case FINISHED:

				scanningDialog.dismiss();
				scanningDialog = null;
				new LoadTrackListTask().execute();

				Toast.makeText(
						LibraryActivity.this,
						intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE),
						Toast.LENGTH_LONG).show();

				break;
			case UPDATE:

				if (intent.hasExtra(BroadcastConstants.EXTRA_MAX)) {
					scanningDialog.setIndeterminate(false);
					scanningDialog.setMax(intent.getIntExtra(
							BroadcastConstants.EXTRA_MAX, 0));
				}
				if (intent.hasExtra(BroadcastConstants.EXTRA_PROGRESS)) {
					scanningDialog.setProgress(intent.getIntExtra(
							BroadcastConstants.EXTRA_PROGRESS, 0));
				}

				if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
					scanningDialog.displayComment(intent
							.getStringExtra(BroadcastConstants.EXTRA_MESSAGE));
				}

				break;
			}

		}

	}

}
