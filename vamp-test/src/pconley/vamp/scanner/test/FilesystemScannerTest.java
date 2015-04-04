package pconley.vamp.scanner.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.db.LibraryOpenHelper;
import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.scanner.FilesystemScanner;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

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

	private File mediaFolder;
	private FilesystemScanner scanner;
	private TrackDAO dao;
	private SQLiteDatabase library;

	// Note: these have faked extensions because the APK recompresses media
	// files.
	private static final String MP3 = "sample.mp3_";
	private static final String OGG = "sample.ogg_";

	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getInstrumentation()
				.getTargetContext(), Constants.DB_PREFIX);

		// Make a folder for music
		mediaFolder = new File(context.getCacheDir(), "media");
		FileUtils.deleteDirectory(mediaFolder);
		mediaFolder.mkdir();

		SharedPreferences preferences = context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferences
				.edit()
				.putString(SettingsHelper.KEY_MUSIC_FOLDER,
						mediaFolder.getAbsolutePath()).commit();

		SettingsHelper.setPreferences(preferences);

		scanner = new FilesystemScanner(context);
		dao = new TrackDAO(context).openReadableDatabase();
		library = new LibraryOpenHelper(context).getWritableDatabase();
	}

	public void tearDown() throws Exception {
		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
		library.close();

		dao.close();
		FileUtils.deleteDirectory(mediaFolder);

		super.tearDown();
	}

	/**
	 * Given an empty media directory, when I try to scan media, then the
	 * database is unmodified
	 */
	public void testEmptyDirectory() {
		// When
		scanner.scanMediaFolder();

		// Then
		assertEquals("No files are found in an empty directory", 0, dao
				.getIds().size());
	}

	/**
	 * Given a media directory containing a single non-media file, when I try to
	 * scan media, then the database is unmodified.
	 */
	public void testSingleNonMediaFile() throws IOException {
		// Given
		File.createTempFile("sample", null, mediaFolder);

		// When
		scanner.scanMediaFolder();

		// Then
		assertEquals("No files are found in a directory with no media files",
				0, dao.getIds().size());

	}

	/**
	 * Given a media directory containing a single MP3 file, when I try to scan
	 * media, then the database contains the file and its tags.
	 */
	public void testSingleMP3() throws IOException {
		// Given
		File destination = new File(mediaFolder, "sample.mp3");
		destination.delete();
		copyAsset(MP3, destination);

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The MP3 file was scanned", buildExpected(destination),
				track);

	}

	/**
	 * Given a media directory containing a single Ogg Vorbis file, when I try
	 * to scan media, then the database contains the file and its tags.
	 */
	public void testSingleOgg() throws IOException {
		// Given
		File destination = new File(mediaFolder, "sample.ogg");
		copyAsset(OGG, destination);

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned",
				buildExpected(destination), track);
	}

	/**
	 * Given a media directory containing a media and a non-media file, when I
	 * try to scan media, then the database contains the media file and its
	 * tags.
	 */
	public void testMixedFiles() throws IOException {
		// Given
		File.createTempFile("sample", null, mediaFolder);
		File destination = new File(mediaFolder, "sample.ogg");
		copyAsset(OGG, destination);

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned",
				buildExpected(destination), track);
	}

	/**
	 * Given a media directory containing two media files, when I try to scan
	 * media, then the database contains both files and their tags.
	 */
	public void testTwoFiles() throws IOException {
		// Given
		File mp3Dest = new File(mediaFolder, "sample.mp3");
		copyAsset(MP3, mp3Dest);
		File oggDest = new File(mediaFolder, "sample.ogg");
		copyAsset(OGG, oggDest);

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 2, ids.size());

		Track track = dao.getTrack(ids.get(0));
		if (track.getUri().toString().endsWith(".mp3")) {
			assertEquals("The MP3 file was scanned", buildExpected(mp3Dest),
					track);
			assertEquals("The Ogg Vorbis file was scanned",
					buildExpected(oggDest), dao.getTrack(ids.get(1)));
		} else {
			assertEquals("The Ogg Vorbis file was scanned",
					buildExpected(oggDest), track);
			assertEquals("The MP3 file was scanned", buildExpected(mp3Dest),
					dao.getTrack(ids.get(1)));
		}
	}

	/**
	 * Given a media directory containing a single, empty directory, when I try
	 * to scan media, then the database is unmodified.
	 */
	public void testEmptyChildDirectory() {
		// Given
		new File(mediaFolder, "sample").mkdir();

		// When
		scanner.scanMediaFolder();

		// Then
		assertEquals("No files are found in an empty directory", 0, dao
				.getIds().size());
	}

	/**
	 * Given a media directory containing a single media file inside a
	 * directory, when I try to scan media, then the database contains the file
	 * and its tags.
	 */
	public void testSingleFileInChildDirectory() throws IOException {
		// Given
		File folder = new File(mediaFolder, "sample");
		folder.mkdir();
		File destination = new File(folder, "sample.ogg");
		copyAsset(OGG, destination);

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned",
				buildExpected(destination), track);
	}

	/**
	 * Given a media directory containing a media file and .nomedia file, and a
	 * child directory containing another media file, when I try to scan media,
	 * then the database is unmodified.
	 */
	public void testNoMedia() throws IOException {
		// Given
		File folder = new File(mediaFolder, "sample");
		folder.mkdir();

		new File(mediaFolder, ".nomedia").createNewFile();

		File oggDest = new File(mediaFolder, "sample.ogg");
		copyAsset(OGG, oggDest);
		File mp3Dest = new File(folder, "sample.mp3");
		copyAsset(MP3, mp3Dest);

		// When
		scanner.scanMediaFolder();

		// Then
		assertEquals("No files are found in an empty directory", 0, dao
				.getIds().size());
	}

	/**
	 * Given a media directory containing a media file, and a child directory
	 * containing another media file and a .nomedia file, when I try to scan
	 * media, then the database contains the first file and its tags.
	 */
	public void testNoMediaInChild() throws IOException {
		// Given
		File folder = new File(mediaFolder, "sample");
		folder.mkdir();

		new File(folder, ".NOMEDIA").createNewFile();

		File oggDest = new File(mediaFolder, "sample.ogg");
		copyAsset(OGG, oggDest);
		File mp3Dest = new File(folder, "sample.mp3");
		copyAsset(MP3, mp3Dest);

		// When
		scanner.scanMediaFolder();

		// Then
		List<Long> ids = dao.getIds();
		assertEquals("One file has been found", 1, ids.size());

		Track track = dao.getTrack(ids.get(0));
		assertEquals("The Ogg Vorbis file was scanned",
				buildExpected(oggDest), track);
	}

	/**
	 * Build a Track with URI of the given File and with hard-coded tags
	 * corresponding to the included sample files.
	 * 
	 * @param location
	 * @return
	 */
	private Track buildExpected(File location) {
		return new Track.Builder(0, Uri.fromFile(location))
				.add(new Tag(0, "album", "MyAlbum"))
				.add(new Tag(0, "artist", "MyArtist"))
				.add(new Tag(0, "composer", "MyComposer"))
				.add(new Tag(0, "genre", "Silence"))
				.add(new Tag(0, "title", "MyTitle"))
				.add(new Tag(0, "tracknumber", "03")).build();
	}

	/**
	 * Copy an asset file from the .apk into the filesystem.
	 * 
	 * @param asset
	 * @param destination
	 * @throws IOException
	 */
	private void copyAsset(String asset, File destination) throws IOException {
		destination.createNewFile();

		InputStream inStream = getInstrumentation().getContext().getAssets()
				.open(asset);
		OutputStream outStream = new FileOutputStream(destination);

		byte[] buffer = new byte[1024];
		int length;

		while ((length = inStream.read(buffer)) > 0) {
			outStream.write(buffer, 0, length);
		}

		outStream.flush();
		inStream.close();
		outStream.close();
	}
}
