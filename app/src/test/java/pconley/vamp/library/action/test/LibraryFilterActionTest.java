package pconley.vamp.library.action.test;

import android.app.FragmentManager;
import android.net.Uri;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.library.action.LibraryFilterAction;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryFilterActionTest {

	private LibraryActivity activity;
	private FragmentManager fm;

	private TrackDAO dao;

	private ArrayAdapter<LibraryItem> adapter;

	@Before
	public void setUpTest() {
		activity = Robolectric.buildActivity(LibraryActivity.class).create()
		                      .start().restart().get();
		fm = activity.getFragmentManager();

		dao = new TrackDAO(activity).openWritableDatabase();

		adapter = new ArrayAdapter<LibraryItem>(activity,
		                                        R.layout.fragment_library);
	}

	@After
	public void tearDownTest() {
		dao.wipeDatabase();
		dao.close();
	}

	/**
	 * Given the library contains a single item, when I run the action, then an
	 * Album filter is added.
	 */
	@Test
	public void testSingleItem() {
		// Given
		Tag album = new Tag("album", "foo");
		adapter.add(album);

		// When
		new LibraryFilterAction().execute(activity, adapter, 0);

		// Then
		List<Tag> filters
				= ((LibraryFragment) fm.findFragmentById(R.id.library))
				.getCollection().getHistory();

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
		adapter.add(album);

		Set<Track> expected = new HashSet<Track>();
		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album)
				.add(new Tag("title", "foo"))
				.build();
		expected.add(track);
		insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album)
				.add(new Tag("title", "bar"))
				.build();
		expected.add(track);
		insertTrack(track);

		track = new Track.Builder(0, Uri.parse("three"))
				.add(album)
				.add(new Tag("title", "baz"))
				.build();
		expected.add(track);
		insertTrack(track);

		activity = Robolectric.buildActivity(LibraryActivity.class).create()
		                      .start().restart().get();

		// When
		new LibraryFilterAction().execute(activity, adapter, 0);

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
		// Given
		Tag album1 = new Tag("album", "foo");
		adapter.add(album1);

		Tag album2 = new Tag("album", "bar");
		adapter.add(album2);

		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album1)
				.add(new Tag("title", "foo"))
				.build();
		insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album1)
				.add(new Tag("title", "bar"))
				.build();
		insertTrack(track);

		Track expected = new Track.Builder(0, Uri.parse("three"))
				.add(album2)
				.add(new Tag("title", "baz"))
				.build();
		insertTrack(expected);

		// When
		new LibraryFilterAction().execute(activity, adapter, 1);

		// Then
		Adapter fragmentAdapter = ((ListView) activity.findViewById(
				R.id.library_contents)).getAdapter();

		assertEquals("Adapter contains tracks", 1, fragmentAdapter.getCount());
		assertEquals("Tracks in the album are displayed", expected,
		             fragmentAdapter.getItem(0));

	}

	private void insertTrack(Track track) {
		long trackId = dao.insertTrack(track.getUri());

		for (String name : track.getTagNames()) {
			for (Tag tag : track.getTags(name)) {
				dao.insertTag(trackId, tag);
			}
		}

	}

}
