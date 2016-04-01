package pconley.vamp.library.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pconley.vamp.R;
import pconley.vamp.library.action.LibraryFilterAction;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.player.view.PlayerActivity;
import pconley.vamp.preferences.view.SettingsActivity;
import pconley.vamp.scanner.view.ScannerProgressDialogFragment;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.BroadcastConstants;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity {

	public static final String LIBRARY_ROOT_TAG = "pconley.vamp.library.root";

	/*
	 * Receive status messages from the player. Only necessary to show errors.
	 */
	private BroadcastReceiver playerEventReceiver;
	private LocalBroadcastManager broadcastManager;

	private TagHistoryView tagHistory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		loadLibrary();

		tagHistory = (TagHistoryView) findViewById(
				R.id.library_tag_history);
		tagHistory.setHasFixedSize(true);
		tagHistory.setLayoutManager(
				new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
				                        false));

		broadcastManager = LocalBroadcastManager.getInstance(this);
		playerEventReceiver = new PlayerEventReceiver();
	}

	@Override
	protected void onStart() {
		super.onStart();

		IntentFilter filter = new IntentFilter(
				BroadcastConstants.FILTER_PLAYER_EVENT);
		broadcastManager.registerReceiver(playerEventReceiver, filter);
	}

	@Override
	protected void onStop() {
		broadcastManager.unregisterReceiver(playerEventReceiver);

		new LibraryOpenHelper(this).close();

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
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
				ScannerProgressDialogFragment scanningDialog
						= new ScannerProgressDialogFragment();
				scanningDialog.show(getFragmentManager(),
				                    ScannerProgressDialogFragment.TAG);

				startService(new Intent(this, ScannerService.class));

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		tagHistory.pop();
	}

	public void onClickPlayContents(View view) {
		((LibraryFragment) getFragmentManager()
				.findFragmentById(R.id.library_container))
				.playContents();
	}

	/**
	 * Reload the library when the page is opened or after the device is
	 * scanned. Existing filters may have been invalidated by the scan.
	 */
	public void loadLibrary() {
		getFragmentManager().popBackStack(LIBRARY_ROOT_TAG,
		                FragmentManager.POP_BACK_STACK_INCLUSIVE);
		new LibraryFilterAction(this).execute(null, -1);
	}

	private class PlayerEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
				Toast.makeText(
						LibraryActivity.this,
						intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE),
						Toast.LENGTH_LONG).show();
			}
		}
	}

}
