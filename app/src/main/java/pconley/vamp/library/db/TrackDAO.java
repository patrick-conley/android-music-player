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
import pconley.vamp.model.MusicCollection;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;

/**
 * Methods to read and write track data from the database.
 *
 * @author pconley
 */
public class TrackDAO {
	private static final String TAG = "TrackDAO";

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

	private LibraryOpenHelper libraryOpenHelper;
	private SQLiteDatabase library;

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
		if (library != null) {
			library.close();
		}
	}

	/**
	 * Convenience method. Get all tracks.
	 */
	public List<Track> getTracks() {
		if (library == null) {
			throw new IllegalStateException("Library is not open");
		}

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
	 * Find the tracks that belong in the given collection.
	 * <p/>
	 *
	 * @param collection
	 * @return The tracks matching the set of tags in the collection.
	 */
	public List<Track> getTracks(MusicCollection collection) {
		int nTags = collection.getHistory().size();

		// SELECT trackId, trackUri, tagId, name, value FROM
		//   buildMatchingTracksQuery
		//   INNER JOIN Tags ON tagId = Tags._id
		//   INNER JOIN Tracks ON trackId = Tracks._id;
		String query = MessageFormat
				.format("SELECT {1}, {3}, {5}, {7}, {8} FROM {9} " +
				        "INNER JOIN {4} ON {5} = {4}.{6} " +
				        "INNER JOIN {0} ON {1} = {0}.{2}",
				        TrackEntry.NAME, TrackTagRelation.TRACK_ID,
				        TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI,
				        TagEntry.NAME, TrackTagRelation.TAG_ID,
				        TagEntry.COLUMN_ID, TagEntry.COLUMN_TAG,
				        TagEntry.COLUMN_VAL, buildMatchingTracksQuery(nTags));

		// Use the tag IDs to make the selection
		String[] selectionArgs = new String[nTags];
		for (int i = 0; i < nTags; i++) {
			selectionArgs[i] =
					String.valueOf(collection.getHistory().get(i).getId());
		}

		// Get the tracks
		Cursor results = library.rawQuery(query, selectionArgs);

		List<Track> tracks = new LinkedList<Track>();

		Track track;
		while ((track = mapTrack(results)) != null) {
			tracks.add(track);
		}

		results.close();

		return tracks;
	}

	/**
	 * Convenience method. Get all tags.
	 */
	public List<Tag> getTags() {
		List<Tag> tags = new LinkedList<Tag>();

		Cursor rows = library.query(TagEntry.NAME, null, null, null, null,
		                            null, null);

		int idCol = rows.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int tagCol = rows.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valCol = rows.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);
		for (rows.moveToFirst(); !rows.isAfterLast(); rows.moveToNext()) {
			tags.add(new Tag(rows.getLong(idCol), rows.getString(tagCol),
			                 rows.getString(valCol)));
		}

		rows.close();

		return tags;
	}

	/**
	 * Get the tags that belong in the given collection.
	 * <p/>
	 * TODO: Count is currently discarded. Add the count to Tag, or return a map
	 * of tags to integers
	 * <p/>
	 *
	 * @param collection
	 * @return The tags whose name matches the collection and which belong in
	 * tracks that match the set of tags in the collection.
	 * @throws IllegalArgumentException
	 * 		if the collection's name isn't set
	 */
	public List<Tag> getTags(MusicCollection collection)
			throws IllegalArgumentException {
		if (collection.getSelection() == null) {
			throw new IllegalArgumentException("Tag name unset");
		}
		int nTags = collection.getHistory() == null ? 0
		                                         : collection.getHistory().size();

		// SELECT *, COUNT(*) FROM buildMatchingTracksQuery
		//   INNER JOIN Tags ON tagId = _id
		// WHERE name = ?
		// GROUP BY (value);
		String query = MessageFormat
				.format("SELECT *, COUNT(*) FROM {6} " +
				        "INNER JOIN {2} ON {1} = {3} " +
				        "WHERE {4} = ? GROUP BY ({5})",
				        TrackTagRelation.NAME, TrackTagRelation.TAG_ID,
				        TagEntry.NAME, TagEntry.COLUMN_ID,
				        TagEntry.COLUMN_TAG, TagEntry.COLUMN_VAL,
				        buildMatchingTracksQuery(nTags));

		// Use the tag IDs and desired name to make the selection
		String[] selectionArgs = new String[nTags + 1];
		for (int i = 0; i < nTags; i++) {
			selectionArgs[i] =
					String.valueOf(collection.getHistory().get(i).getId());
		}
		selectionArgs[nTags] = collection.getSelection();

		// Get the tags
		Cursor results = library.rawQuery(query, selectionArgs);

		int idColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int valueColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		List<Tag> tags = new LinkedList<Tag>();

		for (results.moveToFirst(); !results.isAfterLast();
		     results.moveToNext()) {
			tags.add(new Tag(results.getLong(idColumn), collection.getSelection(),
			                 results.getString(valueColumn)));
		}

		results.close();

		return tags;
	}

	/**
	 * Build a table/subquery to return the track IDs matching a set of tags, by
	 * ID. The tag IDs must be inserted as selection args when the query is
	 * executed.
	 *
	 * @param nTags
	 * 		Number of tags to match
	 * @return An SQL query giving track IDs
	 */
	private String buildMatchingTracksQuery(int nTags) {
		if (nTags <= 0) {
			return TrackTagRelation.NAME;
		}

		String tracksWithTag = MessageFormat
				.format("(SELECT {1} FROM {0} WHERE {2} = ?)",
				        TrackTagRelation.NAME,
				        TrackTagRelation.TRACK_ID,
				        TrackTagRelation.TAG_ID);

		// (SELECT trackId FROM TrackHasTags WHERE tagId = ?)
		// NATURAL JOIN ([as above]) [repeat as needed]
		StringBuilder query = new StringBuilder(tracksWithTag);
		for (int i = 1; i < nTags; i++) {
			query.append(" NATURAL JOIN ").append(tracksWithTag);
		}

		// INNER JOIN TrackHasTags USING trackId
		query.append(MessageFormat
				             .format(" INNER JOIN {0} USING ({1})",
				                     TrackTagRelation.NAME,
				                     TrackTagRelation.TRACK_ID));

		return query.toString();
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

		// First track: move to the first row
		if (cursor.isBeforeFirst()) {
			cursor.moveToFirst();
		}

		// Last track: do nothing
		if (cursor.isAfterLast()) {
			return null;
		}

		int trackIdColumn = cursor
				.getColumnIndexOrThrow(TrackTagRelation.TRACK_ID);
		int uriColumn = cursor.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);
		int tagIdColumn = cursor.getColumnIndexOrThrow(TrackTagRelation.TAG_ID);
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
		if (library == null) {
			throw new IllegalStateException("Library is not open");
		}

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

		// Insert the tag & relation
		library.beginTransaction();
		try {
			ContentValues values;

			// Tag
			if (tagId == -1) {
				values = new ContentValues();
				values.put(TagEntry.COLUMN_TAG, tag.getName());
				values.put(TagEntry.COLUMN_VAL, tag.getValue());

				tagId = library.insertOrThrow(TagEntry.NAME, null, values);
			}

			// Track relation
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
