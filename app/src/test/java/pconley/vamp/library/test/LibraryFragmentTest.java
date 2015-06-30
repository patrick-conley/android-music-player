package pconley.vamp.library.test;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import org.robolectric.util.FragmentTestUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import pconley.vamp.library.LibraryFragment;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.MusicCollection;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryFragmentTest {

	private Context context;
	private TrackDAO dao;

	@Before
	public void setUpTest() {
		context = Robolectric.getShadowApplication().getApplicationContext();

		dao = new TrackDAO(context).openWritableDatabase();
	}

	@After
	public void tearDownTest() {
		dao.wipeDatabase();
		dao.close();
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
		             fragment.getCollection().getTags());
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
				new MusicCollection(null, null), filters.get(0));
		startFragment(fragment);

		// Then
		assertEquals("Fragment was created with filters",
		             filters, fragment.getCollection().getTags());
	}

	/**
	 * When I use the filtered factory method with an unset filter, then I get
	 * an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createWithNullTag() throws IllegalArgumentException {
		startFragment(LibraryFragment.newInstance(
				new MusicCollection(null, null), null));
	}

	/**
	 * When I use the filtered factory method with an unset parent collection,
	 * then I get an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createWithNullParent() throws IllegalArgumentException {
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
		             fragment.getContents());
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
		List<Tag> expected = dao.getTags("album");


		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment);

		// Then
		assertEquals("Unfiltered library displays albums",
		             new HashSet<LibraryItem>(expected),
		             new HashSet<LibraryItem>(fragment.getContents()));
	}

	/**
	 * Given the library is not empty, when I start the fully-filtered fragment,
	 * then the correct tracks are displayed.
	 */
	@Test
	public void displayFilteredLibrary() {
		File ogg = new File("sample.ogg");
		File flac = new File("sample.flac");

		// Given
		AssetUtils.addTracksToDb(context, new File[] { ogg, flac });
		List<Track> expected = dao.getTracks();

		ArrayList<Tag> filters
				= new ArrayList<Tag>(expected.get(0).getTags("album"));

		// When
		LibraryFragment fragment = LibraryFragment.newInstance(
				new MusicCollection(null, "album"), filters.get(0));
		startFragment(fragment);

		// Then
		assertEquals("Filtered library displays tracks",
		             new HashSet<LibraryItem>(expected),
		             new HashSet<LibraryItem>(
				             fragment.getContents()));
	}

	private void startFragment(Fragment fragment) {
		FragmentTestUtil.startFragment(fragment, MockActivity.class);

		Robolectric.runBackgroundTasks();
		Robolectric.runUiThreadTasks();
	}

	public static class MockActivity extends Activity
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
