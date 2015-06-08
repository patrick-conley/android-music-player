package pconley.vamp.scanner;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.filesystem.MediaFolder;
import pconley.vamp.model.filesystem.MediaVisitorBase;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.scanner.filesystem.FileCountVisitor;
import pconley.vamp.scanner.filesystem.FileScanVisitor;
import pconley.vamp.util.BroadcastConstants;
import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Scan for media files in the Media Folder defined in the app's preferences;
 * add conventional metadata from these files to the app database. ".nomedia"
 * files are respected. Work is done in a background thread.
 * 
 * Sends a broadcast with {@link BroadcastConstants#FILTER_SCANNER} when
 * <ul>
 * <li>It enters a folder (includes folder name, total files)
 * <li>It looks at a file (includes progress)
 * <li>If a file couldn't be scanned because of an invalid tag
 * <li>On completion
 * <li>If the scan was aborted because of invalid settings or a DB error.
 * </ul>
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
		}

		if (settings.getMusicFolder() == null) {
			Log.w(TAG, "Abort scan: no music folder set");
			broadcastResult(R.string.scan_error_no_music_folder);
			return;
		}

		// Clear the database
		TrackDAO dao = new TrackDAO(getBaseContext()).openWritableDatabase();
		dao.wipeDatabase();
		dao.close();

		MediaFolder musicFolder = new MediaFolder(settings.getMusicFolder());

		// Count
		MediaVisitorBase visitor = new FileCountVisitor();
		musicFolder.accept(visitor);

		Intent countIntent = new Intent(BroadcastConstants.FILTER_SCANNER);
		countIntent.putExtra(BroadcastConstants.EXTRA_EVENT,
				ScannerEvent.UPDATE);
		countIntent.putExtra(BroadcastConstants.EXTRA_TOTAL,
				((FileCountVisitor) visitor).getCount());
		LocalBroadcastManager.getInstance(this).sendBroadcast(countIntent);

		// Scan
		visitor = new FileScanVisitor(settings.getMusicFolder(),
				getBaseContext());

		try {
			musicFolder.accept(visitor);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
			broadcastResult(R.string.scan_error_db, e.getMessage());
		}

		((FileScanVisitor) visitor).close();

		Log.i(TAG, "Scan complete");
		broadcastResult(R.string.scan_done);
	}

	private void broadcastResult(int scanStatus, Object... args) {
		Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
		intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.FINISHED);
		intent.putExtra(BroadcastConstants.EXTRA_MESSAGE,
				getString(scanStatus, args));

		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
				intent);
	}

}
