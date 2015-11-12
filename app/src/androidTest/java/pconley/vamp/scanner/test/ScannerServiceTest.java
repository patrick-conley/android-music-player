package pconley.vamp.scanner.test;

import static android.test.MoreAsserts.assertEmpty;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import pconley.vamp.R;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.scanner.ScannerEvent;
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
	private static final int WAIT_TIME = 1000;

	private SharedPreferences preferences;
	private Intent scannerIntent;
	private TrackDAO dao;

	private BroadcastReceiver receiver;
	private String finalStatus;

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
		SettingsHelper.setPreferences(preferences);

		dao = new TrackDAO(context).openWritableDatabase();

		scannerIntent = new Intent(getContext(), ScannerService.class);

		finalStatus = null;

		LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
				new IntentFilter(BroadcastConstants.FILTER_SCANNER));
	}

	@Override
	protected void tearDown() throws Exception {
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(
				receiver);

		preferences.edit().putBoolean(SettingsHelper.KEY_DEBUG, false).commit();

		dao.wipeDatabase();
		dao.close();

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
				latch.await(WAIT_TIME, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scanner requires non-debug mode",
				getContext().getString(R.string.scan_error_debug_mode),
				finalStatus);
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
				latch.await(WAIT_TIME, TimeUnit.MILLISECONDS));
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
				latch.await(WAIT_TIME, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scanner requires a music folder",
				getContext().getString(R.string.scan_error_no_music_folder),
				finalStatus);
	}

	/**
	 * Given preferences are valid, when I run the scanner, then it finishes
	 * with a message.
	 */
	public void testSuccessfulScan() throws InterruptedException, IOException {
		// Given
		AssetUtils.setupMusicFolder(getContext());

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(WAIT_TIME, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scanner runs with valid app preferences", getContext()
				.getString(R.string.scan_done), finalStatus);
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
		Track expected = AssetUtils.addAssetToFolder((Context) getClass()
				.getMethod("getTestContext").invoke(this), AssetUtils.OGG,
				new File(musicFolder, "sample.ogg"));

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(WAIT_TIME*2, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scan completed",
				getContext().getString(R.string.scan_done), finalStatus);
		assertEquals("Scanner writes to the library",
				Arrays.asList(new Track[] { expected }), dao.getAllTracks());

		FileUtils.deleteDirectory(musicFolder);
	}

	/**
	 * Given the database contains a track, when I run the scanner, then the
	 * database is first cleared.
	 */
	public void testDatabaseIsCleared() throws IOException,
			InterruptedException {
		File musicFolder = AssetUtils.setupMusicFolder(getContext());
		AssetUtils.addTrackToDb(getContext(), musicFolder);

		// When
		latch = new CountDownLatch(1);
		startService(scannerIntent);
		assertTrue("Scanner completes on time",
				latch.await(WAIT_TIME, TimeUnit.MILLISECONDS));

		// Then
		assertEquals("Scan completed",
				getContext().getString(R.string.scan_done), finalStatus);
		assertEmpty("Library is cleared", dao.getAllTracks());

		FileUtils.deleteDirectory(musicFolder);
	}

	/**
	 * Listen for broadcasts from ScannerService and set the global
	 * {@link ScannerServiceTest#finalStatus} according to the broadcast's
	 * message.
	 */
	private class ScannerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ScannerEvent event = (ScannerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT);

			if (event == ScannerEvent.FINISHED) {
				finalStatus = intent
						.getStringExtra(BroadcastConstants.EXTRA_MESSAGE);
				latch.countDown();
			}
		}

	}
}
