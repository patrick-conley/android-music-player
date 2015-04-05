package pconley.vamp.scanner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pconley.vamp.db.TrackDAO;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.FileUtils;
import wseemann.media.FFmpegMediaMetadataRetriever;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

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
	private Context context;

	// I use this instead of MediaMetadataRetriever as that class doesn't return
	// any tags for the MP3 files I've tested against.
	private FFmpegMediaMetadataRetriever metadataRetriever;

	private Map<String, String> metadataKeys;

	public FilesystemScanner(Context context) {
		this.context = context;

		settings = new SettingsHelper(context);
		dao = new TrackDAO(context);

		// Define a mapping between a minimal set of metadata keys and
		// appropriate names.
		metadataKeys = new HashMap<String, String>();
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM,
				"album");
		metadataKeys.put(
				FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM_ARTIST,
				"albumartist");
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST,
				"artist");
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_GENRE,
				"genre");
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK,
				"tracknumber");
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_COMPOSER,
				"composer");
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_DISC,
				"discnumber");
		metadataKeys.put(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE,
				"title");
	}

	/**
	 * Scan for music files, starting at the directory given by the Music Folder
	 * preference item. Abort (displaying a warning if the app is in the
	 * foreground) if the Music Folder isn't a readable directory.
	 * 
	 * Don't call this from the UI thread.
	 */
	public void scanMediaFolder() {
		dao.openWritableDatabase();
		metadataRetriever = new FFmpegMediaMetadataRetriever();

		scanDir(new File(settings.getMusicFolder()));

		metadataRetriever.release();
		dao.close();
	}

	// FIXME: directory loops will cause an infinite recursion
	private void scanDir(File path) {

		// Check the directory is readable
		if (!FileUtils.validateDirectory(path, context.getApplicationContext())) {
			Log.w(TAG, "Media directory is invalid");
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
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "Skipping file (not media)");
			return; // The file isn't media.
		}

		trackId = dao.insertTrack(Uri.fromFile(file));

		// Read and store data for each key
		for (String key : metadataKeys.keySet()) {
			String metadata = metadataRetriever.extractMetadata(key);
			if (metadata != null) {
				dao.insertTag(trackId, metadataKeys.get(key), metadata);
			}
		}

	}
}
