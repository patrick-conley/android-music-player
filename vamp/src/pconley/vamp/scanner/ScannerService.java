package pconley.vamp.scanner;

import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.BroadcastConstants;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Scan the media folder defined in the app's preferences. Work is performed in
 * a worker thread. If scan Intents are sent while a scan is in progress, they
 * will be quietly ignored.
 * 
 * When a scan is complete a broadcast message with
 * {@link BroadcastConstants#FILTER_SCANNER} will be sent.
 * 
 * @author pconley
 *
 */
public class ScannerService extends IntentService {
	private static final String TAG = "Scanner Service";

	private boolean isRunningScan = false;

	private FilesystemScanner scanner;

	/**
	 * Constructor. Do not call this explicitly.
	 * 
	 * @param name
	 */
	public ScannerService() {
		super("ScannerService");

		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SettingsHelper settings = new SettingsHelper(getApplicationContext());

		// Prohibit scanning into the sample library
		if (settings.getDebugMode()) {
			Log.w(TAG, "Abort scan to debug library");
			return;
		} else if (settings.getMusicFolder() == null) {
			Log.w(TAG, "Abort scan: no music folder set");
			return;
		}

		// Ignore intents sent while a scan is in progress
		if (isRunningScan) {
			return;
		}

		scanner = new FilesystemScanner(getApplicationContext());

		isRunningScan = true;
		scanner.scanMediaFolder();
		isRunningScan = false;

		Intent broadcastIntent = new Intent(BroadcastConstants.FILTER_SCANNER);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(broadcastIntent);
	}
}
