package pconley.vamp.scanner.test;

import static android.test.MoreAsserts.assertContentsInAnyOrder;
import static android.test.MoreAsserts.assertEmpty;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.library.model.Track;
import pconley.vamp.scanner.FilesystemScanner;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.BroadcastConstants;
import pconley.vamp.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Tests against the FilesystemScanner:
 * 
 * <ul>
 * <li>Empty directory
 * <li>Non-media file
 * <li>Ogg Vorbis file
 * <li>FLAC file
 * <li>Ogg Vorbis and non-media files together
 * <li>Ogg Vorbis and FLAC files together
 * <li>Empty child directory
 * <li>Ogg Vorbis in a child directory
 * <li>Ogg Vorbis file and .nomedia, FLAC file in a child directory
 * <li>Ogg Vorbis file, FLAC file and .nomedia in a child directory
 * <li>Untested: media file and a symlink to it
 * <li>Untested: directory loop
 * </ul>
 * 
 * Untested behaviour relies on links, which don't have a public API (they can
 * be implemented in native code), and which can't be created on VFAT
 * filesystems anyway.
 * 
 * @author pconley
 *
 */
public class FilesystemScannerTest extends InstrumentationTestCase {

	private File musicFolder;
	private FilesystemScanner scanner;
	private TrackDAO dao;

	private Context testContext;
	private Context targetContext;

	// Latch counts down each time a file or directory is scanned
	private BroadcastReceiver receiver;
	private CountDownLatch latch;
	private int WAIT_TIME = 250;

	private List<Integer> receivedProgress;
	private List<String> receivedDirs;

	// Expected broadcast messages for a music folder without children
	private List<String> singleDir;

	public FilesystemScannerTest() {
		receivedProgress = new LinkedList<Integer>();
		receivedDirs = new LinkedList<String>();

		singleDir = new LinkedList<String>();
		singleDir.add("");
	}

	public void setUp() throws Exception {
		super.setUp();

		testContext = getInstrumentation().getContext();
		targetContext = new RenamingDelegatingContext(getInstrumentation()
				.getTargetContext(), Constants.DB_PREFIX);

		musicFolder = AssetUtils.setupMusicFolder(targetContext);

		scanner = new FilesystemScanner(targetContext, musicFolder);
		dao = new TrackDAO(targetContext).openReadableDatabase();

		receivedProgress.clear();
		receivedDirs.clear();

		receiver = new ScannerBroadcastReceiver();
		LocalBroadcastManager.getInstance(targetContext).registerReceiver(
				receiver, new IntentFilter(BroadcastConstants.FILTER_SCANNER));
	}

	public void tearDown() throws Exception {
		LocalBroadcastManager.getInstance(targetContext).unregisterReceiver(
				receiver);

		dao.wipeDatabase();
		dao.close();
		FileUtils.deleteDirectory(musicFolder);

		super.tearDown();
	}

	/**
	 * Given an empty media directory, when I scan media, then the database is
	 * unmodified
	 */
	public void testEmptyDirectory() throws InterruptedException {
		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(1);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEmpty("No progress is recorded", receivedProgress);
		assertEquals("Folders scanned are correct", singleDir, receivedDirs);

		assertEquals("No files are found in an empty directory", 0, count);
		assertEmpty("No files are scanned from an empty directory",
				dao.getTracks());
	}

	/**
	 * Given a media directory containing a single non-media file, when I scan
	 * media, then the database is unmodified.
	 */
	public void testSingleNonMediaFile() throws IOException,
			InterruptedException {
		// Given
		File.createTempFile("sample", null, musicFolder);

		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(2);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEquals("Files are recorded", Arrays.asList(new Integer[] { 1 }),
				receivedProgress);
		assertEquals("Folders scanned are correct", singleDir, receivedDirs);
		assertEquals("A non-media file is counted", 1, count);
		assertEmpty(
				"No files are scanned from a directory with no media files",
				dao.getTracks());

	}

	/**
	 * Given a media directory containing a single Ogg Vorbis file, when I scan
	 * media, then the database contains the file and its tags.
	 */
	public void testSingleOgg() throws IOException, InterruptedException {
		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, new File(musicFolder, "sample.ogg"));

		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(2);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEquals("Files are recorded", Arrays.asList(new Integer[] { 1 }),
				receivedProgress);
		assertEquals("Folders scanned are correct", singleDir, receivedDirs);

		assertEquals("One file is found", 1, count);

		assertEquals("The Ogg Vorbis file was scanned",
				Arrays.asList(new Track[] { expected }), dao.getTracks());
	}

	/**
	 * Given an empty media directory and a non-empty database, when I scan
	 * media, then the database is cleared.
	 */
	public void testDatabaseIsCleared() throws InterruptedException,
			IOException {
		// Given
		dao.openWritableDatabase();
		dao.insertTrack(Uri.fromFile(new File(musicFolder, "sample.ogg")));

		// When
		scanner.scanMusicFolder();

		// Then
		assertEmpty("Database is emptied before scanning", dao.getTracks());

	}

	/**
	 * Given a media directory containing a media and a non-media file, when I
	 * scan media, then the database contains the media file and its tags.
	 */
	public void testMixedFiles() throws IOException, InterruptedException {
		// Given
		File.createTempFile("sample", null, musicFolder);
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, new File(musicFolder, "sample.ogg"));

		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(3);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEquals("Files are recorded",
				Arrays.asList(new Integer[] { 1, 2 }), receivedProgress);
		assertEquals("Folders scanned are correct", singleDir, receivedDirs);

		assertEquals("Media and non-media files are counted", 2, count);

		assertEquals("The Ogg Vorbis file was scanned",
				Arrays.asList(new Track[] { expected }), dao.getTracks());
	}

	/**
	 * Given a media directory containing two media files, when I scan media,
	 * then the database contains both files and their tags.
	 */
	public void testTwoFiles() throws IOException, InterruptedException {
		// Given
		Track exp1 = AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG,
				new File(musicFolder, "sample_1.ogg"));
		Track exp2 = AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG,
				new File(musicFolder, "sample_2.ogg"));

		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(3);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEquals("Files are recorded",
				Arrays.asList(new Integer[] { 1, 2 }), receivedProgress);
		assertEquals("Folders scanned are correct", singleDir, receivedDirs);

		assertEquals("Multiple media files are counted", 2, count);

		assertContentsInAnyOrder("Two files are scanned", dao.getTracks(),
				exp1, exp2);
	}

	/**
	 * Given a media directory containing a single, empty directory, when I scan
	 * media, then the database is unmodified.
	 */
	public void testEmptyChildDirectory() throws InterruptedException {
		// Given
		new File(musicFolder, "sample").mkdir();

		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(2);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEmpty("No files are recorded", receivedProgress);
		assertEquals("Folders scanned are correct",
				Arrays.asList(new String[] { "", "sample" }), receivedDirs);

		assertEquals("Directories are not counted", 0, count);
		assertEmpty("No files are scanned in an empty directory",
				dao.getTracks());
	}

	/**
	 * Given a media directory containing a single media file inside a
	 * directory, when I scan media, then the database contains the file and its
	 * tags.
	 */
	public void testSingleFileInChildDirectory() throws IOException,
			InterruptedException {
		// Given
		File folder = new File(musicFolder, "sample");
		folder.mkdir();
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, new File(folder, "sample.ogg"));

		// When
		int count = scanner.countMusicFiles();
		latch = new CountDownLatch(3);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEquals("Files are recorded", Arrays.asList(new Integer[] { 1 }),
				receivedProgress);
		assertEquals("Folders scanned are correct",
				Arrays.asList(new String[] { "", "sample" }), receivedDirs);

		assertEquals("Files in child directories are counted", 1, count);

		assertEquals("The Ogg Vorbis file was scanned",
				Arrays.asList(new Track[] { expected }), dao.getTracks());
	}

	/**
	 * Given a media directory containing a media file and .nomedia file, and a
	 * child directory containing another media file, when I scan media, then
	 * the database is unmodified.
	 */
	public void testNoMedia() throws IOException, InterruptedException {
		// Given
		File folder = new File(musicFolder, "sample");
		folder.mkdir();

		new File(musicFolder, ".nomedia").createNewFile();

		AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG, new File(
				musicFolder, "sample.ogg"));
		AssetUtils.addAssetToFolder(testContext, AssetUtils.FLAC, new File(
				folder, "sample.flac"));

		// When
		int count = scanner.countMusicFiles();
		latch = new CountDownLatch(0);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEmpty("No files are recorded", receivedProgress);
		assertEmpty("No folders are recorded", receivedDirs);

		assertEquals(".nomedia directories are not counted", 0, count);
		assertEmpty(".nomedia is respected by the scanner", dao.getTracks());
	}

	/**
	 * Given a media directory containing a media file, and a child directory
	 * containing another media file and a .nomedia file, when I scan media,
	 * then the database contains the first file and its tags.
	 */
	public void testNoMediaInChild() throws IOException, InterruptedException {
		// Given
		File folder = new File(musicFolder, "sample");
		folder.mkdir();

		new File(folder, ".NOMEDIA").createNewFile();

		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, new File(musicFolder, "sample.ogg"));
		AssetUtils.addAssetToFolder(testContext, AssetUtils.FLAC, new File(
				folder, "sample.flac"));

		// When
		int count = scanner.countMusicFiles();

		latch = new CountDownLatch(2);
		scanner.scanMusicFolder();
		latch.await(WAIT_TIME, TimeUnit.MILLISECONDS);

		// Then
		assertEquals("Files are recorded", Arrays.asList(new Integer[] { 1 }),
				receivedProgress);
		assertEquals("Folders scanned are correct", singleDir, receivedDirs);

		assertEquals(".nomedia child directories are not counted", 1, count);
		assertEquals(".nomedia child directories are not scanned",
				Arrays.asList(new Track[] { expected }), dao.getTracks());
	}

	public class ScannerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra(BroadcastConstants.EXTRA_PROGRESS)) {
				receivedProgress.add(intent.getIntExtra(
						BroadcastConstants.EXTRA_PROGRESS, 0));
			}

			if (intent.hasExtra(BroadcastConstants.EXTRA_MESSAGE)) {
				receivedDirs.add(intent
						.getStringExtra(BroadcastConstants.EXTRA_MESSAGE));
			}

			if (latch != null) {
				latch.countDown();
			}
		}

	}

}
