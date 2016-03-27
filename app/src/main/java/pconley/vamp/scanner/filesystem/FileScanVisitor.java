package pconley.vamp.scanner.filesystem;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.scanner.filesystem.model.MediaFile;
import pconley.vamp.scanner.filesystem.model.MediaFolder;
import pconley.vamp.scanner.ScannerEvent;
import pconley.vamp.scanner.strategy.TagStrategyLocator;
import pconley.vamp.scanner.strategy.TagStrategy;
import pconley.vamp.util.BroadcastConstants;

/**
 * Visit part of a filesystem, scanning its files for audio metadata.
 *
 * @author pconley
 */
public class FileScanVisitor implements FileVisitor {
	private static final String TAG = "FilesystemScanner";

	private int progress = 0;

	private final String musicFolderName;
	private final Context context;

	private TrackDAO dao;
	private LocalBroadcastManager broadcastManager;

	private Intent dirIntent;
	private Intent fileIntent;
	private Intent errorIntent;

	/**
	 * Create an instance of the visitor.
	 *
	 * @param musicRoot
	 * 		Path to the system's music folder.
	 * @param context
	 * 		The context running the visitor.
	 * @param count
	 * 		Number of files expected. (The number needs to be resent periodically
	 * 		in case the fragment was destroyed & restarted.)
	 */
	public FileScanVisitor(File musicRoot, Context context, int count) {
		this.musicFolderName = musicRoot.toString();
		this.context = context;

		this.broadcastManager = LocalBroadcastManager.getInstance(context);
		this.dao = new TrackDAO(new LibraryOpenHelper(context));

		dirIntent = new Intent(BroadcastConstants.FILTER_SCANNER);
		dirIntent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent
				.UPDATE);
		dirIntent.putExtra(BroadcastConstants.EXTRA_TOTAL, count);

		fileIntent = new Intent(BroadcastConstants.FILTER_SCANNER);
		fileIntent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent
				.UPDATE);

		errorIntent = new Intent(BroadcastConstants.FILTER_SCANNER);
		errorIntent
				.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.ERROR);
	}

	/**
	 * Close the database connection and release native resources. Call this
	 * when finished.
	 */
	public void close() {
		new LibraryOpenHelper(context).close();

		TagStrategyLocator.release();
	}

	/**
	 * Broadcast the relative filename of the folder being scanned.
	 */
	@Override
	public void visit(MediaFolder dir) {

		// Get the directory's name, relative to the music folder
		String relativeName = dir.toString().replace(musicFolderName, "");
		// Remove the leading slash
		if (relativeName.length() > 0) {
			relativeName = relativeName.substring(1);
		}

		// Broadcast folder information.
		dirIntent.putExtra(BroadcastConstants.EXTRA_MESSAGE, relativeName);
		broadcastManager.sendBroadcast(dirIntent);
	}

	/**
	 * Scan the file for audio metadata; write the file and metadata to the
	 * library. Broadcast the number of files scanned so far, and any errors
	 * that occur. If the file isn't audio, return without writing anything.
	 */
	@Override
	public void visit(MediaFile file) {
		progress++;

		// Update the count
		Log.v(TAG, "Scanning file " + file.toString());
		fileIntent.putExtra(BroadcastConstants.EXTRA_PROGRESS, progress);
		broadcastManager.sendBroadcast(fileIntent);

		TagStrategy strategy = TagStrategyLocator.getStrategy(file);

		// Read tags
		List<Tag> tags;
		try {
			tags = strategy.getTags(file.getFile());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}

		if (tags == null) {
			return;
		}

		Uri uri = Uri.fromFile(file.getFile());

		Track.Builder builder = new Track.Builder(-1, uri);
		for (Tag tag : tags) {
			builder.add(tag);
		}

		try {
			dao.insertTrack(builder.build());
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());

			errorIntent.putExtra(
					BroadcastConstants.EXTRA_MESSAGE,
					context.getString(R.string.scan_warning_invalid_tag,
					                  file.toString()));
			broadcastManager.sendBroadcast(errorIntent);

		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());

			errorIntent.putExtra(
					BroadcastConstants.EXTRA_MESSAGE,
					context.getString(R.string.scan_warning_duplicate,
					                  file.toString()));
			broadcastManager.sendBroadcast(errorIntent);
		}
	}

}
