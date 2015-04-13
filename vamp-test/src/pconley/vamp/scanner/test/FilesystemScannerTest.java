package pconley.vamp.scanner.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;
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
 * <li>MP3 file
 * <li>Ogg Vorbis and non-media files together
 * <li>Ogg Vorbis and MP3 files together
 * <li>Empty child directory
 * <li>Ogg Vorbis in a child directory
 * <li>Ogg Vorbis file and .nomedia, MP3 file in a child directory
 * <li>Ogg Vorbis file, MP3 file and .nomedia in a child directory
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
	 * Given a media directory containing a single MP3 file, when I scan media,
	 * then the database contains the file and its tags.
	 */
	public void testSingleMP3() throws IOException {
		// Given
		Track expected = AssetUtils.copyMusicAsset(testContext, AssetUtils.MP3,
				new File(musicFolder, "sample.mp3"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The MP3 file was scanned", expected, track);

	}

	/**
	 * Given a media directory containing a single Ogg Vorbis file, when I scan
	 * media, then the database contains the file and its tags.
	 */
	public void testSingleOgg() throws IOException {
		// Given
		Track expected = AssetUtils.copyMusicAsset(testContext, AssetUtils.OGG,
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
		Track expected = AssetUtils.copyMusicAsset(testContext, AssetUtils.OGG,
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
		Track mp3Expected = AssetUtils.copyMusicAsset(testContext,
				AssetUtils.MP3, new File(musicFolder, "sample.mp3"));
		Track oggExpected = AssetUtils.copyMusicAsset(testContext,
				AssetUtils.OGG, new File(musicFolder, "sample.ogg"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("Two files have been found", 2, ids.size());

		Track track = dao.getTrack(ids.get(0));
		if (track.getUri().toString().endsWith(".mp3")) {
			assertEquals("The MP3 file was scanned", mp3Expected, track);
			assertEquals("The Ogg Vorbis file was scanned", oggExpected,
					dao.getTrack(ids.get(1)));
		} else {
			assertEquals("The Ogg Vorbis file was scanned", oggExpected, track);
			assertEquals("The MP3 file was scanned", mp3Expected,
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
		Track expected = AssetUtils.copyMusicAsset(testContext, AssetUtils.OGG,
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

		AssetUtils.copyMusicAsset(testContext, AssetUtils.OGG, new File(
				musicFolder, "sample.ogg"));
		AssetUtils.copyMusicAsset(testContext, AssetUtils.MP3, new File(folder,
				"sample.mp3"));

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

		Track expected = AssetUtils.copyMusicAsset(testContext, AssetUtils.OGG,
				new File(musicFolder, "sample.ogg"));
		AssetUtils.copyMusicAsset(testContext, AssetUtils.MP3, new File(folder,
				"sample.mp3"));

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned", expected, track);
	}

}
