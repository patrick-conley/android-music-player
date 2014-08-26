package pconley.vamp.db;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.model.Track;
import pconley.vamp.source.TrackSource;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TrackDAO implements TrackSource {

	private SQLiteDatabase library;

	/**
	 * Open a database connection. Should not be called from the UI thread.
	 *
	 * @param context
	 *            Activity context
	 */
	public TrackDAO(Context context) {
		library = new LibraryHelper(context).getReadableDatabase();
	}

	/**
	 * Get a list of tracks.
	 *
	 * @return Every track in the library, without the URI or any tags
	 */
	public List<Track> getAll() {
		Cursor results = library.query(TrackEntry.NAME, new String[] {
				TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI }, null, null,
				null, null, null);

		List<Track> tracks = new LinkedList<Track>();
		int idColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_ID);
		int uriColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);

		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			tracks.add(new Track(results.getInt(idColumn), results
					.getString(uriColumn)));
		}

		return tracks;
	}

	@Override
	public void insert() {
		// TODO Auto-generated method stub
	}

}
