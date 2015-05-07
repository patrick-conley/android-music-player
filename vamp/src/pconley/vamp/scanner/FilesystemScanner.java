package pconley.vamp.scanner;

import java.io.File;

import pconley.vamp.library.db.TrackDAO;
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

	private int progress = 0;
	private int total = 0;

	private File musicFolder;
	private TrackDAO dao;
	private LocalBroadcastManager broadcastManager;

	private MediaMetadataRetriever metadataRetriever;

	private SparseArray<String> metadataKeys;

	public FilesystemScanner(Context context, File musicFolder) {
		this.musicFolder = musicFolder;

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
	 * <p>
	 * Sends a broadcast each time it enters a folder with the current folder
	 * (relative to the Music Folder) name and number of tracks scanned.
	 * 
	 * <p>
	 * Don't call this from the UI thread. The metadata retriever can only scan
	 * 5-10 files per second.
	 */
	public void scanMusicFolder() {
		Log.i(TAG, "Scanning for music");

		metadataRetriever = new MediaMetadataRetriever();
		dao.openWritableDatabase();
		dao.wipeDatabase();

		scanDir(musicFolder, false);

		dao.close();
		metadataRetriever.release();
	}

	/**
	 * Count the number of files in the music folder and its children.
	 * 
	 * <p>
	 * This can scan ~1000 files per second, which may block the UI on large
	 * libraries.
	 */
	public int countMusicFiles() {
		Log.i(TAG, "Counting music files");

		total = 0;
		scanDir(musicFolder, true);
		progress = 0;

		return total;
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
			String folder = path.toString().replace(musicFolder.toString(), "");
			if (folder.length() > 0) {
				folder = folder.substring(1); // skip the leading slash
			}

			// Broadcast folder information.
			// Total is included in case a listening activity was closed &
			// reopened, and needs that info.
			Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
			intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
			intent.putExtra(BroadcastConstants.EXTRA_TOTAL, total);
			intent.putExtra(BroadcastConstants.EXTRA_MESSAGE, folder);

			broadcastManager.sendBroadcast(intent);
		}

		File[] contents = path.listFiles();
		for (File file : contents) {
			if (file.isDirectory()) {
				scanDir(file, countOnly);
			} else if (countOnly) {
				total++;
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

		// Update the count
		Log.v(TAG, "Scanning file " + file.toString());

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
