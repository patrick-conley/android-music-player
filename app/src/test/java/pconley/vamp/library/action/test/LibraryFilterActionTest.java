package pconley.vamp.library.action.test;

import android.app.FragmentManager;
import android.net.Uri;
import android.widget.Adapter;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pconley.vamp.R;
import pconley.vamp.library.view.LibraryActivity;
import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.library.action.LibraryFilterAction;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryFilterActionTest {

	private LibraryActivity activity;
	private FragmentManager fm;

	private TrackDAO dao;

	@Before
	public void setUpTest() {
		activity = Robolectric.buildActivity(LibraryActivity.class).create()
		                      .start().restart().get();
		fm = activity.getFragmentManager();

		dao = new TrackDAO(new LibraryOpenHelper(activity));
	}

	@After
	public void tearDownTest() {
		dao.wipeDatabase();
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
		new LibraryFilterAction().execute(activity, contents, 0);

		// Then
		List<Tag> filters
				= ((LibraryFragment) fm.findFragmentById(R.id.library))
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

		ArrayList<Tag> contents = new ArrayList<Tag>();
		contents.add(album);

		Set<Track> expected = new HashSet<Track>();
		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album)
				.add(new Tag("title", "foo"))
				.build();
		expected.add(track);
		dao.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album)
				.add(new Tag("title", "bar"))
				.build();
		expected.add(track);
		dao.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("three"))
				.add(album)
				.add(new Tag("title", "baz"))
				.build();
		expected.add(track);
		dao.insertTrack(track);

		activity = Robolectric.buildActivity(LibraryActivity.class).create()
		                      .start().restart().get();

		// When
		new LibraryFilterAction().execute(activity, contents, 0);

		// Then
		Adapter fragmentAdapter = ((ListView) activity.findViewById(
				R.id.library_contents)).getAdapter();

		Set<Track> actual = new HashSet<Track>();
		for (int i = 0; i < fragmentAdapter.getCount(); i++) {
			actual.add((Track) fragmentAdapter.getItem(i));
		}

		assertEquals("Tracks in the album are displayed", expected, actual);

	}

	/**
	 * Given the library contains several items in different albums, when I run
	 * the action, then items from one album are displayed.
	 */
	@Test
	public void testTwoAlbums() {
		ArrayList<Tag> contents = new ArrayList<Tag>();

		// Given
		Tag album1 = new Tag("album", "foo");
		contents.add(album1);

		Tag album2 = new Tag("album", "bar");
		contents.add(album2);

		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album1)
				.add(new Tag("title", "foo"))
				.build();
		dao.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album1)
				.add(new Tag("title", "bar"))
				.build();
		dao.insertTrack(track);

		Track expected = new Track.Builder(0, Uri.parse("three"))
				.add(album2)
				.add(new Tag("title", "baz"))
				.build();
		dao.insertTrack(expected);

		// When
		new LibraryFilterAction().execute(activity, contents, 1);

		// Then
		Adapter fragmentAdapter = ((ListView) activity.findViewById(
				R.id.library_contents)).getAdapter();

		assertEquals("Adapter contains tracks", 1, fragmentAdapter.getCount());
		assertEquals("Tracks in the album are displayed", expected,
		             fragmentAdapter.getItem(0));

	}

}
