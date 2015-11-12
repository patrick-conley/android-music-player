package pconley.vamp.library.db.test;

import android.content.ContentValues;
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

import pconley.vamp.library.db.LibraryOpenHelper;
import pconley.vamp.library.db.LibrarySchema.TagEntry;
import pconley.vamp.library.db.LibrarySchema.TrackEntry;
import pconley.vamp.library.db.LibrarySchema.TrackTagRelation;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.MusicCollection;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.Constants;

import static android.test.MoreAsserts.assertEmpty;

public class TrackDAOTest extends AndroidTestCase {

	private static final Uri uri = Uri.parse("file:///track.ogg");

	private static final Uri sampleUri = Uri.parse("file:///sample.ogg");
	private static final String[] sampleNames = { "title", "album", "artist" };
	private static final String[] sampleValues = { "SampleTrack",
			"SampleAlbum", "SampleArtist" };
	private static Tag sampleTag;

	private SQLiteDatabase library;
	private TrackDAO dao;

	public TrackDAOTest() {
		sampleTag = new Tag(sampleNames[0], sampleValues[0]);
	}

	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(),
		                                                Constants.DB_PREFIX);

		SettingsHelper.setPreferences(context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE));

		library = new LibraryOpenHelper(context).getWritableDatabase();
		dao = new TrackDAO(context);

		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
	}

	public void tearDown() {
		if (dao != null) {
			dao.close();
		}
	}

	/**
	 * Given the database has not been opened, when I retrieve a track, then an
	 * exception is thrown.
	 */
	public void testReadOnUnopenedDatabase() {
		try {
			dao.getTracks();
			fail("DAO fails on read-before-open");
		} catch (IllegalStateException ignored) {
		}
	}

	/**
	 * Given the database has not been opened, when I insert a track, then an
	 * exception is thrown
	 */
	public void testWriteOnUnopenedDatabase() {
		try {
			dao.insertTrack(Uri.parse("test"));
			fail("DAO fails on write-before-open");
		} catch (IllegalStateException ignored) {
		}
	}

	/**
	 * Given the database has been closed, when I close the database, then
	 * nothing happens.
	 */
	public void testCloseOnClosedDatabase() {
		dao.openReadableDatabase().close();
		dao.close();
	}

	/**
	 * Given the database has been closed, when I retrieve a track, then an
	 * exception is thrown.
	 */
	public void testReadOnClosedDatabase() {
		dao.openReadableDatabase().close();

		try {
			dao.getTracks();
			fail("DAO fails on read-after-close");
		} catch (IllegalStateException ignored) {
		}
	}

	/**
	 * Given the database has been closed, when I insert a track, then an
	 * exception is thrown.
	 */
	public void testWriteOnClosedDatabase() {
		dao.openWritableDatabase().close();

		try {
			dao.insertTrack(Uri.parse("test"));
			fail("DAO fails on write-after-close");
		} catch (IllegalStateException ignored) {
		}
	}

	/**
	 * Given there is nothing in the database, when I retrieve tags, then I get
	 * nothing, successfully.
	 */
	public void testGetTagsFromEmptyDatabase() {
		dao.openReadableDatabase();

		// When
		List<Tag> tags = dao.getTags();

		// Then
		assertEmpty("Empty database has no tracks", tags);
	}

	/**
	 * Given there is nothing in the database, when I retrieve tracks, then I
	 * get nothing, successfully.
	 */
	public void testGetTracksFromEmptyDatabase() {
		dao.openReadableDatabase();

		// When
		List<Track> tracks = dao.getTracks();

		// Then
		assertEmpty("Empty database has no tracks", tracks);
	}

	/**
	 * Given the database contains a track, when I insert several distinct tags,
	 * then the database contains the correct tags.
	 */
	public void testInsertDistinctTags() {
		dao.openWritableDatabase();

		// Given
		long trackId = dao.insertTrack(Uri.parse("sample"));

		// When
		Set<Tag> expected = new HashSet<Tag>();
		for (int i = 0; i < 5; i++) {
			Tag tag = new Tag("name " + String.valueOf(i),
			                  "value " + String.valueOf(i));
			expected.add(tag);
			dao.insertTag(trackId, tag);
		}

		// Then
		assertEquals("Distinct tags are inserted", expected,
		             new HashSet<Tag>(dao.getTags()));
	}

	/**
	 * When I insert several distinct tracks without tags and retrieve tracks,
	 * then I get the correct ones.
	 */
	public void testInsertDistinctTracks() {
		dao.openWritableDatabase();

		// When
		Set<Track> expected = new HashSet<Track>();
		for (int i = 0; i < 5; i++) {
			Uri uri = Uri.parse("track" + String.valueOf(i));

			expected.add(new Track.Builder(-1, uri).build());
			dao.insertTrack(uri);
		}

		// Then
		assertEquals("Distinct tracks are inserted", expected,
		             new HashSet<Track>(dao.getTracks()));
	}

	/**
	 * Given the database contains a track, when I insert a null tag, then an
	 * exception is thrown.
	 */
	public void testInsertNullTag() {
		dao.openWritableDatabase();

		long trackId = dao.insertTrack(Uri.parse("sample"));

		// When
		try {
			dao.insertTag(trackId, null);
			fail("Null tags can't be inserted");
		} catch (NullPointerException ignored) {
		}
	}

	/**
	 * When I insert a track with a null URI, then an exception is thrown.
	 */
	public void testInsertNullTrack() {
		dao.openWritableDatabase();

		// When
		try {
			dao.insertTrack(null);
			fail("Null track URIs can't be inserted");
		} catch (NullPointerException ignored) {
		}
	}

	/**
	 * When I insert a tag to a missing track, then an exception is thrown.
	 */
	public void testInsertTagOnMissingTrack() {
		dao.openWritableDatabase();

		// Given
		long trackId = insertSampleTrack().getId();

		// When
		try {
			dao.insertTag(trackId + 1, sampleTag);
			fail("Tags can't be inserted into a nonexistent track");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * When I insert a tag twice for different tracks, then the database has
	 * only one tag.
	 */
	public void testInsertRepeatedTagOnSeveralTracks() {
		dao.openWritableDatabase();

		long trackId = dao.insertTrack(Uri.parse("track1.ogg"));
		dao.insertTag(trackId, sampleTag);

		trackId = dao.insertTrack(Uri.parse("track2.ogg"));
		dao.insertTag(trackId, sampleTag);

		assertEquals("Tags are shared between tracks.",
		             Collections.singletonList(sampleTag), dao.getTags());

	}

	/**
	 * When I insert a tag twice for a single track, then an exception is
	 * thrown.
	 */
	public void testInsertRepeatedTagOnOneTrack() {
		dao.openWritableDatabase();

		long trackId = dao.insertTrack(uri);
		dao.insertTag(trackId, sampleTag);

		try {
			dao.insertTag(trackId, sampleTag);
			fail("Tags can't be inserted more than once to a track");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * When I insert a track twice, then an exception is thrown.
	 */
	public void testInsertDuplicateTrack() {
		dao.openWritableDatabase();

		dao.insertTrack(uri);

		try {
			dao.insertTrack(uri);
			fail("A single track can't be added twice");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * Given the database has several tags, when I retrieve tags matching some
	 * name, then I get the correct ones.
	 */
	public void testGetTags() {
		dao.openWritableDatabase();

		MusicCollection match = new MusicCollection(null, sampleNames[0]);

		// Given
		long trackId = dao.insertTrack(uri);
		insertAllTags(trackId);

		Set<Tag> expected = new HashSet<Tag>();
		for (String value : sampleValues) {
			expected.add(new Tag(match.getSelection(), value));
		}

		// When/Then
		assertEquals("Tags are retrieved by name", expected,
		             new HashSet<Tag>(dao.getTags(match)));
	}

	/**
	 * Given the database has several tags, when I retrieve tags matching some
	 * name that doesn't exist, then I get nothing.
	 */
	public void testGetAbsentTags() {
		dao.openWritableDatabase();

		MusicCollection match = new MusicCollection(null, "no such name");

		// Given
		long trackId = dao.insertTrack(uri);
		insertAllTags(trackId);

		// When/Then
		assertEquals("No tags are retrieved if the name is wrong",
		             Collections.emptyList(), dao.getTags(match));
	}

	/**
	 * Given the database has several tags, when I insert a track with some tags
	 * and retrieve tracks, then the track is returned correctly.
	 */
	public void testGetTrackWithSomeTags() {
		dao.openWritableDatabase();

		// Given
		long trackId = dao.insertTrack(uri);
		insertAllTags(trackId);

		// When
		Track expected = insertSampleTrack();
		List<Track> actual = dao.getTracks();

		// Then
		for (Track track : actual) {
			if (track.getUri().equals(sampleUri)) {
				assertEquals("Track with some tags is retrieved", expected,
				             track);
				return;
			}
		}
		fail("Track with some tags is retrieved");
	}

	/**
	 * Given the database has several tags, when I retrieve tags matching a
	 * nameless collection, then an exception is thrown.
	 */
	public void testGetTracksForEmptyCollection() {
		dao.openWritableDatabase();

		// Given
		insertSampleTrack();

		try {
			dao.getTags(new MusicCollection(null, null));
			fail("Can't get tags for a nameless/filterless collection");
		} catch (IllegalArgumentException ignored) {

		}

	}

	/**
	 * Given the database has two tracks with tags, when I retrieve tags through
	 * a collection with a single filter matching one track, then I get the
	 * correct tags.
	 */
	public void testGetTrackMatchingSingleTag() {
		dao.openWritableDatabase();

		// Given
		Track expected = insertTrack(Uri.parse("track1"),
		                             new String[] { "title" },
		                             new String[] { "foo" });
		insertTrack(Uri.parse("track2"),
		            new String[] { "title" },
		            new String[] { "bar" });

		// When
		List<Track> actual = dao.getTracks(new MusicCollection(
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
		dao.openWritableDatabase();

		// Given
		Track expected = insertTrack(Uri.parse("track1"),
		                             new String[] { "album", "title" },
		                             new String[] { "album1", "title1" });
		insertTrack(Uri.parse("track2"),
		            new String[] { "album", "title" },
		            new String[] { "album1", "title2" });
		insertTrack(Uri.parse("track3"),
		            new String[] { "album", "title" },
		            new String[] { "album2", "title1" });
		insertTrack(Uri.parse("track4"),
		            new String[] { "album", "title" },
		            new String[] { "album2", "title2" });

		// When
		List<Track> actual = dao.getTracks(new MusicCollection(
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
		dao.openWritableDatabase();

		// Given
		Track expected = insertTrack(Uri.parse("track1"),
		                             new String[] { "artist", "album",
				                             "title" },
		                             new String[] { "artist1", "album1",
				                             "title1" });
		insertTrack(Uri.parse("track2"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist1", "album1", "title2" });
		insertTrack(Uri.parse("track3"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist1", "album2", "title1" });
		insertTrack(Uri.parse("track4"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist1", "album2", "title2" });
		insertTrack(Uri.parse("track5"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist2", "album1", "title1" });
		insertTrack(Uri.parse("track6"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist2", "album1", "title2" });
		insertTrack(Uri.parse("track7"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist2", "album2", "title1" });
		insertTrack(Uri.parse("track8"),
		            new String[] { "artist", "album", "title" },
		            new String[] { "artist2", "album2", "title2" });

		// When
		List<Track> actual = dao.getTracks(new MusicCollection(
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
		dao.openWritableDatabase();

		// Given
		insertTrack(sampleUri, sampleNames, sampleValues);

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

	/*
	 * Add a track to the database to ensure there are unrelated tags floating
	 * around
	 */
	private Track insertSampleTrack() {
		return insertTrack(sampleUri, sampleNames, sampleValues);
	}

	/*
	 * Add a track to the database. Duplicate tracks will be updated.
	 */
	private Track insertTrack(Uri uri, String[] tagNames, String[] tagValues) {
		long trackId;

		final String duplicateTrackSelect = TrackEntry.COLUMN_ID + " = ?";

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
	private long insertTag(String name, String value, long relatedTrackId) {
		long tagId;

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
	private void insertAllTags(long trackId) {
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
