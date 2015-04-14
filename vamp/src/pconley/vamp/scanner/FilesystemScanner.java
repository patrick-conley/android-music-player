package pconley.vamp.scanner;

import java.io.File;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.preferences.SettingsHelper;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

/**
 * Scan for media files in the Media Folder defined in the app's preferences;
 * add conventional metadata from these files to the app database. A ".nomedia"
 * file is respected.
 * 
 * @author pconley
 *
 */
public class FilesystemScanner {

	private static final String TAG = "FilesystemScanner";

	private SettingsHelper settings;
	private TrackDAO dao;

	// I use this instead of MediaMetadataRetriever as that class doesn't return
	// any tags for the MP3 files I've tested against.
	private MediaMetadataRetriever metadataRetriever;

	private SparseArray<String> metadataKeys;

	public FilesystemScanner(Context context) {
		settings = new SettingsHelper(context);
		dao = new TrackDAO(context);

		// Define a mapping between a minimal set of metadata keys and
		// appropriate names.
		metadataKeys = new SparseArray<String>();
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_ALBUM, "album");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
				"albumartist");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_ARTIST, "artist");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_GENRE, "genre");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER,
				"tracknumber");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER,
				"discnumber");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_COMPOSER,
				"composer");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_TITLE, "title");
	}

	/**
	 * Scan for music files, starting at the directory given by the Music Folder
	 * preference item. Abort (displaying a warning if the app is in the
	 * foreground) if the Music Folder isn't a readable directory. If the
	 * database already contains tracks, then those will first be deleted.
	 * 
	 * Don't call this from the UI thread.
	 */
	public void scanMediaFolder() {
		dao.openWritableDatabase();
		dao.wipeDatabase();

		metadataRetriever = new MediaMetadataRetriever();

		scanDir(new File(settings.getMusicFolder()));

		metadataRetriever.release();
		dao.close();
	}

	// FIXME: directory loops will cause an infinite recursion
	private void scanDir(File path) {

		// Check the directory is readable
		if (!(path.exists() && path.isDirectory() && path.canExecute())) {
			Log.w(TAG, "Directory is invalid");
			return;
		}

		Log.d(TAG, "Scanning directory " + path.toString());

		// Check the directory allows media scanning
		if (new File(path, ".nomedia").exists()
				|| new File(path, ".NOMEDIA").exists()) {
			Log.d(TAG, "Skipping directory (.nomedia)");
			return;
		}

		File[] contents = path.listFiles();
		for (File file : contents) {
			if (file.isDirectory()) {
				scanDir(file);
			} else {
				scanFile(file);
			}
		}
	}

	private void scanFile(File file) {
		long trackId;

		if (!file.canRead()) {
			return;
		}

		Log.v(TAG, "Scanning file " + file.toString());

		// Scan the file. Identifying the MIME type is a bit finnicky, so let
		// the retriever determine what it can read.
		try {
			metadataRetriever.setDataSource(file.getAbsolutePath());
		} catch (RuntimeException e) {
			if (e.getMessage().endsWith("0xFFFFFFEA")) {
				Log.v(TAG, "Skipping file (not media)");
				return;
			} else {
				throw e;
			}
		}

		trackId = dao.insertTrack(Uri.fromFile(file));

		// Read and store data for each key
		for (int i = 0; i < metadataKeys.size(); i++) {
			String metadata = metadataRetriever.extractMetadata(metadataKeys
					.keyAt(i));
			if (metadata != null) {
				dao.insertTag(trackId, metadataKeys.valueAt(i), metadata);
			}
		}

	}
}
