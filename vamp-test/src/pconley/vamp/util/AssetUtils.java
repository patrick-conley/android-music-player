package pconley.vamp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.test.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.test.InstrumentationTestCase;

public final class AssetUtils {

	public static final int OGG = R.raw.sample_ogg;
	public static final int FLAC = R.raw.sample_flac;

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

		return getTrack(destination);
	}

	/**
	 * Add a track to the database which corresponds to one of the sample
	 * assets.
	 * 
	 * @param context
	 *            The test's context
	 * @param dest
	 *            The path of the file to add.
	 * @return ID of the new track in the database.
	 */
	public static long addAssetToDb(Context context, File dest) {
		TrackDAO dao = new TrackDAO(context);
		dao.openWritableDatabase();

		Track track = getTrack(dest);

		long trackId = dao.insertTrack(track.getUri());

		for (String name : track.getTagNames()) {
			for (Tag tag : track.getTags(name)) {
				dao.insertTag(trackId, tag.getName(), tag.getValue());
			}
		}

		return trackId;
	}

	private static Track getTrack(File path) {
		return new Track.Builder(0, Uri.fromFile(path))
				.add(new Tag(0, "album", "MyAlbum"))
				.add(new Tag(0, "artist", "MyArtist"))
				.add(new Tag(0, "composer", "MyComposer"))
				.add(new Tag(0, "genre", "Silence"))
				.add(new Tag(0, "title", "MyTitle"))
				.add(new Tag(0, "tracknumber", "3"))
				.add(new Tag(0, "discnumber", "1")).build();
	}
}
