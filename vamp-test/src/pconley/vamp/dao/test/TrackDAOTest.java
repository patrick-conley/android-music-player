package pconley.vamp.dao.test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pconley.vamp.dao.TrackDAO;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryHelper;
import pconley.vamp.model.Track;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class TrackDAOTest extends AndroidTestCase {

	private static final String namePrefix = "test_";

	private SQLiteDatabase library;
	private TrackDAO dao;

	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(), namePrefix);

		library = new LibraryHelper(context).getWritableDatabase();
		dao = new TrackDAO(context);
	}

	public void tearDown() throws Exception {
		dao.close();

		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.close();

		super.tearDown();
	}

	public void testGetTracksOnEmptyDatabase() {
		List<Track> tracks = dao.getTracks();

		assertEquals("DAO retrieves nothing from an empty database", 0,
				tracks.size());
	}

	public void testGetTracksOnNonemptyDatabase() {

		// Insert some tracks. No associated tags are needed.
		Set<Track> expected = new HashSet<Track>();
		for (int i = 0; i < 5; i++) {
			String uri = "file:///music/" + String.valueOf(i * 7);
			Track track = new Track(uri);

			ContentValues value = new ContentValues();
			value.put(TrackEntry.COLUMN_URI, uri);

			library.insertOrThrow(TrackEntry.NAME, null, value);
			expected.add(track);
		}

		Set<Track> actual = new HashSet<Track>(dao.getTracks());

		assertEquals("DAO can retrieve items", expected, actual);
	}

	public void testGetTracksNoDupes() {

		// Insert some tracks.
		Set<Track> expected = new HashSet<Track>();
		for (int i = 0; i < 10; i++) {
			String uri = "file:///music/" + String.valueOf((i * 7) % 5);
			Track track = new Track(uri);

			ContentValues value = new ContentValues();
			value.put(TrackEntry.COLUMN_URI, uri);

			library.insert(TrackEntry.NAME, null, value);
			expected.add(track);
		}

		List<Track> actual = new LinkedList<Track>(dao.getTracks());

		assertEquals("DAO does not return extra tracks", expected.size(),
				actual.size());
		for (Track track : expected) {
			assertTrue("DAO returns the correct tracks",
					actual.contains(track));
		}
	}

}
