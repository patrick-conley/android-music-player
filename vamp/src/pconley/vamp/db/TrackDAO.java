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

		// Hardcoded path: applies to my device only
		final String libPath = "file:///storage/extSdCard/Music/";

		// Insert several tracks
		long[] trackIds = new long[13];
		String[] uris = new String[] {
				// Test: MP4 tracks with "standard" tags
				libPath
						+ "popular/They Might Be Giants/They Might Be Giants/01 Everything Right Is Wrong Again.m4a",
				libPath
						+ "popular/They Might Be Giants/They Might Be Giants/02 Put Your Hand Inside the Puppet Head.m4a",
				libPath
						+ "popular/They Might Be Giants/They Might Be Giants/03 Number Three.m4a",
				libPath
						+ "popular/They Might Be Giants/They Might Be Giants/17 Alienation's for the Rich.m4a",
				libPath
						+ "popular/They Might Be Giants/They Might Be Giants/18 The Day.m4a",
				libPath
						+ "popular/They Might Be Giants/They Might Be Giants/19 Rhythm Section Want Ad.m4a",
				// Test: Ogg Vorbis tracks with unusual metadata
				libPath
						+ "classical/Schoenberg, Arnold (1874-1951)/Concerto for Violin and Orchestra, op 36/00-01 Poco allegro.ogg",
				libPath
						+ "classical/Schoenberg, Arnold (1874-1951)/Concerto for Violin and Orchestra, op 36/00-02 Andante grazioso.ogg",
				libPath
						+ "classical/Schoenberg, Arnold (1874-1951)/Concerto for Violin and Orchestra, op 36/00-03 Finale Allegro.ogg",
				libPath
						+ "classical/Sibelius, Jean (1865-1957)/Concerto for Violin and Orchestra in D minor, Op. 47/00-04 Allegro moderato.ogg",
				libPath
						+ "classical/Sibelius, Jean (1865-1957)/Concerto for Violin and Orchestra in D minor, Op. 47/00-05 Adagio di molto.ogg",
				libPath
						+ "classical/Sibelius, Jean (1865-1957)/Concerto for Violin and Orchestra in D minor, Op. 47/00-06 Allegro, ma non tanto.ogg",
				// Test: missing track
				libPath
						+ "popular/Elvis Presley/Girls! Girls! Girls!/Return to Sender.mp3", };

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
