package pconley.vamp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.source.TagSource;

public class TagDAO implements TagSource {

	private SQLiteDatabase library;

	/**
	 * Open a database connection. Should not be called from the UI thread.
	 *
	 * @param context
	 *            Activity context
	 */
	public TagDAO(Context context) {
		LibraryHelper dbHelper = new LibraryHelper(context);
		library = dbHelper.getWritableDatabase();
	}

	/**
	 * Load every tag for a single track.
	 *
	 * @param track
	 */
	public void getTags(Track track) {

		String query = String.format("SELECT * FROM (SELECT %s AS %s FROM %s WHERE %s = ?) INNER JOIN %s USING (%s) ORDER BY %s,%s",
				TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID, TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
				TagEntry.NAME, TagEntry.COLUMN_ID, TagEntry.COLUMN_ID, TagEntry.COLUMN_TAG);

		Cursor results = library.rawQuery(query, new String[] { String.valueOf(track.getId()) });
		int idColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
		int tagColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		int valColumn = results.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);

		Tag.Builder builder = null;
		String tagName = null;
		for (results.moveToFirst(); !results.isAfterLast(); results.moveToNext()) {

			// Check if this row is the first value of a new tag
			if (!results.getString(tagColumn).equals(tagName)) {
				if (builder != null) {
					track.addTag(builder.build());
				}
				builder = new Tag.Builder();
				builder.name(results.getString(tagColumn));
			}

			builder.addValue(results.getInt(idColumn), results.getString(valColumn));
		}

		if (builder != null) {
			track.addTag(builder.build());
		}

		results.close();
	}
}

