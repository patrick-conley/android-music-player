package pconley.vamp.scanner.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.library.model.Track;
import pconley.vamp.scanner.FilesystemScanner;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;
import static android.test.MoreAsserts.*;

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

	Context testContext;
	Context targetContext;

	public void setUp() throws Exception {
		super.setUp();

		testContext = getInstrumentation().getContext();
		targetContext = new RenamingDelegatingContext(getInstrumentation()
				.getTargetContext(), Constants.DB_PREFIX);

		musicFolder = AssetUtils.setupMusicFolder(targetContext);

		scanner = new FilesystemScanner(targetContext);
		dao = new TrackDAO(targetContext).openReadableDatabase();
	}

	public void tearDown() throws Exception {
		dao.wipeDatabase();
		dao.close();
		FileUtils.deleteDirectory(musicFolder);

		super.tearDown();
	}

	/**
	 * Given an empty media directory, when I scan media, then the database is
	 * unmodified
	 */
	public void testEmptyDirectory() {
		// When
		scanner.scanMediaFolder();

		// Then
		assertEmpty("No files are found in an empty directory", dao.getIds());
	}

	/**
	 * Given a media directory containing a single non-media file, when I scan
	 * media, then the database is unmodified.
	 */
	public void testSingleNonMediaFile() throws IOException {
		// Given
		File.createTempFile("sample", null, musicFolder);

		// When
		scanner.scanMediaFolder();

		// Then
		assertEmpty("No files are found in a directory with no media files",
				dao.getIds());

	}

	/**
	 * Given a media directory containing a single Ogg Vorbis file, when I scan
	 * media, then the database contains the file and its tags.
	 */
	public void testSingleOgg() throws IOException {
		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG,
				new File(musicFolder, "sample.ogg"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned", expected, track);
	}

	/**
	 * Given the database contains data, when I scan media, then the database is
	 * first emptied.
	 */
	public void testDatabaseIsCleared() throws InterruptedException,
			IOException {
		// Given
		dao.openWritableDatabase();
		dao.insertTrack(Uri.fromFile(new File(musicFolder, "sample.ogg")));

		// When
		scanner.scanMediaFolder();

		// Then
		assertEmpty("Database is emptied before scanning", dao.getIds());

	}

	/**
	 * Given a media directory containing a media and a non-media file, when I
	 * scan media, then the database contains the media file and its tags.
	 */
	public void testMixedFiles() throws IOException {
		// Given
		File.createTempFile("sample", null, musicFolder);
		Track expected = AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG,
				new File(musicFolder, "sample.ogg"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned", expected, track);
	}

	/**
	 * Given a media directory containing two media files, when I scan media,
	 * then the database contains both files and their tags.
	 */
	public void testTwoFiles() throws IOException {
		// Given
		Track expected1 = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, new File(musicFolder, "sample_1.ogg"));
		Track expected2 = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, new File(musicFolder, "sample_2.ogg"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("Two files have been found", 2, ids.size());

		Track track = dao.getTrack(ids.get(0));
		if (track.getUri().toString().endsWith("sample_1.ogg")) {
			assertEquals("First file is correct", expected1, track);
			assertEquals("Second file is correct", expected2,
					dao.getTrack(ids.get(1)));
		} else {
			assertEquals("Second file is correct", expected2, track);
			assertEquals("First file is correct", expected1,
					dao.getTrack(ids.get(1)));
		}
	}

	/**
	 * Given a media directory containing a single, empty directory, when I scan
	 * media, then the database is unmodified.
	 */
	public void testEmptyChildDirectory() {
		// Given
		new File(musicFolder, "sample").mkdir();

		// When
		scanner.scanMediaFolder();

		// Then
		assertEmpty("No files are found in an empty directory", dao.getIds());
	}

	/**
	 * Given a media directory containing a single media file inside a
	 * directory, when I scan media, then the database contains the file and its
	 * tags.
	 */
	public void testSingleFileInChildDirectory() throws IOException {
		// Given
		File folder = new File(musicFolder, "sample");
		folder.mkdir();
		Track expected = AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG,
				new File(folder, "sample.ogg"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned", expected, track);
	}

	/**
	 * Given a media directory containing a media file and .nomedia file, and a
	 * child directory containing another media file, when I scan media, then
	 * the database is unmodified.
	 */
	public void testNoMedia() throws IOException {
		// Given
		File folder = new File(musicFolder, "sample");
		folder.mkdir();

		new File(musicFolder, ".nomedia").createNewFile();

		AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG, new File(
				musicFolder, "sample.ogg"));
		AssetUtils.addAssetToFolder(testContext, AssetUtils.FLAC, new File(
				folder, "sample.flac"));

		// When
		scanner.scanMediaFolder();

		// Then
		assertEmpty("No files are found in an empty directory", dao.getIds());
	}

	/**
	 * Given a media directory containing a media file, and a child directory
	 * containing another media file and a .nomedia file, when I scan media,
	 * then the database contains the first file and its tags.
	 */
	public void testNoMediaInChild() throws IOException {
		// Given
		File folder = new File(musicFolder, "sample");
		folder.mkdir();

		new File(folder, ".NOMEDIA").createNewFile();

		Track expected = AssetUtils.addAssetToFolder(testContext, AssetUtils.OGG,
				new File(musicFolder, "sample.ogg"));
		AssetUtils.addAssetToFolder(testContext, AssetUtils.FLAC, new File(
				folder, "sample.flac"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned", expected, track);
	}

}
