package pconley.vamp.scanner;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.scanner.strategy.GenericTagStrategy;
import pconley.vamp.scanner.strategy.Mp4TagStrategy;
import pconley.vamp.scanner.strategy.TagStrategy;
import pconley.vamp.scanner.strategy.VorbisCommentTagStrategy;
import pconley.vamp.util.BroadcastConstants;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
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

	private int progress = 0;
	private int total = 0;

	private Context context;
	private File musicFolder;

	private TrackDAO dao;
	private LocalBroadcastManager broadcastManager;

	private TagStrategy defaultVorbisStrategy;
	private TagStrategy defaultGenericStrategy;
	private TagStrategy defaultMp4Strategy;

	private MediaMetadataRetriever metadataRetriever;

	public FilesystemScanner(Context context, File musicFolder) {
		this.context = context;
		this.musicFolder = musicFolder;

		dao = new TrackDAO(context);
		broadcastManager = LocalBroadcastManager.getInstance(context);
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

		try {
			scanDir(musicFolder, false);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());

			Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
			intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
			intent.putExtra(BroadcastConstants.EXTRA_MESSAGE,
					context.getString(R.string.scan_error_db, e.getMessage()));
			broadcastManager.sendBroadcast(intent);
		}

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
	private void scanDir(File path, boolean countOnly) throws SQLException {

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

	private void scanFile(File file) throws SQLException {
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

		TagStrategy strategy;

		// Identify the strategy to use
		String extension = file.toString().substring(
				file.toString().lastIndexOf('.'));
		switch (extension) {
		case ".ogg":
		case ".mkv":
		case ".flac":
			strategy = getDefaultVorbisCommentTagStrategy();
			break;
		case ".mp4":
		case ".m4a":
			strategy = getDefaultMp4TagStrategy();
			break;
		default:
			strategy = getDefaultGenericTagStrategy();
			break;
		}

		// Read tags
		Map<String, List<String>> tags = null;
		try {
			tags = strategy.getTags(file);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}

		if (tags == null) {
			return;
		}

		// Write the track and tags
		long trackId = dao.insertTrack(Uri.fromFile(file));

		// Insert tags; abandon the track if any is invalid
		// TODO: mark the track somehow in the DB
		try {
			for (Entry<String, List<String>> tag : tags.entrySet()) {
				for (String value : tag.getValue()) {
					dao.insertTag(trackId, tag.getKey(), value);
				}
			}
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());

			intent = new Intent(BroadcastConstants.FILTER_SCANNER);
			intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
			intent.putExtra(
					BroadcastConstants.EXTRA_MESSAGE,
					context.getString(R.string.scan_error_invalid_tag,
							file.toString()));
			broadcastManager.sendBroadcast(intent);

		}
	}

	private TagStrategy getDefaultVorbisCommentTagStrategy() {
		if (defaultVorbisStrategy == null) {
			defaultVorbisStrategy = new VorbisCommentTagStrategy();
		}

		return defaultVorbisStrategy;
	}

	private TagStrategy getDefaultGenericTagStrategy() {
		if (defaultGenericStrategy == null) {
			defaultGenericStrategy = new GenericTagStrategy(metadataRetriever);
		}

		return defaultGenericStrategy;
	}

	private TagStrategy getDefaultMp4TagStrategy() {
		if (defaultMp4Strategy == null) {
			defaultMp4Strategy = new Mp4TagStrategy();
		}

		return defaultMp4Strategy;
	}

}
