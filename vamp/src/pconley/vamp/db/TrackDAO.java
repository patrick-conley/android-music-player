package pconley.vamp.db;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.model.Tag;
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
		library = new LibraryHelper(context).getReadableDatabase();
	}

	public List<Long> getIds() {
		Cursor results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_ID }, null, null, null, null,
				null);

		List<Long> tracks = new LinkedList<Long>();
		int idColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_ID);

		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			tracks.add(results.getLong(idColumn));
		}

		return tracks;
	}

	/**
	 * Load every tag for a single track.
	 *
	 * @param trackId
	 */
	public Track getTrack(long trackId) {

		// Get the track itself from the library
		Cursor results = library.query(TrackEntry.NAME, new String[] {
				TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI },
				String.format("%s = %d", TrackEntry.COLUMN_ID, trackId), null,
				null, null, null);

		int trackIdColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_ID);
		int uriColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);

		if (results.getCount() != 1) {
			if (results.getCount() == 0) {
				return null;
			} else {
				throw new IllegalArgumentException("Database returned more than one track");
			}
		}

		results.moveToFirst();

		if (results.getLong(trackIdColumn) != trackId) {
			throw new IllegalStateException(String.format("Database returned incorrect track %d. %d expected.",
						results.getLong(trackIdColumn), trackId));
		}

		Track.Builder builder = new Track.Builder(trackId,
				results.getString(uriColumn));

		results.close();

		final String query = String
				.format("SELECT * FROM (SELECT %s AS %s FROM %s WHERE %s = ?) INNER JOIN %s USING (%s) ORDER BY %s,%s",
						TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID,
						TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
						TagEntry.NAME, TagEntry.COLUMN_ID, TagEntry.COLUMN_ID,
						TagEntry.COLUMN_TAG);

		results = library.rawQuery(query,
				new String[] { String.valueOf(trackId) });
		int tagIdColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int nameColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valueColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			builder.add(new Tag(results.getLong(tagIdColumn), results
					.getString(nameColumn), results.getString(valueColumn)));
		}

		results.close();

		return builder.build();
	}

}
