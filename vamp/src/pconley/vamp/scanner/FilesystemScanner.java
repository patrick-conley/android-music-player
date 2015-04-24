package pconley.vamp.scanner;

import java.io.File;

import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.BroadcastConstants;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
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

	int progress = 0;
	int max = 0;

	private File musicFolder;
	private TrackDAO dao;
	private LocalBroadcastManager broadcastManager;

	private MediaMetadataRetriever metadataRetriever;

	private SparseArray<String> metadataKeys;

	public FilesystemScanner(Context context) {
		musicFolder = new File(new SettingsHelper(context).getMusicFolder());
		dao = new TrackDAO(context);
		broadcastManager = LocalBroadcastManager.getInstance(context);

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
	 * Sends a broadcast each time it enters a folder with the current folder
	 * (relative to the Music Folder) name and number of tracks scanned.
	 * 
	 * Don't call this from the UI thread.
	 */
	public void scanMusicFolder() {
		Log.i(TAG, "Scanning for music");

		dao.openWritableDatabase();
		dao.wipeDatabase();

		metadataRetriever = new MediaMetadataRetriever();

		scanDir(musicFolder, false);

		metadataRetriever.release();
		dao.close();
	}

	/**
	 * Count the number of folders descendent from the Music Folder.
	 * 
	 * Sends a single broadcast when finished to announce the total.
	 */
	public void countFolders() {
		Log.i(TAG, "Counting music files");
		max = 0;
		scanDir(musicFolder, true);
		progress = 0;

		Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
		intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
		intent.putExtra(BroadcastConstants.EXTRA_MAX, max);
		broadcastManager.sendBroadcast(intent);
	}

	// FIXME: directory loops will cause an infinite recursion
	private void scanDir(File path, boolean countOnly) {

		// Check the directory is readable
		if (!path.exists() || !path.isDirectory() || !path.canExecute()) {
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

		if (!countOnly) {
			Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
			intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
			intent.putExtra(BroadcastConstants.EXTRA_PROGRESS, progress);
			intent.putExtra(BroadcastConstants.EXTRA_MESSAGE, path.toString()
					.replace(musicFolder.toString() + "/", ""));

			broadcastManager.sendBroadcast(intent);
		}

		File[] contents = path.listFiles();
		for (File file : contents) {
			if (file.isDirectory()) {
				scanDir(file, countOnly);
			} else if (countOnly) {
				max++;
			} else {
				scanFile(file);
			}
		}
	}

	private void scanFile(File file) {
		long trackId;

		progress++;

		if (!file.canRead()) {
			return;
		}

		Log.v(TAG, "Scanning file " + file.toString());

		// Update the count
		Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
		intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
		intent.putExtra(BroadcastConstants.EXTRA_PROGRESS, progress);
		broadcastManager.sendBroadcast(intent);

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
