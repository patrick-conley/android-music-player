package pconley.vamp.db.test;

import pconley.vamp.db.LibraryHelper;
import pconley.vamp.db.LibraryContract.TrackEntry;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Test that the database helper class works correctly. This test case is
 * deliberately simple (and rather stupid): it's only meant to test that the
 * LibraryDatabaseHelper provides a database with approximately the write
 * schema, not that the database works correctly.
 */
public class LibraryHelperTest extends AndroidTestCase {

	private static final String namePrefix = "test_";

	public void testDatabaseExists() {

		String uri = "Sample URI";

		// Create the database (RenamingDelegatingContext ensures
		// production databases aren't overwritten)
		RenamingDelegatingContext context = new RenamingDelegatingContext(
				getContext(), namePrefix);
		LibraryHelper libraryHelper = new LibraryHelper(context);
		SQLiteDatabase library = libraryHelper.getWritableDatabase();

		// Get everything in Tracks; check empty
		Cursor contents = library.rawQuery("SELECT * FROM "
				+ TrackEntry.TABLE_NAME, null);
		assertEquals("Database is created empty", 0, contents.getCount());

		// Add a track
		ContentValues rowContents = new ContentValues();
		rowContents.put(TrackEntry.COLUMN_NAME_URI, uri);
		library.insertOrThrow(TrackEntry.TABLE_NAME, null, rowContents);

		// Check Tracks is correct
		contents = library.rawQuery("SELECT * FROM " + TrackEntry.TABLE_NAME,
				null);
		assertEquals("Database has data after insert", 1, contents.getCount());
		contents.moveToFirst();
		assertEquals("Inserted data is correct", uri,
				contents.getString(contents
						.getColumnIndexOrThrow(TrackEntry.COLUMN_NAME_URI)));

		// Delete the DB
		library.close();
		getContext().deleteDatabase(
				namePrefix + libraryHelper.getDatabaseName());
	}

}
