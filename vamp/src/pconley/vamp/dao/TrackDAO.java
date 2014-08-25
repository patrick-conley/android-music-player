package pconley.vamp.dao;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryHelper;
import pconley.vamp.model.Track;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TrackDAO {

	private SQLiteDatabase library;

	/**
	 * Open a database connection. Should not be called from the UI thread.
	 *
	 * @param context
	 *            Activity context
	 */
	public TrackDAO(Context context) {
		LibraryHelper dbHelper = new LibraryHelper(context);
		library = dbHelper.getWritableDatabase();
	}

	/**
	 * Get a list of tracks.
	 *
	 * @return Every track in the library, without the URI or any tags
	 */
	public List<Track> getTracks() {
		Cursor results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_ID }, null, null, null, null,
				null);

		List<Track> tracks = new LinkedList<Track>();
		int idColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_ID);

		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			tracks.add(new Track(results.getInt(idColumn)));
		}

		return tracks;
	}

}
