package pconley.vamp.library.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.robolectric.util.FragmentTestUtil.startFragment;
import static org.robolectric.util.FragmentTestUtil.startVisibleFragment;

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
	public void testCreateEmptyFragment() {
		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment, LibraryActivity.class);

		// Then
		assertEquals("Fragment was created without filters",
		             Collections.emptyList(), fragment.copyFilters());
	}

	/**
	 * When I use the filtered factory method to make a fragment, then the
	 * fragment has those filters.
	 */
	@Test
	public void testCreateFilteredFragment() {
		ArrayList<Tag> filters = new ArrayList<Tag>();
		filters.add(new Tag(0, "foo", "bar"));

		// When
		LibraryFragment fragment = LibraryFragment.newInstance(filters);
		startFragment(fragment, LibraryActivity.class);

		// Then
		assertEquals("Fragment was created with filters",
		             filters, fragment.copyFilters());
	}

	/**
	 * Given I have created the fragment with filters, when I get & modify the
	 * filters, then the originals are unchanged.
	 */
	@Test
	public void testFiltersAreImmutable() {
		ArrayList<Tag> filters = new ArrayList<Tag>();
		filters.add(new Tag(0, "foo", "bar"));

		LibraryFragment fragment = LibraryFragment.newInstance(filters);
		startFragment(fragment, LibraryActivity.class);

		// When
		ArrayList<Tag> modified = fragment.copyFilters();
		modified.add(new Tag(1, "foob", "barb"));

		// Then
		assertNotEquals("A fragment's filters are copied on Get",
		                fragment.copyFilters(), modified);
	}

	/**
	 * Given the library is empty, when I start the fragment, then no items are
	 * displayed.
	 */
	@Test
	public void testDisplayEmptyLibrary() {
		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment, LibraryActivity.class);

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
	public void testDisplayPopulatedLibrary() {
		File ogg = new File("sample.ogg");
		File flac = new File("sample.flac");

		// Given
		AssetUtils.addTracksToDb(context, new File[] { ogg, flac });
		List<Tag> expected = dao.getTag("album");


		// When
		LibraryFragment fragment = LibraryFragment.newInstance();
		startFragment(fragment, LibraryActivity.class);

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
	public void testDisplayFilteredLibrary() {
		File ogg = new File("sample.ogg");
		File flac = new File("sample.flac");

		// Given
		AssetUtils.addTracksToDb(context, new File[] { ogg, flac });
		List<Track> expected = dao.getTracks();

		ArrayList<Tag> filters
				= new ArrayList<Tag>(expected.get(0).getTags("album"));

		// When
		LibraryFragment fragment = LibraryFragment.newInstance(filters);
		startVisibleFragment(fragment, LibraryActivity.class, R.id.library);

		// Then

		assertEquals("Filtered library displays tracks",
		             new HashSet<LibraryItem>(expected),
		             new HashSet<LibraryItem>(fragment.getContents()));
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
