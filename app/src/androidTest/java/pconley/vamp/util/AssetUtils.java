package pconley.vamp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.preferences.SettingsHelper;

public final class AssetUtils {

	/**
	 * Sample Ogg Vorbis file with several comments: see {@link
	 * #buildTrack(File)}
	 */
	public static final String OGG = "sample.ogg";

	/**
	 * Sample FLAC file with several comments.
	 */
	public static final String FLAC = "sample.flac";

	/**
	 * Sample MP4 file with several comments.
	 */
	public static final String MP4 = "sample.m4a";

	/**
	 * Sample MP3 file with several comments
	 */
	public static final String MP3 = "sample.mp3";

	/*
	 * Vendor string. Brittle, but unavoidable.
	 */
	private static final String OGG_VENDOR
			= "Xiph.Org libVorbis I 20101101 (Schaufenugget)";
	private static final String FLAC_VENDOR
			= "reference libFLAC 1.3.0 20130526";

	/**
	 * Tests using Robolectric must prefix their assets with this string, as it
	 * uses the app's manifest (by configuration).
	 */
	public static final String ROBO_ASSET_PATH = "../../androidTest/assets/";

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
	 * 		Context to use for the app's preferences and cache directory. Use a
	 * 		RenamingDelegatingContext.
	 * @return The newly-created folder
	 * @throws IOException
	 * 		If an existing folder could not be deleted.
	 */
	public static File setupMusicFolder(Context context) throws IOException {

		File musicFolder = new File(context.getCacheDir(), "music");
		FileUtils.deleteDirectory(musicFolder);
		musicFolder.mkdir();

		SharedPreferences preferences = context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferences.edit()
		           .putString(SettingsHelper.KEY_MUSIC_FOLDER,
		                      musicFolder.getAbsolutePath())
		           .apply();

		SettingsHelper.setPreferences(preferences);

		return musicFolder;
	}

	/**
	 * Copy an asset file from the .apk into the filesystem.
	 *
	 * @param context
	 * 		The test's context. Unless the test is built around {@link
	 * 		InstrumentationTestCase}, then getContext() will not return the test's
	 * 		context. You can get this context introspectively: {@code (Context)
	 * 		getClass().getMethod("getTestContext").invoke(this)} .
	 * @param asset
	 * 		The asset to copy
	 * @param destination
	 * 		File to copy to.
	 * @return A Track corresponding to the destination file.
	 * @throws IOException
	 * 		In case of any file read/write errors.
	 */
	public static Track addAssetToFolder(Context context, String asset,
			File destination) throws IOException {
		destination.createNewFile();

		InputStream inStream = context.getAssets().open(asset);
		OutputStream outStream = new FileOutputStream(destination);

		byte[] buffer = new byte[1024];
		int length;

		while ((length = inStream.read(buffer)) > 0) {
			outStream.write(buffer, 0, length);
		}

		outStream.flush();
		inStream.close();
		outStream.close();

		return buildTrack(destination);
	}

	/**
	 * Add a track to the database with metadata matching the sample assets. The
	 * tracks added don't need to exist on disk.
	 *
	 * @param context
	 * 		The test's context
	 * @param dest
	 * 		The path of the file to add.
	 * @return ID of the new track in the database.
	 */
	public static long addTrackToDb(Context context, File dest) {
		return addTracksToDb(context, new File[] { dest })[0];
	}

	/**
	 * Add several tracks to the database.
	 *
	 * @param context
	 * 		The test's context
	 * @param files
	 * 		The path of each file to add.
	 * @return IDs of the new tracks in the database.
	 */
	public static long[] addTracksToDb(Context context, File[] files) {
		long[] ids = new long[files.length];

		TrackDAO dao = new TrackDAO(context);
		dao.openWritableDatabase();

		for (int i = 0; i < files.length; i++) {
			Track track = buildTrack(files[i]);

			ids[i] = dao.insertTrack(track.getUri());

			for (String name : track.getTagNames()) {
				for (Tag tag : track.getTags(name)) {
					dao.insertTag(ids[i], tag);
				}
			}
		}

		dao.close();

		return ids;
	}

	public static Track buildTrack(File path) {
		Track.Builder builder = new Track.Builder(0, Uri.fromFile(path))
				.add(new Tag("album", "MyAlbum"))
				.add(new Tag("artist", "MyArtist"))
				.add(new Tag("composer", "MyComposer"))
				.add(new Tag("genre", "Silence"))
				.add(new Tag("title", "MyTitle"))
				.add(new Tag("tracknumber", "3"))
				.add(new Tag("discnumber", "1"));

		String extension = path.toString().substring(
				path.toString().lastIndexOf('.'));

		if (extension.equals(".ogg") || extension.equals(".flac")) {
			builder.add(new Tag("conductor", "MyConductor"))
			       .add(new Tag("comments", "MyComment"))
			       .add(new Tag("uniquetag", "MyUniqueTag"));

			if (extension.equals(".ogg")) {
				builder.add(new Tag("vendor", OGG_VENDOR));
			} else {
				builder.add(new Tag("vendor", FLAC_VENDOR));
			}
		} else if (extension.equals(".mp3")) {
			builder.add(new Tag("tracktotal", "1"));
		}
		return builder.build();
	}
}
