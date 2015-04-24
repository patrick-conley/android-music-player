package pconley.vamp.scanner;

import pconley.vamp.R;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.BroadcastConstants;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Scan the media folder defined in the app's preferences. Work is performed in
 * a worker thread.
 * 
 * Scan for media files in the Media Folder defined in the app's preferences;
 * add conventional metadata from these files to the app database. A ".nomedia"
 * file is respected. Work is done in a background thread.
 * 
 * When a scan is complete a broadcast message with
 * {@link BroadcastConstants#FILTER_SCANNER} will be sent.
 * 
 * @author pconley
 */
public class ScannerService extends IntentService {
	private static final String TAG = "Scanner Service";

	/**
	 * Constructor. Do not call this explicitly.
	 */
	public ScannerService() {
		super("ScannerService");

		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SettingsHelper settings = new SettingsHelper(this);

		// Prohibit scanning into the sample library
		if (settings.getDebugMode()) {
			Log.w(TAG, "Abort scan: debug mode is set");
			broadcastResult(R.string.scan_error_debug_mode);
			return;
		} else if (settings.getMusicFolder() == null) {
			Log.w(TAG, "Abort scan: no music folder set");
			broadcastResult(R.string.scan_error_no_music_folder);
			return;
		}

		FilesystemScanner scanner = new FilesystemScanner(getBaseContext());
		scanner.countFolders();
		scanner.scanMusicFolder();

		Log.i(TAG, "Scan complete");
		broadcastResult(R.string.scan_done);
	}

	private void broadcastResult(int scanStatus) {
		Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);

		intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.FINISHED);
		intent.putExtra(BroadcastConstants.EXTRA_MESSAGE, getString(scanStatus));

		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
				intent);
	}

}
