package pconley.vamp.persistence.test;

import android.app.Activity;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import pconley.vamp.persistence.LoadCollectionTask;
import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.library.view.MockLibraryActivity;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LoadCollectionTaskTest {

	private TrackDAO trackDAO;
	private TagDAO tagDAO;
	private MockLibraryAction action;

	@Before
	public void setUp() {
		Activity activity = Robolectric.buildActivity(MockLibraryActivity.class).create()
		                      .start().restart().get();

		action = new MockLibraryAction(activity);

		LibraryOpenHelper helper = new LibraryOpenHelper(activity);
		trackDAO = new TrackDAO(helper);
		tagDAO = new TagDAO(helper);

		Tag artist = new Tag("artist", "artist");

		Track track = new Track.Builder(0, Uri.parse("track1"))
				.add(artist).add(new Tag("album", "album1")).build();
		trackDAO.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("track2"))
				.add(artist).add(new Tag("album", "album2")).build();
		trackDAO.insertTrack(track);
	}

	/**
	 * When I run the task without a caller, then an exception is thrown
	 */
	@Test(expected = IllegalArgumentException.class)
	public void nullCaller() {
		new LoadCollectionTask(null, null, null);
	}

	/**
	 * Given the library contains tags, when I run the task with a name and no
	 * filter, then root tags are returned.
	 */
	@Test
	public void loadUnfilteredTags() {
		MusicCollection expected
				= new MusicCollection("artist", null,
				                      tagDAO.getFilteredTags(null, "artist"));

		// When
		new LoadCollectionTask(action, "artist", null).execute();

		// Then
		assertEquals("Task can load tags without a filter", expected,
		             action.getCollection());
	}

	/**
	 * Given the library contains tags, when I run the task with a name and a
	 * filter, then appropriate tags are returned.
	 */
	@Test
	public void loadFilteredTags() {
		List<Tag> filter = tagDAO.getFilteredTags(null, "artist");
		MusicCollection expected
				= new MusicCollection("album", filter,
				                      tagDAO.getFilteredTags(filter, "album"));

		// When
		new LoadCollectionTask(action, "album", filter).execute();

		// Then
		assertEquals("Task can load tags with a filter", expected,
		             action.getCollection());
	}

	/**
	 * Given the library contains tracks, when I run the task with no name and
	 * no filter, then all tracks are returned.
	 */
	@Test
	public void loadAllTracks() {
		MusicCollection expected
				= new MusicCollection(null, null,
				                      trackDAO.getAllTracks());

		// When
		new LoadCollectionTask(action, null, null).execute();

		// Then
		assertEquals("Task can load tracks without a filter", expected,
		             action.getCollection());
	}

	/**
	 * Given the library contains tracks, when I run the task with a filter and
	 * no name, then appropriate tracks are returned.
	 */
	@Test
	public void loadFilteredTracks() {
		List<Tag> filter = tagDAO.getFilteredTags(null, "album").subList(0, 1);
		MusicCollection expected
				= new MusicCollection(null, filter,
				                      trackDAO.getFilteredTracks(filter));

		// When
		new LoadCollectionTask(action, null, filter).execute();

		// Then
		assertEquals("Task can load tracks with a filter", expected,
		             action.getCollection());
	}

	private class MockLibraryAction extends LibraryAction {

		private MusicCollection collection;

		public MockLibraryAction(Activity activity) {
			super(activity);
		}

		@Override
		public void execute(MusicCollection collection, int position) {
			// Does nothing
		}

		@Override
		public void onLoadCollection(MusicCollection collection) {
			this.collection = collection;
		}

		public MusicCollection getCollection() {
			return collection;
		}
	}
}
