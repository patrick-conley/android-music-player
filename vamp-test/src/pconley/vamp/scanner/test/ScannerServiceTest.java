package pconley.vamp.scanner.test;

import static android.test.MoreAsserts.assertNotEmpty;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import pconley.vamp.R;
import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.BroadcastConstants;
import pconley.vamp.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.test.RenamingDelegatingContext;
import android.test.ServiceTestCase;

public class ScannerServiceTest extends ServiceTestCase<ScannerService> {

	/*
	 * The Service's onHandleIntent is run asynchronously, so this latch ensures
	 * the test method waits until the service has finished.
	 * 
	 * The latch is decremented in the BroadcastReceiver, as this service always
	 * ends with a broadcast. If there were no broadcast, I would have to run
	 * tests against a subclass of ScannerService, and decrement the latch at
	 * the end of the overridden onHandleIntent.
	 */
	private CountDownLatch latch;

	private BroadcastReceiver receiver;
	private SharedPreferences preferences;
	private Intent scannerIntent;

	private TrackDAO dao;

	private String status;

	public ScannerServiceTest() {
		super(ScannerService.class);

		receiver = new ScannerBroadcastReceiver();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(),
				Constants.DB_PREFIX);
		setContext(context);

		preferences = context.getSharedPreferences(Constants.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		preferences.edit().putBoolean(SettingsHelper.KEY_DEBUG, false).commit();
		SettingsHelper.setPreferences(preferences);

		dao = new TrackDAO(context);

		scannerIntent = new Intent(getContext(), ScannerService.class);

		LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
				new IntentFilter(BroadcastConstants.FILTER_SCANNER));
	}

	@Override
	protected void tearDown() throws Exception {
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(
				receiver);

		dao.openWritableDatabase();
		dao.wipeDatabase();
		dao.close();

		status = null;

		super.tearDown();
	}

	/**
	 * Given the app is running in debug mode, when run the scanner, then it
	 * aborts with a message.
	 */
	public void testDebugMode() throws InterruptedException {
		// Given
		preferences.edit().putBoolean(SettingsHelper.KEY_DEBUG, true).commit();

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(1000, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scanner requires non-debug mode",
				getContext().getString(R.string.scan_error_debug_mode), status);
	}

	/**
	 * Given debug mode is scan, when I run the scanner several times, then it
	 * sends multiple (failed) completion broadcasts.
	 */
	public void testRepeatedRuns() throws InterruptedException {
		// Given
		preferences.edit().putBoolean(SettingsHelper.KEY_DEBUG, true).commit();

		latch = new CountDownLatch(3);
		startService(scannerIntent);
		startService(scannerIntent);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(1000, TimeUnit.MILLISECONDS));

		// Then (failure to complete will send an InterruptedException)

	}

	/**
	 * Given the music folder has not been set, when I run the scanner, then it
	 * aborts with a message.
	 */
	public void testNullMusicFolder() throws InterruptedException {
		// Given
		preferences.edit().remove(SettingsHelper.KEY_MUSIC_FOLDER).commit();

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(1000, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scanner requires a music folder",
				getContext().getString(R.string.scan_error_no_music_folder),
				status);
	}

	/**
	 * Given preferences are valid, when I run the scanner, then it finishes
	 * with a message
	 */
	public void testSuccessfulScan() throws InterruptedException, IOException {
		// Given
		AssetUtils.setupMusicFolder(getContext());

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(1000, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scanner runs with valid app preferences", getContext()
				.getString(R.string.scan_done), status);
	}

	/**
	 * Given the music folder is valid and contains a music file, when I run the
	 * scanner, then that Track is added to the database.
	 */
	public void testMusicIsScanned() throws InterruptedException, IOException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {
		// Given
		File musicFolder = AssetUtils.setupMusicFolder(getContext());
		Track expected = AssetUtils.copyMusicAsset((Context) getClass()
				.getMethod("getTestContext").invoke(this), AssetUtils.OGG,
				new File(musicFolder, "sample.ogg"));

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(1000, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scan completed",
				getContext().getString(R.string.scan_done), status);
		dao.openReadableDatabase();
		List<Long> ids = dao.getIds();
		assertNotEmpty("Files have been scanned", ids);
		assertEquals("Scanner writes to the library", expected,
				dao.getTrack(ids.get(0)));

	}

	/**
	 * Listen for broadcasts from ScannerService and set the global
	 * {@link ScannerServiceTest#status} according to the broadcast's message.
	 */
	private class ScannerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			status = intent.getStringExtra(BroadcastConstants.EXTRA_MESSAGE);
			latch.countDown();
		}

	}
}
