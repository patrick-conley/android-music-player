package pconley.vamp.db;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Methods to read and write track data from the database.
 * 
 * @author pconley
 */
public class TrackDAO {

	private LibraryOpenHelper libraryOpenHelper;
	private SQLiteDatabase library;

	private static final String GET_TRACK_QUERY = String
			.format("SELECT * FROM (SELECT * FROM (SELECT %s AS %s, %s FROM %s WHERE %s = ?) LEFT OUTER JOIN %s USING (%s)) LEFT OUTER JOIN %s ON %s = %s",
					TrackEntry.COLUMN_ID, TrackTagRelation.TRACK_ID,
					TrackEntry.COLUMN_URI, TrackEntry.NAME,
					TrackEntry.COLUMN_ID, TrackTagRelation.NAME,
					TrackTagRelation.TRACK_ID, TagEntry.NAME,
					TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID);

	public TrackDAO(Context context) {
		libraryOpenHelper = new LibraryOpenHelper(context);
	}

	/**
	 * Open a database connection, which may or may not be read-only. Do not run
	 * from the UI thread.
	 * 
	 * @return The current TrackDAO object, for method chaining.
	 */
	public TrackDAO openReadableDatabase() {
		library = libraryOpenHelper.getReadableDatabase();

		return this;
	}

	/**
	 * Open a writable database connection. Do not run from the UI thread.
	 * 
	 * @return The current TrackDAO object, for method chaining.
	 * @throws SQLException
	 *             If the database cannot be opened for writing.
	 */
	public TrackDAO openWritableDatabase() throws SQLException {
		library = libraryOpenHelper.getWritableDatabase();

		return this;
	}

	/**
	 * Close this DAO's reference to the database.
	 */
	public void close() {
		if (library != null) {
			library.close();
		}
	}

	/**
	 * @return Every track ID in the database.
	 */
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

	/**
	 * Insert a track. This method is only to be used when populating a database
	 * from scratch: inserting a duplicate track URI is an error.
	 * 
	 * @param uri
	 *            URI of a new track.
	 * @return The inserted track's ID.
	 * @throws SQLException
	 *             If the database already contains this track.
	 */
	public long insertTrack(Uri uri) throws SQLException {

		ContentValues values = new ContentValues();
		values.put(TrackEntry.COLUMN_URI, uri.toString());

		return library.insertOrThrow(TrackEntry.NAME, null, values);
	}

	/**
	 * Insert a tag, and associate it with a track that uses it. If the tag
	 * given by the parameters (tag, value) is a duplicate, then return the
	 * original tag's ID.
	 * 
	 * @param trackId
	 *            ID of a track in the database.
	 * @param tag
	 *            Name of a tag ("title", "composer", etc.)
	 * @param value
	 *            Value of the tag
	 * @throws SQLException
	 *             If the track doesn't exist
	 */
	public void insertTag(long trackId, String tag, String value)
			throws SQLException {

		long tagId = -1;

		// Check whether the tag exists already
		Cursor results = library.query(TagEntry.NAME,
				new String[] { TagEntry.COLUMN_ID }, String.format(
						"%s = ? AND %s = ?", TagEntry.COLUMN_TAG,
						TagEntry.COLUMN_VAL), new String[] { tag, value },
				null, null, null);

		if (results.getCount() > 0) {
			results.moveToFirst();
			tagId = results.getLong(results.getColumnIndex(TagEntry.COLUMN_ID));
		}

		library.beginTransaction();
		try {
			ContentValues values;

			// Insert the tag
			if (tagId == -1) {
				values = new ContentValues();
				values.put(TagEntry.COLUMN_TAG, tag);
				values.put(TagEntry.COLUMN_VAL, value);

				tagId = library.insertOrThrow(TagEntry.NAME, null, values);
			}

			// Relate it to its track
			values = new ContentValues();
			values.put(TrackTagRelation.TRACK_ID, trackId);
			values.put(TrackTagRelation.TAG_ID, tagId);

			library.insertOrThrow(TrackTagRelation.NAME, null, values);

			library.setTransactionSuccessful();
		} finally {
			library.endTransaction();
		}

	}
	
	/**
	 * Remove all tracks, tags, and relations
	 */
	public void wipeDatabase() {
		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
	}

}
