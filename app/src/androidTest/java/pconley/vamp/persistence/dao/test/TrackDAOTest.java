package pconley.vamp.persistence.dao.test;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.LibrarySchema.TagEntry;
import pconley.vamp.persistence.LibrarySchema.TrackEntry;
import pconley.vamp.persistence.LibrarySchema.TrackTagRelation;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.Constants;

import static android.test.MoreAsserts.assertEmpty;

public class TrackDAOTest extends AndroidTestCase {

	private static final Uri uri = Uri.parse("file:///track.ogg");

	private SQLiteDatabase library;
	private TrackDAO dao;

	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(),
		                                                Constants.DB_PREFIX);

		SettingsHelper.setPreferences(context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE));

		LibraryOpenHelper helper = new LibraryOpenHelper(context);

		DAOUtils.setLibraryOpenHelper(helper);
		library = helper.getWritableDatabase();
		dao = new TrackDAO(helper);

		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
	}

	/**
	 * Given there is nothing in the database, when I retrieve tracks, then I
	 * get nothing, successfully.
	 */
	public void testGetTracksFromEmptyDatabase() {
		// When
		List<Track> tracks = dao.getAllTracks();

		// Then
		assertEmpty("Empty database has no tracks", tracks);
	}

	/**
	 * When I insert several distinct tracks without tags and retrieve tracks,
	 * then I get the correct ones.
	 */
	public void testInsertDistinctTracks() {
		// When
		Set<Track> expected = new HashSet<Track>();
		for (int i = 0; i < 5; i++) {
			Uri uri = Uri.parse("track" + String.valueOf(i));
			Track track = new Track.Builder(-1, uri).build();

			expected.add(track);
			dao.insertTrack(track);
		}

		// Then
		assertEquals("Distinct tracks are inserted", expected,
		             new HashSet<Track>(dao.getAllTracks()));
	}

	/**
	 * When I insert a track with a null URI, then an exception is thrown.
	 */
	public void testInsertNullTrack() {
		// When
		try {
			dao.insertTrack(null);
			fail("Null track URIs can't be inserted");
		} catch (NullPointerException ignored) {
		}
	}

	/**
	 * When I insert a track twice, then an exception is thrown.
	 */
	public void testInsertDuplicateTrack() {
		Track track = new Track.Builder(-1, uri).add(DAOUtils.sampleTag)
		                                        .build();

		dao.insertTrack(track);

		try {
			dao.insertTrack(track);
			fail("A single track can't be added twice");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * Given the database has several tags, when I insert a track with some tags
	 * and retrieve tracks, then the track is returned correctly.
	 */
	public void testGetTrackWithSomeTags() {
		// Given
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();
		DAOUtils.insertAllTags(trackId);

		// When
		Track expected = DAOUtils.insertSampleTrack();
		List<Track> actual = dao.getAllTracks();

		// Then
		for (Track track : actual) {
			if (track.getUri().equals(DAOUtils.sampleUri)) {
				assertEquals("Track with some tags is retrieved", expected,
				             track);
				return;
			}
		}
		fail("Track with some tags is retrieved");
	}

	/**
	 * Given the database has two tracks with tags, when I retrieve tags through
	 * a collection with a single filter matching one track, then I get the
	 * correct tags.
	 */
	public void testGetTrackMatchingSingleTag() {
		// Given
		Track expected =
				DAOUtils.insertTrack(Uri.parse("track1"),
				                     new String[] { "title" },
				                     new String[] { "foo" });
		DAOUtils.insertTrack(Uri.parse("track2"),
		                     new String[] { "title" },
		                     new String[] { "bar" });

		// When
		List<Track> actual = dao.getTracksWithCollection(new MusicCollection(
				Collections.singletonList(expected.getTags("title").get(0)),
				null));

		// Then
		assertEquals("Find the track matching a single tag",
		             Collections.singletonList(expected), actual);
	}

	/**
	 * Given the database has four tracks with tags, when I retrieve tags
	 * through a collection with two filters matching one track, then I get the
	 * correct tags.
	 */
	public void testGetTrackMatchingTwoTags() {
		// Given
		Track expected =
				DAOUtils.insertTrack(Uri.parse("track1"),
				                     new String[] { "album", "title" },
				                     new String[] { "album1", "title1" });
		DAOUtils.insertTrack(Uri.parse("track2"),
		                     new String[] { "album", "title" },
		                     new String[] { "album1", "title2" });
		DAOUtils.insertTrack(Uri.parse("track3"),
		                     new String[] { "album", "title" },
		                     new String[] { "album2", "title1" });
		DAOUtils.insertTrack(Uri.parse("track4"),
		                     new String[] { "album", "title" },
		                     new String[] { "album2", "title2" });

		// When
		List<Track> actual = dao.getTracksWithCollection(new MusicCollection(
				Arrays.asList(expected.getTags("album").get(0),
				              expected.getTags("title").get(0)), null));

		// Then
		assertEquals("Find the track matching a single tag",
		             Collections.singletonList(expected), actual);
	}

	/**
	 * Given the database has eight tracks with tags, when I retrieve tags
	 * through a collection with three filters matching one track, then I get
	 * the correct tags.
	 */
	public void testGetTrackMatchingThreeTags() {
		// Given
		Track expected = DAOUtils.insertTrack(
				Uri.parse("track1"),
				new String[] { "artist", "album", "title" },
				new String[] { "artist1", "album1", "title1" });
		DAOUtils.insertTrack(Uri.parse("track2"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist1", "album1", "title2" });
		DAOUtils.insertTrack(Uri.parse("track3"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist1", "album2", "title1" });
		DAOUtils.insertTrack(Uri.parse("track4"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist1", "album2", "title2" });
		DAOUtils.insertTrack(Uri.parse("track5"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist2", "album1", "title1" });
		DAOUtils.insertTrack(Uri.parse("track6"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist2", "album1", "title2" });
		DAOUtils.insertTrack(Uri.parse("track7"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist2", "album2", "title1" });
		DAOUtils.insertTrack(Uri.parse("track8"),
		                     new String[] { "artist", "album", "title" },
		                     new String[] { "artist2", "album2", "title2" });

		// When
		List<Track> actual = dao.getTracksWithCollection(new MusicCollection(
				Arrays.asList(expected.getTags("artist").get(0),
				              expected.getTags("album").get(0),
				              expected.getTags("title").get(0)), null));

		// Then
		assertEquals("Find the track matching a single tag",
		             Collections.singletonList(expected), actual);
	}

	/**
	 * Given the database contains tracks and tags, when I empty the database,
	 * then it contains nothing.
	 */
	public void testWipeDatabase() {
		// Given
		DAOUtils.insertSampleTrack();

		// When
		dao.wipeDatabase();

		// Then
		Cursor results = library.query(TrackEntry.NAME,
		                               new String[] { TrackEntry.COLUMN_ID },
		                               null, null, null, null, null);
		assertEquals("Track table is empty", 0, results.getCount());
		results.close();

		results = library.query(TagEntry.NAME,
		                        new String[] { TagEntry.COLUMN_ID }, null, null,
		                        null, null, null);
		assertEquals("Tag table is empty", 0, results.getCount());
		results.close();

		results = library.query(TrackTagRelation.NAME,
		                        new String[] { TrackTagRelation.TAG_ID }, null,
		                        null, null, null, null);
		assertEquals("Relation is empty", 0, results.getCount());
		results.close();

	}

}
