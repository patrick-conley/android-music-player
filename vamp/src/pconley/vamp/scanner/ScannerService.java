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
 * When a scan is complete a broadcast message with
 * {@link BroadcastConstants#FILTER_SCANNER} will be sent.
 * 
 * @author pconley
 *
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
		SettingsHelper settings = new SettingsHelper(getBaseContext());

		// Prohibit scanning into the sample library
		if (settings.getDebugMode()) {
			Log.w(TAG, "Abort scan: debug mode is set");
			broadcastScanStatus(R.string.scan_error_debug_mode);
			return;
		} else if (settings.getMusicFolder() == null) {
			Log.w(TAG, "Abort scan: no music folder set");
			broadcastScanStatus(R.string.scan_error_no_music_folder);
			return;
		}
		
		new FilesystemScanner(getBaseContext()).scanMediaFolder();

		Log.i(TAG, "Scan complete");
		broadcastScanStatus(R.string.scan_done);
	}

	private void broadcastScanStatus(int scanStatus) {
		Intent broadcastIntent = new Intent(BroadcastConstants.FILTER_SCANNER);

		broadcastIntent.putExtra(BroadcastConstants.EXTRA_MESSAGE,
				getString(scanStatus));

		LocalBroadcastManager.getInstance(getBaseContext())
				.sendBroadcast(broadcastIntent);
	}

}
