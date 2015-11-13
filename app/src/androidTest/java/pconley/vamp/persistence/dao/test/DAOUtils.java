package pconley.vamp.persistence.dao.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.LibrarySchema.TagEntry;
import pconley.vamp.persistence.LibrarySchema.TrackEntry;
import pconley.vamp.persistence.LibrarySchema.TrackTagRelation;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

public class DAOUtils {

	public static final Uri sampleUri = Uri.parse("file:///sample.ogg");
	public static final String[] sampleNames = { "title", "album", "artist" };
	public static final String[] sampleValues = { "SampleTrack",
			"SampleAlbum", "SampleArtist" };

	public static final Tag sampleTag = new Tag(sampleNames[0],
	                                            sampleValues[0]);
	private static LibraryOpenHelper helper;

	private DAOUtils() {
	}

	/*
	 * Add a track to the database to ensure there are unrelated tags floating
	 * around
	 */
	public static Track insertSampleTrack() {
		return insertTrack(sampleUri, sampleNames, sampleValues);
	}

	public static void setLibraryOpenHelper(LibraryOpenHelper helper) {
		DAOUtils.helper = helper;
	}

	/*
	 * Add a track to the database. Duplicate tracks will be updated.
	 */
	public static Track insertTrack(Uri uri, String[] tagNames,
			String[] tagValues) {
		long trackId;

		final String duplicateTrackSelect = TrackEntry.COLUMN_ID + " = ?";

		SQLiteDatabase library = helper.getWritableDatabase();

		// Check if the track already exists; get its ID or insert it
		Cursor dupes = library.query(TrackEntry.NAME,
		                             new String[] { TrackEntry.COLUMN_ID },
		                             duplicateTrackSelect,
		                             new String[] { uri.toString() }, null,
		                             null, null);

		if (dupes.getCount() > 0) {
			dupes.moveToFirst();
			trackId = dupes.getLong(dupes.getColumnIndexOrThrow(
					TrackEntry.COLUMN_ID));
		} else {
			ContentValues track = new ContentValues();
			track.put(TrackEntry.COLUMN_URI, uri.toString());
			trackId = library.insertOrThrow(TrackEntry.NAME, null, track);
		}

		dupes.close();

		Track.Builder builder = new Track.Builder(trackId, uri);

		if (tagNames != null) {
			for (int i = 0; i < tagNames.length; i++) {
				builder.add(new Tag(insertTag(tagNames[i], tagValues[i],
				                              trackId), tagNames[i],
				                    tagValues[i]));
			}
		}

		return builder.build();
	}

	/*
	 * Add a tag to the database. Duplicate tags (including for a single track)
	 * will be ignored.
	 */
	public static long insertTag(String name, String value,
			long relatedTrackId) {
		long tagId;

		SQLiteDatabase library = helper.getWritableDatabase();

		final String duplicateTagSelect = TagEntry.COLUMN_TAG + " = ? AND "
		                                  + TagEntry.COLUMN_VAL + " = ?";

		// Check if the tag already exists; get its ID or insert it
		Cursor dupes = library.query(TagEntry.NAME,
		                             new String[] { TagEntry.COLUMN_ID },
		                             duplicateTagSelect,
		                             new String[] { name, value }, null, null,
		                             null);

		if (dupes.getCount() > 0) {
			dupes.moveToFirst();
			tagId = dupes.getLong(dupes.getColumnIndexOrThrow(
					TagEntry.COLUMN_ID));
		} else {
			ContentValues tag = new ContentValues();
			tag.put(TagEntry.COLUMN_TAG, name);
			tag.put(TagEntry.COLUMN_VAL, value);

			tagId = library.insertOrThrow(TagEntry.NAME, null, tag);
		}

		dupes.close();

		ContentValues relation = new ContentValues();
		relation.put(TrackTagRelation.TRACK_ID, relatedTrackId);
		relation.put(TrackTagRelation.TAG_ID, tagId);
		library.insertWithOnConflict(TrackTagRelation.NAME, null, relation,
		                             SQLiteDatabase.CONFLICT_IGNORE);

		return tagId;
	}

	/*
	 * Insert all combinations of tags from sampleNames and sampleValue.
	 *
	 * @throw SQLException if anything goes wrong.
	 */
	public static void insertAllTags(long trackId) {
		SQLiteDatabase library = helper.getWritableDatabase();

		for (String name : sampleNames) {
			for (String value : sampleValues) {
				ContentValues tag = new ContentValues();
				tag.put(TagEntry.COLUMN_TAG, name);
				tag.put(TagEntry.COLUMN_VAL, value);
				long tagId = library.insertOrThrow(TagEntry.NAME, null, tag);

				ContentValues relation = new ContentValues();
				relation.put(TrackTagRelation.TRACK_ID, trackId);
				relation.put(TrackTagRelation.TAG_ID, tagId);
				library.insertOrThrow(TrackTagRelation.NAME, null, relation);
			}
		}
	}

}
