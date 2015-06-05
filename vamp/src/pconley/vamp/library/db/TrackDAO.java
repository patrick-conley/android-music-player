package pconley.vamp.library.db;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.library.db.LibrarySchema.TagEntry;
import pconley.vamp.library.db.LibrarySchema.TrackEntry;
import pconley.vamp.library.db.LibrarySchema.TrackTagRelation;
import pconley.vamp.library.model.Tag;
import pconley.vamp.library.model.Track;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

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

	private static final String GET_TRACKS_QUERY = String
			.format("SELECT * FROM (SELECT * FROM (SELECT %s AS %s, %s FROM %s) LEFT OUTER JOIN %s USING (%s)) LEFT OUTER JOIN %s ON %s = %s ORDER BY %s",
					TrackEntry.COLUMN_ID, TrackTagRelation.TRACK_ID,
					TrackEntry.COLUMN_URI, TrackEntry.NAME,
					TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
					TagEntry.NAME, TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID,
					TrackTagRelation.TRACK_ID);

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
	 * Load every tag for a single track.
	 *
	 * @param trackId
	 * @return Track with the given ID; null if no such track exists.
	 */
	public Track getTrack(long trackId) {

		Cursor results = library.rawQuery(GET_TRACK_QUERY,
				new String[] { String.valueOf(trackId) });

		if (results.getCount() == 0) {
			results.close();
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

	public List<Track> getTracks() {
		List<Track> tracks = new LinkedList<Track>();

		Cursor results = library.rawQuery(GET_TRACKS_QUERY, null);

		int trackIdColumn = results
				.getColumnIndexOrThrow(TrackTagRelation.TRACK_ID);
		int uriColumn = results.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);
		int tagIdColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int nameColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valueColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		long id = -1;
		Track.Builder builder = null;

		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			long trackId = results.getLong(trackIdColumn);

			// Check if this row is part of a new track: add the current track
			// to the list and begin a new track.
			if (id != trackId) {
				if (builder != null) {
					tracks.add(builder.build());
				}

				builder = new Track.Builder(trackId, Uri.parse(results
						.getString(uriColumn)));
				id = trackId;
			}

			if (!results.isNull(nameColumn)) {
				builder.add(new Tag(results.getLong(tagIdColumn), results
						.getString(nameColumn), results.getString(valueColumn)));
			}
		}

		if (builder != null) {
			tracks.add(builder.build());
		}

		results.close();
		return tracks;
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
	 * @param name
	 *            Name of a tag ("title", "composer", etc.)
	 * @param value
	 *            Value of the tag
	 * @throws SQLException
	 *             If the track ID is invalid
	 * @throws NullPointerException
	 *             If either the name or value is missing
	 */
	public void insertTag(long trackId, String name, String value)
			throws SQLException, NullPointerException {

		long tagId = -1;

		if (name == null) {
			Log.w("TrackDAO", "Tag for " + value + " is null");
			throw new NullPointerException("Missing tag");
		} else if (value == null) {
			Log.w("TrackDAO", name + " is null");
			throw new NullPointerException("Missing value for tag " + name);
		}

		// Check whether the tag exists already
		Cursor results = library.query(TagEntry.NAME,
				new String[] { TagEntry.COLUMN_ID }, String.format(
						"%s = ? AND %s = ?", TagEntry.COLUMN_TAG,
						TagEntry.COLUMN_VAL), new String[] { name, value },
				null, null, null);

		if (results.getCount() > 0) {
			results.moveToFirst();
			tagId = results.getLong(results.getColumnIndex(TagEntry.COLUMN_ID));
		}

		results.close();

		library.beginTransaction();
		try {
			ContentValues values;

			// Insert the tag
			if (tagId == -1) {
				values = new ContentValues();
				values.put(TagEntry.COLUMN_TAG, name);
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
