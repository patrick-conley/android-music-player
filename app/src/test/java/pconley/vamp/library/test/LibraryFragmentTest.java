package pconley.vamp.library.test;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.FragmentTestUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryFragmentTest {

	private Context context;
	private TrackDAO trackDAO;
	private TagDAO tagDAO;

	@Before
	public void setUpTest() {
		context = Robolectric.getShadowApplication().getApplicationContext();

		LibraryOpenHelper helper = new LibraryOpenHelper(context);
		trackDAO = new TrackDAO(helper);
		tagDAO = new TagDAO(helper);
	}

	@After
	public void tearDownTest() {
		trackDAO.wipeDatabase();
	}

	/**
	 * When I use the no-arg factory method to make a fragment, then the
	 * fragment has no filters.
	 */
	@Test
	public void createEmptyFragment() {
		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment);

		// Then
		assertEquals("Fragment was created without filters",
		             Collections.emptyList(),
		             fragment.getCollection().getFilter());
	}

	/**
	 * When I use the filtered factory method to make a fragment, then the
	 * fragment has those filters.
	 */
	@Test
	public void createFilteredFragment() {
		ArrayList<Tag> filters = new ArrayList<Tag>();
		filters.add(new Tag(0, "foo", "bar"));

		// When
		LibraryFragment fragment = LibraryFragment.newInstance(
				new MusicCollection("artist", filters, null), null);
		startFragment(fragment);

		// Then
		assertEquals("Fragment was created with filters",
		             filters, fragment.getCollection().getFilter());
	}

	/**
	 * When I used the filtered factory method with an unset parent name, then I
	 * get an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createWithNullParentName() throws IllegalArgumentException {
		startFragment(LibraryFragment.newInstance(
				new MusicCollection(null, new LinkedList<Tag>(), null), null));
	}

	/**
	 * When I use the filtered factory method with an unset parent collection,
	 * then I don't get an exception.
	 */
	public void createWithNullParent() {
		startFragment(LibraryFragment.newInstance(null, new Tag("foo", "bar")));
	}

	/**
	 * Given the library is empty, when I start the fragment, then no items are
	 * displayed.
	 */
	@Test
	public void displayEmptyLibrary() {
		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment);

		// Then
		assertEquals("Fragment in an empty library displays nothing",
		             Collections.emptyList(),
		             fragment.getCollection().getContents());
	}

	/**
	 * Given the library is not empty, when I start the unfiltered fragment,
	 * then the correct tags are displayed.
	 */
	@Test
	public void displayPopulatedLibrary() {
		File ogg = new File("sample.ogg");
		File flac = new File("sample.flac");

		// Given
		AssetUtils.addTracksToDb(context, new File[] { ogg, flac });
		List<Tag> expected = tagDAO.getFilteredTags(null, "artist");


		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment);

		// Then
		assertEquals("Unfiltered library displays artists",
		             new HashSet<LibraryItem>(expected),
		             new HashSet<LibraryItem>(
				             fragment.getCollection().getContents()));
	}

	/**
	 * Given the library is not empty, when I start the fully-filtered fragment,
	 * then the correct tracks are displayed.
	 */
	@Test
	public void displayFilteredLibrary() {

		// Given
		AssetUtils.addTracksToDb(context, new File[] { new File("sample.ogg"),
				new File("sample.flac") });
		List<Track> expected = trackDAO.getAllTracks();

		ArrayList<Tag> filters = new ArrayList<Tag>();
		filters.add(expected.get(0).getTags("artist").get(0));
		filters.add(expected.get(0).getTags("album").get(0));

		// When
		LibraryFragment fragment = LibraryFragment.newInstance(
				new MusicCollection("album", filters.subList(0, 1), null),
				filters.get(1));
		startFragment(fragment);

		// Then
		assertEquals("Filtered library displays tracks",
		             new HashSet<LibraryItem>(expected),
		             new HashSet<LibraryItem>(
				             fragment.getCollection().getContents()));
	}

	/**
	 * Given the library is not empty, when I click the Play All button, then
	 * the PlayerActivity is launched
	 */
	@Test
	public void testClickPlayContents() {

		// Given
		AssetUtils.addTracksToDb(context, new File[] { new File("sample.ogg"),
				new File("sample.flac") });

		LibraryFragment fragment = LibraryFragment.newInstance();
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

		Robolectric.runBackgroundTasks();
		Robolectric.runUiThreadTasks();
	}

	public static class MockLibraryActivity extends LibraryActivity
			implements AdapterView.OnItemClickListener {

		public int count = 0;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		protected void onStart() {
			super.onStart();
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			count++;
		}
	}

}
