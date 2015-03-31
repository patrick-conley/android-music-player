package pconley.vamp.db.test;

import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryHelper;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Test that the database helper class works correctly. Detailed tests are to be
 * reserved for DAOs.
 */
public class LibraryHelperTest extends AndroidTestCase {

	private static final String namePrefix = "test_";

	private SQLiteDatabase library;

	// Get a reference to the database
	public void setUp() throws Exception {
		super.setUp();

		// TODO: instead, mock the call to PreferenceManager in LibraryHelper so
		// settings aren't reset.
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		preferences.edit().clear().commit();

		library = new LibraryHelper(new RenamingDelegatingContext(getContext(),
				namePrefix)).getWritableDatabase();
	}

	public void tearDown() throws Exception {
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.close();

		super.tearDown();
	}

	public void testDatabaseExists() {

		String uri = "Sample URI";

		// Get everything in Tracks; check empty
		Cursor contents = library.rawQuery("SELECT * FROM " + TrackEntry.NAME,
				null);
		assertEquals("Database is created empty", 0, contents.getCount());
		contents.close();

		// Add a track
		ContentValues rowContents = new ContentValues();
		rowContents.put(TrackEntry.COLUMN_URI, uri);
		library.insertOrThrow(TrackEntry.NAME, null, rowContents);

		// Check Tracks is correct
		contents = library.rawQuery("SELECT * FROM " + TrackEntry.NAME, null);
		assertEquals("Database has data after insert", 1, contents.getCount());
		contents.moveToFirst();
		assertEquals("Inserted data is correct", uri,
				contents.getString(contents
						.getColumnIndexOrThrow(TrackEntry.COLUMN_URI)));
		contents.close();
	}

}
