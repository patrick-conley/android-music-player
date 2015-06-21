package pconley.vamp.library.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.library.db.LibrarySchema.TagEntry;
import pconley.vamp.library.db.LibrarySchema.TrackEntry;
import pconley.vamp.library.db.LibrarySchema.TrackTagRelation;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;

/**
 * Methods to read and write track data from the database.
 *
 * @author pconley
 */
public class TrackDAO {
	private static final String TAG = "TrackDAO";

	private LibraryOpenHelper libraryOpenHelper;
	private SQLiteDatabase library;

	// SELECT * FROM (SELECT * FROM
	//     (SELECT _id AS trackId, uri FROM Tracks WHERE _id = ?)
	//   LEFT OUTER JOIN TrackHasTags USING (trackId))
	// LEFT OUTER JOIN Tags ON _id = tagId
	private static final String GET_TRACK_QUERY = MessageFormat
			.format("SELECT * FROM (SELECT * FROM "
			        + "(SELECT {4} AS {1}, {5} FROM {3} WHERE {4} = ?)"
			        + "LEFT OUTER JOIN {0} USING ({1}))"
			        + "LEFT OUTER JOIN {6} ON {7} = {2}",
			        TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
			        TrackTagRelation.TAG_ID, TrackEntry.NAME,
			        TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI, TagEntry.NAME,
			        TagEntry.COLUMN_ID);

	// SELECT * FROM Tracks INNER JOIN
	//   (SELECT * FROM Tags INNER JOIN TrackHasTags ON tagId = _id WHERE trackId IN
	//       (SELECT trackId FROM TrackHasTags WHERE tagId = ?))
	//   ON Tracks._id = trackId ORDER BY (trackId);
	private static final String GET_MATCHING_TRACKS_QUERY = MessageFormat
			.format("SELECT * FROM {1} INNER JOIN" +
			        "(SELECT * FROM {2} INNER JOIN {0} ON {5} = {6} WHERE {3} IN "
			        + "(SELECT {3} FROM {0} WHERE {5} = ?)) "
			        + "ON {1}.{4} = {3} ORDER BY ({3})",
			        TrackTagRelation.NAME, TrackEntry.NAME, TagEntry.NAME,
			        TrackTagRelation.TRACK_ID, TrackEntry.COLUMN_ID,
			        TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID
			       );

	// SELECT * FROM (SELECT * FROM (SELECT _id AS trackId, uri FROM Tracks)
	//   LEFT OUTER JOIN TrackHasTags USING (trackId))
	// LEFT OUTER JOIN Tags ON _id = tagId ORDER BY trackId
	private static final String GET_TRACKS_QUERY = MessageFormat
			.format("SELECT * FROM (SELECT * FROM "
			        + "(SELECT {4} AS {1}, {5} FROM {3})"
			        + " LEFT OUTER JOIN {0} USING ({1}))"
			        + " LEFT OUTER JOIN {6} ON {7} = {2} ORDER BY {1}",
			        TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
			        TrackTagRelation.TAG_ID, TrackEntry.NAME,
			        TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI, TagEntry.NAME,
			        TagEntry.COLUMN_ID);

	// SELECT * FROM Tags WHERE name = ?
	private static final String GET_TAG_QUERY = MessageFormat
			.format("SELECT * FROM {0} WHERE {1} = ?", TagEntry.NAME,
			        TagEntry.COLUMN_TAG);

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
	 * 		If the database cannot be opened for writing.
	 */
	public TrackDAO openWritableDatabase() throws SQLException {
		library = libraryOpenHelper.getWritableDatabase();

		return this;
	}

	/**
	 * Close this DAO's reference to the database.
	 */
	public void close() {
		library.close();
	}

	/**
	 * Load every tag for a single track.
	 *
	 * @param trackId
	 * @return Track with the given ID; null if no such track exists.
	 */
	public Track getTrack(long trackId) {

		Cursor results = library
				.rawQuery(GET_TRACK_QUERY,
				          new String[] { String.valueOf(trackId) });

		Track track = mapTrack(results);

		results.close();

		return track;
	}

	/**
	 * Get every track in the database
	 *
	 * @return list of Tracks
	 */
	public List<Track> getTracks() {
		List<Track> tracks = new LinkedList<Track>();
		Track track;

		Cursor results = library.rawQuery(GET_TRACKS_QUERY, null);
		while ((track = mapTrack(results)) != null) {
			tracks.add(track);
		}

		results.close();

		return tracks;
	}

	/**
	 * Find the tracks in the database which match the given tag
	 * <p/>
	 * TODO: test this once its API stabilizes
	 *
	 * @param tag
	 * 		Tag to filter against
	 * @return Matching tracks.
	 */
	public List<Track> getTracks(Tag tag) {
		List<Track> tracks = new LinkedList<Track>();
		Track track;

		Cursor results = library
				.rawQuery(GET_MATCHING_TRACKS_QUERY,
				          new String[] { String.valueOf(tag.getId()) });

		while ((track = mapTrack(results)) != null) {
			tracks.add(track);
		}

		results.close();

		return tracks;
	}

	/**
	 * Extract a single track from a data cursor. The cursor must be positioned
	 * on the first row of a track (alternatively, before the first row of the
	 * cursor); if the cursor contains rows for multiple tracks, then each
	 * track's rows must be contiguous.
	 * <p/>
	 * When done, the cursor will point at the first row of the next track, if
	 * available.
	 *
	 * @param cursor
	 * @return Track beginning at the cursor's current row, or null if
	 * past-the-end.
	 */
	private Track mapTrack(Cursor cursor) {

		// Initial: move to the first row
		if (cursor.isBeforeFirst()) {
			cursor.moveToFirst();
		}

		// Final: do nothing
		if (cursor.isAfterLast()) {
			return null;
		}

		int trackIdColumn = cursor
				.getColumnIndexOrThrow(TrackTagRelation.TRACK_ID);
		int uriColumn = cursor.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);
		int tagIdColumn = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int nameColumn = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valueColumn = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		long id = cursor.getLong(trackIdColumn);
		Track.Builder builder = new Track.Builder(id, Uri.parse(
				cursor.getString(uriColumn)));

		while (!cursor.isAfterLast() && cursor.getLong(trackIdColumn) == id) {

			// Add this row's tag to the existing track (a track might not
			// have tags)
			if (!cursor.isNull(nameColumn)) {
				builder.add(new Tag(cursor.getLong(tagIdColumn),
				                    cursor.getString(nameColumn),
				                    cursor.getString(valueColumn)));
			}

			cursor.moveToNext();
		}

		return builder.build();
	}

	/**
	 * Get all tags with the given name.
	 * <p/>
	 * TODO: test this once its API stabilizes
	 *
	 * @param name
	 */
	public List<Tag> getTag(String name) {
		List<Tag> tags = new LinkedList<>();

		Cursor results = library.rawQuery(GET_TAG_QUERY, new String[] { name });

		int idColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int valueColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		for (results.moveToFirst(); !results.isAfterLast();
		     results.moveToNext()) {
			tags.add(new Tag(results.getLong(idColumn), name,
			                 results.getString(valueColumn)));
		}

		results.close();

		return tags;
	}

	/**
	 * Insert a track with its tags. This method is only to be used when
	 * populating an empty database. A transaction is used.
	 *
	 * @param uri
	 * @param tags
	 * @throws SQLException
	 * 		If the track is already in the database.
	 * @throws NullPointerException
	 * 		If any input is null.
	 */
	public void insertTrack(Uri uri, Iterable<Tag> tags) throws SQLException,
			NullPointerException {

		library.beginTransaction();
		try {
			long trackId = insertTrack(uri);

			for (Tag tag : tags) {
				insertTag(trackId, tag);
			}
			library.setTransactionSuccessful();
		} finally {
			library.endTransaction();
		}
	}

	/**
	 * Insert a track. This method is only to be used when populating a database
	 * from scratch: inserting a duplicate track URI is an error.
	 *
	 * @param uri
	 * 		URI of a new track.
	 * @return The inserted track's ID.
	 * @throws SQLException
	 * 		If the database already contains this track.
	 */
	public long insertTrack(Uri uri) throws SQLException {

		ContentValues values = new ContentValues();
		values.put(TrackEntry.COLUMN_URI, uri.toString());

		return library.insertOrThrow(TrackEntry.NAME, null, values);
	}

	/**
	 * Insert a tag, and associate it with a track that uses it.
	 *
	 * @param trackId
	 * 		ID of a track in the database.
	 * @param tag
	 * 		A tag.
	 * @throws SQLException
	 * 		If the track ID is invalid
	 * @throws NullPointerException
	 * 		If either the name or value is missing
	 */
	public void insertTag(long trackId, Tag tag) throws SQLException,
			NullPointerException {

		long tagId = -1;

		if (tag == null) {
			Log.w(TAG, "Null tag for track " + String.valueOf(trackId));
			throw new NullPointerException("Missing tag");
		}

		// Check whether the tag exists already
		Cursor results = library
				.query(TagEntry.NAME, new String[] { TagEntry.COLUMN_ID },
				       String.format("%s = ? AND %s = ?", TagEntry.COLUMN_TAG,
				                     TagEntry.COLUMN_VAL),
				       new String[] { tag.getName(), tag.getValue() }, null,
				       null, null);

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
				values.put(TagEntry.COLUMN_TAG, tag.getName());
				values.put(TagEntry.COLUMN_VAL, tag.getValue());

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
