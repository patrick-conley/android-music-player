package pconley.vamp.db;

import java.util.LinkedList;
import java.util.List;
import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TrackDAO {

	private SQLiteDatabase library;

	private static final String GET_TRACK_QUERY = String
			.format("SELECT * FROM (SELECT %s AS %s FROM %s WHERE %s = ?) INNER JOIN %s USING (%s) ORDER BY %s,%s",
					TrackTagRelation.TAG_ID, TagEntry.COLUMN_ID,
					TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
					TagEntry.NAME, TagEntry.COLUMN_ID, TagEntry.COLUMN_ID,
					TagEntry.COLUMN_TAG);

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
				throw new IllegalArgumentException(
						"Database returned more than one track");
			}
		}

		results.moveToFirst();

		if (results.getLong(trackIdColumn) != trackId) {
			throw new IllegalStateException(String.format(
					"Database returned incorrect track %d. %d expected.",
					results.getLong(trackIdColumn), trackId));
		}

		Track.Builder builder = new Track.Builder(trackId,
				results.getString(uriColumn));

		results.close();

		results = library.rawQuery(GET_TRACK_QUERY,
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

	/**
	 * Insert some sample tracks, and their tags into the database.
	 *
	 * @throws SQLException
	 *             If any of the tracks or tags already exists in the database
	 */
	@SuppressLint("SdCardPath")
	public static void createSampleLibrary(Context context) throws SQLException {
		SQLiteDatabase library = new LibraryHelper(context)
				.getWritableDatabase();
		library.execSQL("delete from TrackHasTags");
		library.execSQL("delete from Tags");
		library.execSQL("delete from Tracks");

		// Insert several tracks
		long[] trackIds = new long[12];
		String[] uris = new String[] {
				"file:///sdcard/Music/They Might Be Giants/They Might Be Giants/01 Everything Right Is Wrong Again.m4a",
				"file:///sdcard/Music/They Might Be Giants/They Might Be Giants/02 Put Your Hand Inside the Puppet Head.m4a",
				"file:///sdcard/Music/They Might Be Giants/They Might Be Giants/03 Number Three.m4a",
				"file:///sdcard/Music/They Might Be Giants/They Might Be Giants/17 Alienation's for the Rich.m4a",
				"file:///sdcard/Music/They Might Be Giants/They Might Be Giants/18 The Day.m4a",
				"file:///sdcard/Music/They Might Be Giants/They Might Be Giants/19 Rhythm Section Want Ad.m4a",
				"file:///sdcard/Music/Schoenberg, Arnold/Concerto for Violin and Orchestra/00-01 Poco allegro.ogg",
				"file:///sdcard/Music/Schoenberg, Arnold/Concerto for Violin and Orchestra/00-02 Andante grazioso.ogg",
				"file:///sdcard/Music/Schoenberg, Arnold/Concerto for Violin and Orchestra/00-03 Finale Allegro.ogg",
				"file:///sdcard/Music/Sibelius, Jean/Concerto for Violin and Orchestra in D minor/00-04 Allegro moderato.ogg",
				"file:///sdcard/Music/Sibelius, Jean/Concerto for Violin and Orchestra in D minor/00-05 Adagio di molto.ogg",
				"file:///sdcard/Music/Sibelius, Jean/Concerto for Violin and Orchestra in D minor/00-06 Allegro, ma non tanto.ogg", };

		for (int i = 0; i < uris.length; i++) {
			ContentValues value = new ContentValues();
			value.put(TrackEntry.COLUMN_URI, uris[i]);
			trackIds[i] = library.insertOrThrow(TrackEntry.NAME, null, value);
		}

		// Insert the tracks' tags
		long[] tagIds = new long[45];
		String[] tagNames = new String[] { "artist", "composer", "album",
				"genre", "discnumber", "year", "albumartist", "title", "title",
				"title", "title", "title", "title", "tracknumber",
				"tracknumber", "tracknumber", "tracknumber", "tracknumber",
				"tracknumber", "conductor", "genre", "genre", "performer",
				"date", "album", "ensemble", "artist", "label", "genre",
				"composer", "title", "opus", "part", "part", "part", "genre",
				"composer", "title", "opus", "part", "part", "part",
				"tracknumber", "tracknumber", "tracknumber" };
		String[] tagValues = new String[] { "They Might Be Giants",
				"They Might Be Giants", "They Might Be Giants",
				"Rock - Alternative", "1", "1986", "They Might Be Giants",
				"Everything Right Is Wrong Again",
				"Put Your Hand Inside the Puppet Head", "Number Three",
				"Alienation's for the Rich", "The Day",
				"Rhythm Section Want Ad", "1", "2", "3", "17", "18", "19",
				"Esa-Pekka Salonen", "Violin", "Concerto",
				"Hilary Hahn (Violin)", "2008-01-01",
				"Schoenberg/Sibelius · Violin Concertos",
				"Swedish Radio Symphony Orchestra",
				"Hilary Hahn/Swedish Radio Symph. Orch.", "Deutsch Grammophon",
				"20th Century", "Schoenberg, Arnold",
				"Concerto for Violin and Orchestra", "36", "Poco allegro",
				"Andante grazioso", "Finale Allegro", "Late Romantic",
				"Sibelius, Jean",
				"Concerto for Violin and Orchestra in D minor", "47",
				"Allegro moderato", "Adagio di molto", "Allegro, ma non tanto",
				"4", "5", "6" };

		for (int i = 0; i < tagNames.length; i++) {
			ContentValues value = new ContentValues();
			value.put(TagEntry.COLUMN_TAG, tagNames[i]);
			value.put(TagEntry.COLUMN_VAL, tagValues[i]);
			tagIds[i] = library.insertOrThrow(TagEntry.NAME, null, value);
		}

		// Relate the tracks to their tags
		int[] tracks = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6,
				6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7,
				7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
				9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10,
				10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11,
				11, 11, 11, 11, 11, 11, 11, 11, 11, 11 };

		int[] tags = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 13, 0, 1, 2, 3, 4, 5,
				6, 8, 14, 0, 1, 2, 3, 4, 5, 6, 9, 15, 0, 1, 2, 3, 4, 5, 6, 10,
				16, 0, 1, 2, 3, 4, 5, 6, 11, 17, 0, 1, 2, 3, 4, 5, 6, 12, 18,
				19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 13, 19,
				20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 33, 14, 19, 20,
				21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 34, 15, 19, 20, 21,
				22, 23, 24, 25, 26, 27, 35, 36, 37, 38, 39, 42, 19, 20, 21, 22,
				23, 24, 25, 26, 27, 35, 36, 37, 38, 40, 43, 19, 20, 21, 22, 23,
				24, 25, 26, 27, 35, 36, 37, 38, 41, 44, };

		for (int i = 0; i < tracks.length; i++) {
			ContentValues value = new ContentValues();
			value.put(TrackTagRelation.TRACK_ID, trackIds[tracks[i]]);
			value.put(TrackTagRelation.TAG_ID, tagIds[tags[i]]);
			library.insertOrThrow(TrackTagRelation.NAME, null, value);
		}

	}

}
