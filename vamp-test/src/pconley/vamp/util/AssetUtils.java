package pconley.vamp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.db.LibraryOpenHelper;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.test.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public final class AssetUtils {

	// Note: these have faked extensions because the APK recompresses media
	// files.
	public static final int MP3 = R.raw.sample_mp3;
	public static final int OGG = R.raw.sample_ogg;

	// public static final String MP3 = "sample.mp3_";
	// public static final String OGG = "sample.ogg_";

	/**
	 * Private constructor: static members only
	 */
	private AssetUtils() {

	}

	/**
	 * Delete, then recreate, a music folder in the app's cache, and set its
	 * path in the preferences.
	 * 
	 * @param context
	 *            Context to use for the app's preferences and cache directory.
	 *            Use a RenamingDelegatingContext.
	 * @return The newly-created folder
	 * @throws IOException
	 *             If an existing folder could not be deleted.
	 */
	public static File setupMusicFolder(Context context) throws IOException {

		File musicFolder = new File(context.getCacheDir(), "music");
		FileUtils.deleteDirectory(musicFolder);
		musicFolder.mkdir();

		SharedPreferences preferences = context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferences
				.edit()
				.putString(SettingsHelper.KEY_MUSIC_FOLDER,
						musicFolder.getAbsolutePath()).commit();

		SettingsHelper.setPreferences(preferences);

		return musicFolder;
	}

	/**
	 * Copy an asset file from the .apk into the filesystem.
	 * 
	 * @param context
	 *            The test's context. Unless the test is built around
	 *            {@link InstrumentationTestCase}, then getContext() will not
	 *            return the test's context. You can get this context
	 *            introspectively:
	 *            {@code (Context) getClass().getMethod("getTestContext").invoke(this)}
	 *            .
	 * @param asset
	 *            The asset to copy
	 * @param destination
	 *            File to copy to.
	 * @return A Track corresponding to the destination file.
	 * @throws IOException
	 *             In case of any file read/write errors.
	 */
	public static Track copyMusicAsset(Context context, int asset,
			File destination) throws IOException {
		destination.createNewFile();

		InputStream inStream = context.getResources().openRawResource(asset);
		OutputStream outStream = new FileOutputStream(destination);

		byte[] buffer = new byte[1024];
		int length;

		while ((length = inStream.read(buffer)) > 0) {
			outStream.write(buffer, 0, length);
		}

		outStream.flush();
		inStream.close();
		outStream.close();

		return new Track.Builder(0, Uri.fromFile(destination))
				.add(new Tag(0, "album", "MyAlbum"))
				.add(new Tag(0, "artist", "MyArtist"))
				.add(new Tag(0, "composer", "MyComposer"))
				.add(new Tag(0, "genre", "Silence"))
				.add(new Tag(0, "title", "MyTitle"))
				.add(new Tag(0, "tracknumber", "03")).build();
	}

	/**
	 * Delete all tracks and tags from the database.
	 * 
	 * @param context
	 */
	public static void clearDatabase(Context context) {
		SQLiteDatabase library = new LibraryOpenHelper(context)
				.getWritableDatabase();

		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
		library.close();

	}
}
