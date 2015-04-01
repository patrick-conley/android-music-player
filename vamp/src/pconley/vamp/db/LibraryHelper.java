package pconley.vamp.db;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.preferences.SettingsHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibraryHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "library.db";
	private static final String DEBUG_DATABASE_NAME = "sample.db";
	private static final int DATABASE_VERSION = 1;

	private Context context;

	/**
	 * Create a helper object to open a database. If the settings item
	 * "Use Sample Library" is set, then it replaces the database with a small,
	 * prepopulated database of sample tracks.
	 * 
	 * @param context
	 */
	public LibraryHelper(Context context) {
		super(
				context,
				new SettingsHelper(context).getDebugMode() ? DEBUG_DATABASE_NAME
						: DATABASE_NAME, null, DATABASE_VERSION);

		this.context = context;
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TrackEntry.SQL_CREATE);
		db.execSQL(TagEntry.SQL_CREATE);
		db.execSQL(TrackTagRelation.SQL_CREATE);

		if (new SettingsHelper(context).getDebugMode()) {
			populateSampleLibrary(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);

		db.setForeignKeyConstraintsEnabled(true);
	}

	/**
	 * Insert some sample tracks, and their tags into the database.
	 *
	 * @throws SQLException
	 *             If any of the tracks or tags already exists in the database
	 */
	private void populateSampleLibrary(SQLiteDatabase db) throws SQLException {

		// Hardcoded path: applies to my device only
		final String musicPath = "file:///storage/extSdCard/Music/";

		// Insert several tracks
		long[] trackIds = new long[13];
		String[] uris = new String[] {
				// Test: MP4 tracks with "standard" tags
				musicPath
						+ "popular/They Might Be Giants/They Might Be Giants/01 Everything Right Is Wrong Again.m4a",
				musicPath
						+ "popular/They Might Be Giants/They Might Be Giants/02 Put Your Hand Inside the Puppet Head.m4a",
				musicPath
						+ "popular/They Might Be Giants/They Might Be Giants/03 Number Three.m4a",
				musicPath
						+ "popular/They Might Be Giants/They Might Be Giants/17 Alienation's for the Rich.m4a",
				musicPath
						+ "popular/They Might Be Giants/They Might Be Giants/18 The Day.m4a",
				musicPath
						+ "popular/They Might Be Giants/They Might Be Giants/19 Rhythm Section Want Ad.m4a",
				// Test: Ogg Vorbis tracks with unusual metadata
				musicPath
						+ "classical/Schoenberg, Arnold (1874-1951)/Concerto for Violin and Orchestra, op 36/00-01 Poco allegro.ogg",
				musicPath
						+ "classical/Schoenberg, Arnold (1874-1951)/Concerto for Violin and Orchestra, op 36/00-02 Andante grazioso.ogg",
				musicPath
						+ "classical/Schoenberg, Arnold (1874-1951)/Concerto for Violin and Orchestra, op 36/00-03 Finale Allegro.ogg",
				musicPath
						+ "classical/Sibelius, Jean (1865-1957)/Concerto for Violin and Orchestra in D minor, Op. 47/00-04 Allegro moderato.ogg",
				musicPath
						+ "classical/Sibelius, Jean (1865-1957)/Concerto for Violin and Orchestra in D minor, Op. 47/00-05 Adagio di molto.ogg",
				musicPath
						+ "classical/Sibelius, Jean (1865-1957)/Concerto for Violin and Orchestra in D minor, Op. 47/00-06 Allegro, ma non tanto.ogg",
				// Test: missing track
				musicPath
						+ "popular/Elvis Presley/Girls! Girls! Girls!/Return to Sender.mp3", };

		for (int i = 0; i < uris.length; i++) {
			ContentValues value = new ContentValues();
			value.put(TrackEntry.COLUMN_URI, uris[i]);
			trackIds[i] = db.insertOrThrow(TrackEntry.NAME, null, value);
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
			tagIds[i] = db.insertOrThrow(TagEntry.NAME, null, value);
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
			db.insertOrThrow(TrackTagRelation.NAME, null, value);
		}

	}

}
