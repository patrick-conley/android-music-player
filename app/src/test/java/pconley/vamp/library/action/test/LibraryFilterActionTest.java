package pconley.vamp.library.action.test;

import android.app.Activity;
import android.app.FragmentManager;
import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.action.LibraryFilterAction;
import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.library.view.MockLibraryActivity;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.TagCollection;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.model.TrackCollection;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryFilterActionTest {

	private Activity activity;
	private FragmentManager fm;

	private TrackDAO trackDao;
	private TagDAO tagDao;

	@Before
	public void setUpTest() {
		activity = Robolectric.buildActivity(MockLibraryActivity.class).create()
		                      .start().restart().get();
		fm = activity.getFragmentManager();

		LibraryOpenHelper helper = new LibraryOpenHelper(activity);
		trackDao = new TrackDAO(helper);
		tagDao = new TagDAO(helper);
	}

	@After
	public void tearDownTest() {
		trackDao.wipeDatabase();
	}

	/**
	 * Given the library contains a single item, when I run the action, then an
	 * Album filter is added.
	 */
	@Test
	public void testSingleItem() {
		// Given
		Tag album = new Tag("album", "foo");

		ArrayList<Tag> contents = new ArrayList<Tag>();
		contents.add(album);

		// When
		new LibraryFilterAction(activity,
		                        new TagCollection("name", null, contents))
				.execute(0);
		Robolectric.runBackgroundTasks();
		Robolectric.runUiThreadTasks();

		// Then
		List<Tag> filters = ((LibraryFragment) fm
				.findFragmentById(R.id.library_container))
				.getCollection().getFilter();

		assertEquals("Album filter is added to the fragment",
		             Collections.singletonList(album), filters);
	}

	/**
	 * Given the library contains several items in a common album, when I run
	 * the action, all items are displayed.
	 */
	@Test
	public void testCommonAlbum() {
		// Given
		Tag album = new Tag("album", "foo");

		List<Track> expected = new LinkedList<Track>();
		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album)
				.add(new Tag("title", "foo"))
				.build();
		expected.add(track);
		trackDao.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album)
				.add(new Tag("title", "bar"))
				.build();
		expected.add(track);
		trackDao.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("three"))
				.add(album)
				.add(new Tag("title", "baz"))
				.build();
		expected.add(track);
		trackDao.insertTrack(track);

		List<Tag> contents = tagDao.getFilteredTags(null, "album");

		// When
		new LibraryFilterAction(activity,
		                        new TagCollection("name", null, contents))
				.execute(0);

		// Then
		LibraryFragment fragment = (LibraryFragment) fm.findFragmentById(
				R.id.library_container);

		assertEquals("Collection is built correctly",
		             new TrackCollection(contents, expected),
		             fragment.getCollection());
	}

	/**
	 * Given the library contains several items in different albums, when I run
	 * the action, then items from one album are displayed.
	 */
	@Test
	public void testTwoAlbums() {
		// Given
		Tag album1 = new Tag("album", "foo");
		Tag album2 = new Tag("album", "bar");

		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album1)
				.add(new Tag("title", "foo"))
				.build();
		trackDao.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album1)
				.add(new Tag("title", "bar"))
				.build();
		trackDao.insertTrack(track);

		Track expected = new Track.Builder(0, Uri.parse("three"))
				.add(album2)
				.add(new Tag("title", "baz"))
				.build();
		/* FIXME: expected seems to be out of order. Change DAOs to return object w. correct ID */
		trackDao.insertTrack(expected);

		List<Tag> contents = tagDao.getFilteredTags(null, "album");

		// When
		new LibraryFilterAction(activity,
		                        new TagCollection("name", null, contents))
				.execute(0);

		// Then
		LibraryFragment fragment = (LibraryFragment) fm.findFragmentById(
				R.id.library_container);

		assertEquals("Collection is built correctly",
		             new TrackCollection(
				             Collections.singletonList(contents.get(0)),
				             Collections.singletonList(expected)),
		             fragment.getCollection());
	}

}
