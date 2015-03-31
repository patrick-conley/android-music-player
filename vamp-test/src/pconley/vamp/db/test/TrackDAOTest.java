package pconley.vamp.db.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;
import pconley.vamp.db.LibraryHelper;
import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class TrackDAOTest extends AndroidTestCase {

	private static final String namePrefix = "test_";
	private static final Uri uri = Uri.parse("file:///track.ogg");

	private static final Uri sampleUri = Uri.parse("file:///sample.ogg");
	private static final String[] sampleTagNames = { "title", "album", "artist" };
	private static final String[] sampleTagValues = { "SampleTrack",
			"SampleAlbum", "SampleArtist" };

	private static final String duplicateTrackSelection = TrackEntry.COLUMN_ID
			+ " = ?";
	private static final String duplicateTagSelection = TagEntry.COLUMN_TAG
			+ " = ? AND " + TagEntry.COLUMN_VAL + " = ?";

	private SQLiteDatabase library;
	private TrackDAO dao;

	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(),
				namePrefix);

		// TODO: instead, mock the call to PreferenceManager in LibraryHelper so
		// settings aren't reset.
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		preferences.edit().clear().commit();

		library = new LibraryHelper(context).getWritableDatabase();
		dao = new TrackDAO(context);

		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
	}

	/**
	 * Given I have nothing in the database, when I try to retrieve tracks, then
	 * I get nothing, successfully.
	 */
	public void testGetTracksOnEmptyDatabase() {
		List<Long> tracks = dao.getIds();

		assertEquals("DAO retrieves nothing from an empty database", 0,
				tracks.size());
	}

	/**
	 * Given that the database has been closed, when I try to retrieve tracks,
	 * then I get an exception.
	 */
	public void testReadOnClosedDatabase() {
		dao.close();

		try {
			dao.getIds();
		} catch (IllegalStateException e) {
			return;
		}

		fail("DAO throws an exception on read-after-close.");
	}

	/**
	 * Given there are tracks in the database, when I try to retrieve tracks,
	 * then I get all of them.
	 */
	public void testGetTracksOnNonemptyDatabase() {

		// Insert some tracks. No associated tags are needed.
		Set<Long> expected = new HashSet<Long>();
		for (int i = 0; i < 5; i++) {
			String trackUri = "file:///track" + String.valueOf(i * 7) + ".mp3";

			ContentValues value = new ContentValues();
			value.put(TrackEntry.COLUMN_URI, trackUri);

			long id = library.insertOrThrow(TrackEntry.NAME, null, value);
			expected.add(id);
		}

		Set<Long> actual = new HashSet<Long>(dao.getIds());

		assertEquals("DAO can retrieve items", expected, actual);
	}

	/**
	 * Given there are tracks in the database, when I try to retrieve tags for a
	 * track not in the database, then I get null, successfully.
	 */
	public void testGetTrackOnMissingTrack() {
		Track sample = insertSampleTrack();

		Track actual = dao.getTrack(sample.getId() + 1);

		assertNull(sample.toString()
				+ "DAO returns nothing for a non-existent track", actual);
	}

	/**
	 * Given a track with no tags in the database, when I try to retrieve its
	 * tags, then I get a Track with no tags.
	 */
	public void testGetTrackOnNoTags() {
		insertSampleTrack();

		Track expected = insertTrack(uri, new String[] {}, new String[] {});
		Track actual = dao.getTrack(expected.getId());

		assertEquals("DAO returns the correct track", expected, actual);
		assertEquals("DAO returns no tags for an empty track", 0, actual
				.getTagNames().size());
	}

	/**
	 * Given a track with tags in the database, when I try to retrieve its tags,
	 * then I get all of them.
	 */
	public void testGetTrackOneTrack() {
		insertSampleTrack();

		String[] expectedNames = { "a", "b", "c" };
		String[] expectedValues = { "aValue", "bValue", "cValue" };

		Track expected = insertTrack(uri, expectedNames, expectedValues);
		Track actual = dao.getTrack(expected.getId());

		assertEquals("DAO returns the correct tags: one track exists",
				expected, actual);
	}

	/**
	 * Given a track with tags in the database, and two of its tags have the
	 * same name, when I try to retrieve its tags, then I get both of them.
	 */
	public void testGetTrackWithSameName() {
		insertSampleTrack();

		Track expected = insertTrack(uri, new String[] { "a", "b", "a" },
				new String[] { "aValue", "bValue", "cValue" });
		Track actual = dao.getTrack(expected.getId());

		assertEquals("DAO returns the correct tags: one has multiple values",
				expected, actual);
	}

	/**
	 * Given there are two tracks in the database, and they have distinct tags
	 * with the same name, when I try to retrieve tags from one, then I get the
	 * correct ones.
	 */
	public void testGetTrackTwoTracksWithSameTagName() {
		insertSampleTrack();

		Track expected = insertTrack(uri, new String[] { "a",
				sampleTagNames[1], "c" }, new String[] { "aValue", "bValue",
				"cValue" });
		Track actual = dao.getTrack(expected.getId());

		assertEquals(
				"DAO returns the correct tags: tracks have the same tag names",
				expected, actual);
	}

	/**
	 * Given there are two tracks in the database, and they have the same tags,
	 * when I try to retrieve tags from each, then I get the correct ones.
	 */
	public void testGetTrackTwoTracksWithIdenticalTags() {
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
	}

	/**
	 * Given there are two tracks in the database, and they have the some tags
	 * in common, when I try to retrieve tags from each, then I get the correct
	 * ones.
	 */
	public void testGetTrackTwoTracksWithSomeSharedTags() {
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

		// Check if the track already exists; get its ID or insert it
		Cursor dupes = library.query(TrackEntry.NAME,
				new String[] { TrackEntry.COLUMN_ID }, duplicateTrackSelection,
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

		for (int i = 0; i < tagNames.length; i++) {
			builder.add(new Tag(insertTag(tagNames[i], tagValues[i], trackId),
					tagNames[i], tagValues[i]));
		}

		return builder.build();
	}

	/*
	 * Add a tag to the database. Duplicate tags (including for a single track)
	 * will be ignored.
	 */
	private long insertTag(String name, String value, long relatedTrackId) {
		long tagId;

		// Check if the tag already exists; get its ID or insert it
		Cursor dupes = library.query(TagEntry.NAME,
				new String[] { TagEntry.COLUMN_ID }, duplicateTagSelection,
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
