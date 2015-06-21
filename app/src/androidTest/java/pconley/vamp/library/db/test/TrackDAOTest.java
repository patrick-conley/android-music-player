package pconley.vamp.library.db.test;

import static android.test.MoreAsserts.assertContentsInAnyOrder;
import static android.test.MoreAsserts.assertEmpty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pconley.vamp.library.db.LibraryOpenHelper;
import pconley.vamp.library.db.LibrarySchema.TagEntry;
import pconley.vamp.library.db.LibrarySchema.TrackEntry;
import pconley.vamp.library.db.LibrarySchema.TrackTagRelation;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class TrackDAOTest extends AndroidTestCase {

	private static final Uri uri = Uri.parse("file:///track.ogg");

	private static final Uri sampleUri = Uri.parse("file:///sample.ogg");
	private static final String[] sampleTagNames = { "title", "album", "artist" };
	private static final String[] sampleTagValues = { "SampleTrack",
			"SampleAlbum", "SampleArtist" };
	private static Tag sampleTag;

	private SQLiteDatabase library;
	private TrackDAO dao;

	public TrackDAOTest() {
		sampleTag = new Tag(sampleTagNames[0], sampleTagValues[0]);
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

	/**
	 * Given I have nothing in the database, when I retrieve tracks, then I get
	 * nothing, successfully.
	 */
	public void testGetTracksOnEmptyDatabase() {
		dao.openReadableDatabase();

		List<Track> tracks = dao.getTracks();

		assertEmpty("DAO retrieves nothing from an empty database", tracks);
	}

	/**
	 * Given that the database has been closed, when I retrieve tracks, then I
	 * get an exception.
	 */
	public void testReadOnClosedDatabase() {
		dao.openReadableDatabase().close();

		try {
			dao.getTracks();
			fail("DAO throws an exception on read-after-close.");
		} catch (IllegalStateException ignored) {
		}
	}

	/**
	 * Given that the database has been closed, when I close the database, then
	 * I do not get any exception.
	 */
	public void testCloseOnClosedDatabase() {
		dao.openReadableDatabase().close();
		dao.close();
	}

	/**
	 * Given there are tracks in the database, when I retrieve tracks, then I
	 * get all of them.
	 */
	public void testGetTracksOnNonemptyDatabase() {
		dao.openReadableDatabase();

		// Insert some tracks. No associated tags are needed.
		List<Track> expected = new LinkedList<Track>();
		for (int i = 0; i < 5; i++) {
			String trackUri = "file:///track" + String.valueOf(i * 7) + ".mp3";

			ContentValues value = new ContentValues();
			value.put(TrackEntry.COLUMN_URI, trackUri);

			long id = library.insertOrThrow(TrackEntry.NAME, null, value);
			expected.add(new Track.Builder(id, Uri.parse(trackUri)).build());
		}

		assertEquals("DAO can retrieve items", expected, dao.getTracks());
	}

	/**
	 * Given there are tracks in the database, when I retrieve tags for a track
	 * not in the database, then I get null, successfully.
	 */
	public void testGetTrackOnMissingTrack() {
		dao.openReadableDatabase();

		Track sample = insertSampleTrack();

		assertNull("DAO returns nothing for a non-existent track",
				dao.getTrack(sample.getId() + 1));
	}

	/**
	 * Given a track with no tags in the database, when I retrieve its tags,
	 * then I get a Track with no tags.
	 */
	public void testGetTrackWithNoTags() {
		dao.openReadableDatabase();

		Track expected = insertTrack(uri, new String[] {}, new String[] {});

		assertEquals("DAO returns the correct track", expected,
				dao.getTrack(expected.getId()));
		assertEquals("DAO returns the correct tracks",
				Arrays.asList(new Track[] { expected }), dao.getTracks());
	}

	/**
	 * Given a track with tags in the database, when I retrieve its tags, then I
	 * get all of them.
	 */
	public void testGetTrackOneTrack() {
		dao.openReadableDatabase();

		String[] expectedNames = { "a", "b", "c" };
		String[] expectedValues = { "aValue", "bValue", "cValue" };

		Track expected = insertTrack(uri, expectedNames, expectedValues);

		assertEquals("DAO returns the correct tags: one track exists (track)",
				expected, dao.getTrack(expected.getId()));
		assertEquals("DAO returns the correct tags: one track exists (tracks)",
				Arrays.asList(new Track[] { expected }), dao.getTracks());
	}

	/**
	 * Given a track with tags in the database, and two of its tags have the
	 * same name, when I retrieve its tags, then I get both of them.
	 */
	public void testGetTrackWithSameName() {
		dao.openReadableDatabase();
		insertSampleTrack();

		Track expected = insertTrack(uri, new String[] { "a", "b", "a" },
				new String[] { "aValue", "bValue", "cValue" });

		assertEquals(
				"DAO returns the correct tags: one has multiple values (track)",
				expected, dao.getTrack(expected.getId()));
	}

	/**
	 * Given there are two tracks in the database, and they have distinct tags
	 * with the same name, when I retrieve tags from one, then I get the correct
	 * ones.
	 */
	public void testGetTrackTwoTracksWithSameTagName() {
		dao.openReadableDatabase();
		Track sample = insertSampleTrack();

		Track expected = insertTrack(uri, new String[] { "a",
				sampleTagNames[1], "c" }, new String[] { "aValue", "bValue",
				"cValue" });

		assertEquals(
				"DAO returns the correct tags: tracks have the same tag names (track)",
				expected, dao.getTrack(expected.getId()));
		assertContentsInAnyOrder(
				"DAO returns the correct tags: tracks have the same tag names (tracks)",
				dao.getTracks(), sample, expected);
	}

	/**
	 * Given there are two tracks in the database, and they have the same tags,
	 * when I retrieve tags from each, then I get the correct ones.
	 */
	public void testGetTrackTwoTracksWithIdenticalTags() {
		dao.openReadableDatabase();

		Track sample = insertSampleTrack();
		Track expected = insertTrack(uri, sampleTagNames, sampleTagValues);
		Track actual = dao.getTrack(expected.getId());

		assertEquals("DAO returns the correct tags: identical tracks",
				expected, actual);

		// Verify the tags have the expected IDs
		Set<Long> sampleIds = new HashSet<Long>();
		for (String name : sample.getTagNames()) {
			for (Tag tag : sample.getTags(name)) {
				sampleIds.add(tag.getId());
			}
		}

		Set<Long> expectedIds = new HashSet<Long>();
		for (String name : expected.getTagNames()) {
			for (Tag tag : expected.getTags(name)) {
				expectedIds.add(tag.getId());
			}
		}

		Set<Long> actualIds = new HashSet<Long>();
		for (String name : actual.getTagNames()) {
			for (Tag tag : actual.getTags(name)) {
				actualIds.add(tag.getId());
			}
		}

		assertEquals("Tags have the same ID in both tracks", sampleIds,
				expectedIds);
		assertEquals("Tags have the correct ID (are not duplicated in the DB)",
				expectedIds, actualIds);

		assertContentsInAnyOrder(
				"Both tracks are returned with the correct tags",
				dao.getTracks(), sample, expected);
	}

	/**
	 * Given there are two tracks in the database, and they have the some tags
	 * in common, when I retrieve tags from each, then I get the correct ones.
	 */
	public void testGetTrackTwoTracksWithSomeSharedTags() {
		dao.openReadableDatabase();
		Track sample = insertSampleTrack();

		Track expected = insertTrack(uri, new String[] { "a",
				sampleTagNames[1], "c" }, new String[] { "aValue",
				sampleTagValues[1], "cValue" });
		Track actual = dao.getTrack(expected.getId());

		assertEquals("Tags are not duplicated in the database",
				sample.getTags(sampleTagNames[1]).iterator().next().getId(),
				expected.getTags(sampleTagNames[1]).iterator().next().getId());

		assertEquals("DAO returns the correct tags: some duplicated", expected,
				actual);

		actual = dao.getTrack(sample.getId());
		assertEquals("DAO returns the correct tags: some duplicated (reprise)",
				sample, actual);
	}

	/**
	 * Given the database is empty, when I insert a new track, then the correct
	 * track exists.
	 */
	public void testInsertTrack() {
		dao.openWritableDatabase();

		// Given
		Cursor results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_URI }, null, null, null, null,
				null);

		assertEquals("Database is initially empty", 0, results.getCount());
		results.close();

		// When
		dao.insertTrack(uri);

		// Then
		results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_URI }, null, null, null, null,
				null);

		assertEquals("Database contains one inserted track", 1,
				results.getCount());

		results.moveToFirst();
		assertEquals(
				"Inserted track is correct",
				uri.toString(),
				results.getString(results.getColumnIndex(
						TrackEntry.COLUMN_URI)));

		results.close();
	}

	/**
	 * Given the database is not empty, when I insert a new track, then the
	 * correct track exists.
	 */
	public void testInsertSecondTrack() {
		dao.openWritableDatabase();

		// Given
		insertTrack(sampleUri, null, null);
		Cursor results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_URI }, null, null, null, null,
				null);

		assertEquals("Database is initially not empty", 1, results.getCount());
		results.close();

		// When
		long id = dao.insertTrack(uri);

		// Then
		results = library.query(TrackEntry.NAME, new String[] {
				TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI }, null, null,
				null, null, null);

		// A track has been inserted
		assertEquals("Database contains two inserted tracks", 2,
				results.getCount());

		// The correct track has been inserted
		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			if (results.getLong(results.getColumnIndex(TrackEntry.COLUMN_ID)) == id) {
				assertEquals("Inserted track has the correct URI",
						uri.toString(), results.getString(results
								.getColumnIndex(TrackEntry.COLUMN_URI)));
				break;
			}
		}

		if (results.isAfterLast()) {
			fail("Database contains the inserted track");
		}
		results.close();

	}

	/**
	 * Given the database is not empty, when I insert a duplicate track, then an
	 * exception is thrown.
	 */
	public void testInsertDuplicateTrack() {
		dao.openWritableDatabase();

		// Given
		insertTrack(uri, null, null);
		Cursor results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_ID }, null, null, null, null,
				null);

		assertEquals("Database is initially not empty", 1, results.getCount());
		results.close();

		// When
		try {
			dao.insertTrack(uri);
			fail("Inserting a duplicate track throws an exception");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * Given the database contains a track, when I insert a tag, then the
	 * correct track/tag exists.
	 */
	public void testInsertTag() {
		dao.openWritableDatabase();

		// Given
		long trackId = insertTrack(uri, null, null).getId();

		// When
		dao.insertTag(trackId, sampleTag);

		// Then

		// Check the correct tag exists
		Cursor results = library.query(TagEntry.NAME, new String[] {
				TagEntry.COLUMN_ID, TagEntry.COLUMN_TAG, TagEntry.COLUMN_VAL },
				null, null, null, null, null);

		assertEquals("Tag table has one tag", 1, results.getCount());

		results.moveToFirst();
		long tagId = results
				.getLong(results.getColumnIndex(TagEntry.COLUMN_ID));
		assertEquals("Inserted tag has the right name", sampleTagNames[0],
				results.getString(results.getColumnIndex(TagEntry.COLUMN_TAG)));
		assertEquals("Inserted tag has the right value", sampleTagValues[0],
		             results.getString(results.getColumnIndex(
				             TagEntry.COLUMN_VAL)));
		results.close();

		// Check the track has this tag
		results = library.query(TrackTagRelation.NAME, new String[] {
				TrackTagRelation.TRACK_ID, TrackTagRelation.TAG_ID }, null,
				null, null, null, null);

		assertEquals("A track has a tag", 1, results.getCount());

		results.moveToFirst();
		assertEquals("Track in relation is correct", trackId,
		             results.getLong(results
				                             .getColumnIndex(
						                             TrackTagRelation.TRACK_ID)));
		assertEquals(
				"Tag in relation is correct",
				tagId,
				results.getLong(results.getColumnIndex(TrackTagRelation.TAG_ID)));
		results.close();
	}

	/**
	 * Given the database contains two tracks, when I insert the same tag for
	 * both, then the correct tracks exist.
	 */
	public void testInsertTagOnTwoTracks() {
		dao.openWritableDatabase();

		// Given
		long id1 = insertTrack(uri, null, null).getId();
		long id2 = insertTrack(sampleUri, null, null).getId();

		// When
		dao.insertTag(id1, sampleTag);
		dao.insertTag(id2, sampleTag);

		// Then

		// Check the correct tag exists
		// Don't bother checking the tag itself is correct - that's done by
		// testInsertTag()
		Cursor results = library.query(TagEntry.NAME,
				new String[] { TagEntry.COLUMN_ID }, null, null, null, null,
				null);

		assertEquals("Tag table has one tag", 1, results.getCount());
		results.moveToFirst();
		long tagId = results
				.getLong(results.getColumnIndex(TagEntry.COLUMN_ID));
		results.close();

		// Check the correct relations exist
		results = library.query(TrackTagRelation.NAME,
				new String[] { TrackTagRelation.TAG_ID }, null, null, null,
				null, null);

		assertEquals("Two tracks have tags", 2, results.getCount());
		for (results.moveToFirst(); !results.isAfterLast(); results
				.moveToNext()) {
			assertEquals("Track has the correct tag", tagId,
					results.getLong(results
							.getColumnIndex(TrackTagRelation.TAG_ID)));
		}
		results.close();

	}

	/**
	 * Given the database contains a track and tag, when I insert the same tag,
	 * then an exception is thrown.
	 */
	public void testInsertDuplicateTag() {
		dao.openWritableDatabase();

		// Given
		long trackId = insertSampleTrack().getId();

		// When
		try {
			dao.insertTag(trackId, sampleTag);
			fail("Inserting a duplicate tag on one track is an exception");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * Given the database contains a track, when I insert a tag for a
	 * nonexistent track, then an exception is thrown.
	 */
	public void testInsertTagOnMissingTrack() {
		dao.openWritableDatabase();

		// Given
		long trackId = insertSampleTrack().getId();

		// When
		try {
			dao.insertTag(trackId + 1, sampleTag);
			fail("Inserting a tag without a corresponding track is an exception");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * Given the database contains tracks and tags, when I empty the database,
	 * then it contains nothing.
	 */
	public void testWipeDatabase() {
		dao.openWritableDatabase();

		// Given
		insertTrack(sampleUri, sampleTagNames, sampleTagValues);

		// When
		dao.wipeDatabase();

		// Then
		Cursor results = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_ID }, null, null, null, null,
				null);
		assertEquals("Track table is empty", 0, results.getCount());
		results.close();

		results = library.query(TagEntry.NAME,
				new String[] { TagEntry.COLUMN_ID }, null, null, null, null,
				null);
		assertEquals("Tag table is empty", 0, results.getCount());
		results.close();

		results = library.query(TrackTagRelation.NAME,
				new String[] { TrackTagRelation.TAG_ID }, null, null, null,
				null, null);
		assertEquals("Relation is empty", 0, results.getCount());
		results.close();

	}

	/**
	 * When I insert a null tag, then the database throws an exception.
	 */
	public void testInsertNullTag() {
		dao.openWritableDatabase();

		// When
		try {
			dao.insertTag(0, null);
			fail("Inserting a null tag is an exception");
		} catch (NullPointerException ignored) {

		}
	}

	/*
	 * Add a track to the database to ensure there are unrelated tags floating
	 * around
	 */
	private Track insertSampleTrack() {
		return insertTrack(sampleUri, sampleTagNames, sampleTagValues);
	}

	/*
	 * Add a track to the database. Duplicate tracks will be updated.
	 */
	private Track insertTrack(Uri uri, String[] tagNames, String[] tagValues) {
		long trackId;

		final String duplicateTrackSelect = TrackEntry.COLUMN_ID + " = ?";

		// Check if the track already exists; get its ID or insert it
		Cursor dupes = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_ID }, duplicateTrackSelect,
				new String[] { uri.toString() }, null, null, null);

		if (dupes.getCount() > 0) {
			dupes.moveToFirst();
			trackId = dupes.getLong(dupes
					.getColumnIndexOrThrow(TrackEntry.COLUMN_ID));
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
						trackId), tagNames[i], tagValues[i]));
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
				new String[] { TagEntry.COLUMN_ID }, duplicateTagSelect,
				new String[] { name, value }, null, null, null);

		if (dupes.getCount() > 0) {
			dupes.moveToFirst();
			tagId = dupes.getLong(dupes
					.getColumnIndexOrThrow(TagEntry.COLUMN_ID));
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

}
