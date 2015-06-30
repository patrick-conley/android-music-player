package pconley.vamp.library;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import pconley.vamp.R;
import pconley.vamp.library.action.LibraryActionLocator;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.scanner.ScannerProgressDialogFragment;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.BroadcastConstants;

/**
 * Main activity, showing the contents of the library.
 */
public class LibraryActivity extends Activity
		implements AdapterView.OnItemClickListener {

	public static final String LIBRARY_ROOT_TAG = "pconley.vamp.library.root";

	/*
	 * Receive status messages from the player. Only necessary to show errors.
	 */
	private BroadcastReceiver playerEventReceiver;
	private LocalBroadcastManager broadcastManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		loadLibrary();

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

	/**
	 * Callback for items selected in a {@Link LibraryFragment}. Selecting a Tag
	 * will replace the fragment with a new, filtered fragment; selecting a
	 * Track will play the fragment's contents.
	 *
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	@Override
	@SuppressWarnings(value="unchecked")
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ArrayAdapter<LibraryItem> adapter
				= (ArrayAdapter<LibraryItem>) parent.getAdapter();

		LibraryActionLocator.findAction(adapter.getItem(position))
		             .execute(this, adapter, position);
	}

	/**
	 * Reload the library after the device is scanned. Existing filters may have
	 * been invalidated by the scan.
	 */
	public void loadLibrary() {
		FragmentManager fm = getFragmentManager();

		fm.popBackStack(LIBRARY_ROOT_TAG,
		                FragmentManager.POP_BACK_STACK_INCLUSIVE);
		fm.beginTransaction()
		  .replace(R.id.library, LibraryFragment.newInstance())
		  .commit();
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
