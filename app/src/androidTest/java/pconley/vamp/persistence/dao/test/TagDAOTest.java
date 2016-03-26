package pconley.vamp.persistence.dao.test;

import android.content.Context;
import android.database.SQLException;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.preferences.SettingsHelper;
import pconley.vamp.util.Constants;

import static android.test.MoreAsserts.assertEmpty;

public class TagDAOTest extends AndroidTestCase {

	private static final Uri uri = Uri.parse("file:///track1.ogg");

	private TagDAO dao;

	public void setUp() throws Exception {
		super.setUp();

		Context context = new RenamingDelegatingContext(getContext(),
		                                                Constants.DB_PREFIX);

		SettingsHelper.setPreferences(context.getSharedPreferences(
				Constants.PREFERENCES_NAME, Context.MODE_PRIVATE));

		LibraryOpenHelper helper = new LibraryOpenHelper(context);
		DAOUtils.setLibraryOpenHelper(helper);
		dao = new TagDAO(helper);
	}

	/**
	 * Given there is nothing in the database, when I retrieve tags, then I get
	 * nothing, successfully.
	 */
	public void testGetTagsFromEmptyDatabase() {
		// When
		List<Tag> tags = dao.getAllTags();

		// Then
		assertEmpty("Empty database has no tracks", tags);
	}

	/**
	 * Given the database contains a track, when I insert several distinct tags,
	 * then the database contains the correct tags.
	 */
	public void testInsertDistinctTags() {
		// Given
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();

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
		             new HashSet<Tag>(dao.getAllTags()));
	}

	/**
	 * Given the database contains a track, when I insert a null tag, then an
	 * exception is thrown.
	 */
	public void testInsertNullTag() {
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();

		// When
		try {
			dao.insertTag(trackId, null);
			fail("Null tags can't be inserted");
		} catch (NullPointerException ignored) {
		}
	}

	/**
	 * When I insert a tag to a missing track, then an exception is thrown.
	 */
	public void testInsertTagOnMissingTrack() {
		// Given
		long trackId = DAOUtils.insertSampleTrack().getId();

		// When
		try {
			dao.insertTag(trackId + 1, DAOUtils.sampleTag);
			fail("Tags can't be inserted into a nonexistent track");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * When I insert a tag twice for different tracks, then the database has
	 * only one tag.
	 */
	public void testInsertRepeatedTagOnSeveralTracks() {
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();
		dao.insertTag(trackId, DAOUtils.sampleTag);

		trackId = DAOUtils.insertTrack(Uri.parse("file:///track2.ogg"), null,
		                               null).getId();
		dao.insertTag(trackId, DAOUtils.sampleTag);

		assertEquals("Tags are shared between tracks.",
		             Collections.singletonList(DAOUtils.sampleTag),
		             dao.getAllTags());
	}

	/**
	 * When I insert a tag twice for a single track, then an exception is
	 * thrown.
	 */
	public void testInsertRepeatedTagOnOneTrack() {
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();
		dao.insertTag(trackId, DAOUtils.sampleTag);

		try {
			dao.insertTag(trackId, DAOUtils.sampleTag);
			fail("Tags can't be inserted more than once to a track");
		} catch (SQLException ignored) {

		}
	}

	/**
	 * Given the database has several tags, when I retrieve tags matching some
	 * name, then I get the correct ones.
	 */
	public void testGetTags() {
		String name = DAOUtils.sampleNames[0];

		// Given
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();
		DAOUtils.insertAllTags(trackId);

		Set<Tag> expected = new HashSet<Tag>();
		for (String value : DAOUtils.sampleValues) {
			expected.add(new Tag(name, value));
		}

		// When/Then
		assertEquals("Tags are retrieved by name", expected,
		             new HashSet<Tag>(dao.getFilteredTags(null, name)));
	}

	/**
	 * Given the database has several tags, when I retrieve tags matching some
	 * name that doesn't exist, then I get nothing.
	 */
	public void testGetAbsentTags() {
		String name = "no such name";

		// Given
		long trackId = DAOUtils.insertTrack(uri, null, null).getId();
		DAOUtils.insertAllTags(trackId);

		// When/Then
		assertEquals("No tags are retrieved if the name is wrong",
		             Collections.emptyList(), dao.getFilteredTags(null, name));
	}

	/**
	 * Given the database has several tags, when I retrieve tags matching a
	 * nameless collection, then an exception is thrown.
	 */
	public void testGettagsForEmptyCollection() {
		// Given
		DAOUtils.insertSampleTrack();

		try {
			dao.getFilteredTags(null, null);
			fail("Can't get tags for a nameless/filterless collection");
		} catch (IllegalArgumentException ignored) {

		}
	}

}
