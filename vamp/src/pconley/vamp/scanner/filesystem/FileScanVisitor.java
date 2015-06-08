package pconley.vamp.scanner.filesystem;

import java.io.File;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.library.model.Tag;
import pconley.vamp.scanner.ScannerEvent;
import pconley.vamp.scanner.container.ScannerFactory;
import pconley.vamp.scanner.container.TagStrategy;
import pconley.vamp.util.BroadcastConstants;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Visit part of a filesystem, scanning its files for audio metadata.
 * 
 * @author pconley
 */
public class FileScanVisitor implements MediaVisitorBase {
	private static final String TAG = "FilesystemScanner";

	private int progress = 0;

	private String musicFolderName;
	private Context context;

	private TrackDAO dao;
	private LocalBroadcastManager broadcastManager;

	private Intent dirIntent;
	private Intent fileIntent;

	public FileScanVisitor(File musicFolder, Context context) {
		this.musicFolderName = musicFolder.toString();
		this.broadcastManager = LocalBroadcastManager.getInstance(context);

		this.dao = new TrackDAO(context);
		dao.openWritableDatabase();

		dirIntent = new Intent(BroadcastConstants.FILTER_SCANNER);
		dirIntent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);

		fileIntent = new Intent(BroadcastConstants.FILTER_SCANNER);
		fileIntent
				.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
	}

	/**
	 * Close the database connection and release native resources. Call this
	 * when finished.
	 */
	public void close() {
		dao.close();

		ScannerFactory.release();
	}

	/**
	 * Broadcast the relative filename of the folder being scanned.
	 */
	@Override
	public void visit(MediaFolder dir) {

		// Get the directory's name, relative to the music folder
		String relativeName = dir.toString()
				.replace(musicFolderName, "");
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

		TagStrategy strategy = ScannerFactory.getStrategy(file);

		// Read tags
		List<Tag> tags = null;
		try {
			tags = strategy.getTags(file.getFile());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}

		if (tags == null) {
			return;
		}

		// Write the track and tags
		long trackId = dao.insertTrack(Uri.fromFile(file.getFile()));

		// Insert tags; abandon the track if any is invalid
		// TODO: mark the track somehow in the DB if it fails
		try {
			for (Tag tag : tags) {
				dao.insertTag(trackId, tag.getName(), tag.getValue());
			}
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());

			Intent intent = new Intent(BroadcastConstants.FILTER_SCANNER);
			intent.putExtra(BroadcastConstants.EXTRA_EVENT, ScannerEvent.UPDATE);
			intent.putExtra(
					BroadcastConstants.EXTRA_MESSAGE,
					context.getString(R.string.scan_error_invalid_tag,
							file.toString()));
			broadcastManager.sendBroadcast(intent);

		}
	}

}
