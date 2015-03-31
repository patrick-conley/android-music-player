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
import android.net.Uri;

public class TrackDAO {

	private SQLiteDatabase library;

	private static final String GET_TRACK_QUERY = String
			.format("SELECT * FROM (SELECT * FROM (SELECT %s AS %s, %s FROM %s WHERE %s = ?) LEFT OUTER JOIN %s USING (%s)) LEFT OUTER JOIN %s ON %s = %s",
					TrackEntry.COLUMN_ID, TrackTagRelation.TRACK_ID,
					TrackEntry.COLUMN_URI, TrackEntry.NAME,
					TrackEntry.COLUMN_ID, TrackTagRelation.NAME,
					TrackTagRelation.TRACK_ID, TagEntry.NAME,
					TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID);

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
	 * Close this DAO's reference to the database.
	 */
	public void close() {
		library.close();
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

		results.close();

		return tracks;
	}

	/**
	 * Load every tag for a single track.
	 *
	 * @param trackId
	 * @return Track with the given ID; null if no such track exists.
	 */
	public Track getTrack(long trackId) {

		Cursor results = library.rawQuery(GET_TRACK_QUERY,
				new String[] { String.valueOf(trackId) });

		if (results.getCount() == 0) {
			return null;
		}

		int uriColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);
		int tagIdColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int nameColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valueColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		results.moveToFirst();

		Track.Builder builder = new Track.Builder(trackId, Uri.parse(results
				.getString(uriColumn)));

		// Add tags to the track, provided there is at least one.
		if (!results.isNull(tagIdColumn)) {
			for (; !results.isAfterLast(); results.moveToNext()) {
				builder.add(new Tag(results.getLong(tagIdColumn), results
						.getString(nameColumn), results.getString(valueColumn)));
			}
		}

		results.close();

		return builder.build();
	}

}
