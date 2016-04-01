package pconley.vamp.library.view.test;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.FragmentTestUtil;

import java.util.ArrayList;

import pconley.vamp.R;
import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.library.view.MockLibraryActivity;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.player.view.PlayerActivity;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryFragmentTest {

	private TrackDAO trackDAO;

	@Before
	public void setUpTest() {
		Context context = Robolectric.getShadowApplication()
		                             .getApplicationContext();
		LibraryOpenHelper helper = new LibraryOpenHelper(context);
		trackDAO = new TrackDAO(helper);
	}

	@After
	public void tearDownTest() {
		trackDAO.wipeDatabase();
	}

	/**
	 * When I create a fragment, then its progress bar is visible and it shows
	 * nothing
	 */
	@Test
	public void createFragment() {
		// When
		LibraryFragment fragment = new LibraryFragment();
		startFragment(fragment);

		// Then
		View fragmentView = fragment.getView();
		assertEquals("Fragment shows progress bar", ProgressBar.VISIBLE,
		             ((ProgressBar) fragmentView.findViewById(
				             R.id.library_progress_load)).getVisibility());
		assertEquals("Fragment is empty", 0,
		             ((ListView) fragmentView.findViewById(
				             R.id.library_contents)).getCount());
	}

	/**
	 * When I create a fragment, then its collection is not immediately
	 * available.
	 */
	@Test(expected = IllegalStateException.class)
	public void getUnsetCollection() {
		// When
		LibraryFragment fragment = new LibraryFragment();
		startFragment(fragment);

		// Then
		fragment.getCollection();
	}

	/**
	 * Given a started fragment, when I add a collection to the fragment, then
	 * it displays that collection.
	 */
	@Test
	public void addCollection() {
		ArrayList<Tag> contents = new ArrayList<Tag>();
		contents.add(new Tag(0, "foo", "bar"));
		contents.add(new Tag(0, "foo", "baz"));

		LibraryFragment fragment = new LibraryFragment();
		startFragment(fragment);

		// When
		fragment.setCollection(new MusicCollection("artist", null, contents));

		// Then
		Adapter adapter = ((ListView) fragment.getView().findViewById(
				R.id.library_contents)).getAdapter();
		assertEquals("Fragment's view is updated", contents.size(),
		             adapter.getCount());
	}

	/**
	 * Given a fragment which is not started, when I add a collection to the
	 * fragment and I start the fragment, then it displays that collection.
	 */
	@Test
	public void addCollectionAsync() {
		ArrayList<Tag> contents = new ArrayList<Tag>();
		contents.add(new Tag(0, "foo", "bar"));
		contents.add(new Tag(0, "foo", "baz"));

		// Given
		LibraryFragment fragment = new LibraryFragment();

		// When
		fragment.setCollection(new MusicCollection("artist", null, contents));
		startFragment(fragment);

		// Then
		Adapter collection = ((ListView) fragment.getView().findViewById(
				R.id.library_contents)).getAdapter();
		assertEquals("Fragment's view is updated when created", contents.size(),
		             collection.getCount());
	}

	/* TODO: test playOnLoad */

	/**
	 * Given the fragment's collection is unset, when I click the Play All
	 * button, then an exception is thrown.
	 */
	@Test(expected = IllegalStateException.class)
	public void testPlayContentsUnsetCollection() {

		// Given
		LibraryFragment fragment = new LibraryFragment();
		startFragment(fragment);

		// When
		fragment.playContents();
	}

	/**
	 * Given the fragment's collection is set, when I click the Play All button,
	 * then the PlayerActivity is launched
	 */
	@Test
	public void testClickPlayContents() {
		ArrayList<Tag> contents = new ArrayList<Tag>();
		contents.add(new Tag(0, "foo", "bar"));
		contents.add(new Tag(0, "foo", "baz"));

		// Given
		LibraryFragment fragment = new LibraryFragment();
		fragment.setCollection(new MusicCollection("artist", null, contents));
		startFragment(fragment);

		// When
		fragment.playContents();

		// Then
		ShadowActivity shadow = Robolectric.shadowOf(fragment.getActivity());
		Intent next = shadow.peekNextStartedActivity();

		assertEquals("Play All starts the PlayerActivity",
		             PlayerActivity.class.getCanonicalName(),
		             next.getComponent().getClassName());
	}

	// TODO: feature not implemented
	//	/**
	//	 * Given the library is not empty, when I filter against a collection with
	//	 * only one child, then the child is stepped into.
	//	 */
	//	@Test
	//	public void stepThroughSolitaryTags() {
	//		List<Tag> tags = new LinkedList<Tag>();
	//		tags.add(new Tag("artist", "artist"));
	//		tags.add(new Tag("album", "album"));
	//		tags.add(new Tag("title", "title"));
	//
	//		// Given
	//		TrackDAO dao = new TrackDAO(context).openWritableDatabase();
	//		dao.insertTrack(Uri.parse("track1"), tags);
	//		dao.insertTrack(Uri.parse("track2"), tags);
	//
	//		Tag artist = dao.getHistory(new MusicCollection(null, "artist")).get(0);
	//		List<Track> expected = dao.getFilteredTracks(new MusicCollection(null, null));
	//
	//		// When
	//		LibraryFragment fragment = LibraryFragment.newInstance(
	//				new MusicCollection(null, "artist"), artist);
	//		startFragment(fragment);
	//
	//		// Then
	//		assertEquals("Library skips over single-element collections",
	//		             new HashSet<LibraryItem>(expected),
	//		             new HashSet<LibraryItem>(fragment.getContents()));
	//	}

	private void startFragment(Fragment fragment) {
		FragmentTestUtil.startFragment(fragment, MockLibraryActivity.class);
	}

}
