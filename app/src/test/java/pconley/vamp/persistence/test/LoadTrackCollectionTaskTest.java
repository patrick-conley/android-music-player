package pconley.vamp.persistence.test;

import android.app.Activity;
import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import pconley.vamp.library.action.MockLibraryAction;
import pconley.vamp.library.view.MockLibraryActivity;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.LoadTrackCollectionTask;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.model.TrackCollection;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LoadTrackCollectionTaskTest {

	private TrackDAO trackDAO;
	private TagDAO tagDAO;
	private MockLibraryAction action;

	@Before
	public void setUp() {
		Activity activity = Robolectric.buildActivity(MockLibraryActivity.class)
		                               .create()
		                               .start().restart().get();

		action = new MockLibraryAction(activity, null);

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

	@After
	public void tearDown() {
		trackDAO.wipeDatabase();
	}

	/**
	 * When I run the task without a caller, then an exception is thrown
	 */
	@Test(expected = IllegalArgumentException.class)
	public void nullCaller() {
		new LoadTrackCollectionTask(null, null);
	}

	/**
	 * When I call the task's post method, then the method's tags are passed to
	 * the calling action.
	 */
	@Test
	public void testSetCollection() {
		List<Track> tracks = trackDAO.getAllTracks();

		// When
		new LoadTrackCollectionTask(action, null).onPostExecute(tracks);

		// Then
		assertEquals("Task results are returned to the caller",
		             new TrackCollection(null, tracks), action.getCollection());
	}

	/**
	 * Given the library contains tracks, when I run the task with no name and
	 * no filter, then all tracks are returned.
	 */
	@Test
	public void loadAllTracks() {
		MusicCollection expected
				= new TrackCollection(null, trackDAO.getAllTracks());

		// When
		new LoadTrackCollectionTask(action, null).execute();

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
				= new TrackCollection(filter, trackDAO.getFilteredTracks(
				filter));

		// When
		new LoadTrackCollectionTask(action, filter).execute();

		// Then
		assertEquals("Task can load tracks with a filter", expected,
		             action.getCollection());
	}

}
