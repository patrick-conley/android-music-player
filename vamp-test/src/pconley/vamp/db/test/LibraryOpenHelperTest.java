package pconley.vamp.db.test;

import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryOpenHelper;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Test that the database helper class works correctly. Detailed tests are to be
 * reserved for DAOs.
 */
public class LibraryOpenHelperTest extends AndroidTestCase {

	private SQLiteDatabase library;

	// Get a reference to the database
	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(),
				Constants.DB_PREFIX);

		SettingsHelper.setPreferences(context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE));

		library = new LibraryOpenHelper(context).getWritableDatabase();
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
