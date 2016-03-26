package pconley.vamp.persistence.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.LibrarySchema.TagEntry;
import pconley.vamp.persistence.LibrarySchema.TrackTagRelation;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.util.TrackUtil;

public class TagDAO {
	private static final String TAG = "TagDAO";

	private LibraryOpenHelper libraryOpenHelper;

	public TagDAO(LibraryOpenHelper libraryOpenHelper) {
		this.libraryOpenHelper = libraryOpenHelper;
	}

	/**
	 * Convenience method. Get all tags.
	 */
	public List<Tag> getAllTags() {
		Cursor rows = libraryOpenHelper.getReadableDatabase().query(
				TagEntry.NAME, null, null, null, null, null, null);

		int idCol = rows.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int tagCol = rows.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valCol = rows.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		List<Tag> tags = new LinkedList<Tag>();
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
	 * @param filter
	 * @param name
	 * @return The tags whose name matches the collection and which belong in
	 * tracks that match the set of tags in the collection.
	 * @throws IllegalArgumentException
	 * 		if the collection's name isn't set
	 */
	public List<Tag> getFilteredTags(List<Tag> filter, String name)
			throws IllegalArgumentException {
		if (name == null) {
			throw new IllegalArgumentException("Tag name unset");
		}
		int nTags = filter == null ? 0 : filter.size();

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
				        TrackUtil.buildMatchingTracksQuery(nTags));

		// Use the tag IDs and desired name to make the selection
		String[] selectionArgs = new String[nTags + 1];
		for (int i = 0; i < nTags; i++) {
			selectionArgs[i] =
					String.valueOf(filter.get(i).getId());
		}
		selectionArgs[nTags] = name;

		// Get the tags
		Cursor results = libraryOpenHelper.getReadableDatabase()
		                                  .rawQuery(query, selectionArgs);

		int idColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int valueColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		List<Tag> tags = new LinkedList<Tag>();

		for (results.moveToFirst(); !results.isAfterLast();
		     results.moveToNext()) {
			tags.add(new Tag(results.getLong(idColumn), name,
			                 results.getString(valueColumn)));
		}

		results.close();

		return tags;
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
		SQLiteDatabase library = libraryOpenHelper.getWritableDatabase();

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

}
