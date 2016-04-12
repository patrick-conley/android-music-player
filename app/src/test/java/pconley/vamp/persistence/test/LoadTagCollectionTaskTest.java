package pconley.vamp.persistence.test;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.List;

import pconley.vamp.library.action.MockLibraryAction;
import pconley.vamp.library.view.MockLibraryActivity;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.LoadTagCollectionTask;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.TagCollection;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LoadTagCollectionTaskTest {

	private MockLibraryAction action;
	private TagDAO tagDAO;
	private TrackDAO trackDAO;

	@Before
	public void setUp() {
		Activity activity = Robolectric.buildActivity(MockLibraryActivity.class)
		                               .create()
		                               .start().restart().get();

		action = new MockLibraryAction(activity, null);

		LibraryOpenHelper helper = new LibraryOpenHelper(activity);
		trackDAO = new TrackDAO(helper);
		tagDAO = new TagDAO(helper);

		AssetUtils.addTrackToDb(activity, new File(AssetUtils.OGG));

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
		new LoadTagCollectionTask(null, null);
	}

	/**
	 * When I call the task's post method, then the method's tags are passed to
	 * the calling action.
	 */
	@Test
	public void testSetCollection() {
		List<Tag> tags = tagDAO.getFilteredTags(null, "album");

		// When
		new LoadTagCollectionTask(action, null).onPostExecute(tags);

		// Then
		assertEquals("Task results are returned to the caller",
		             new TagCollection(null, null, tags),
		             action.getCollection());
	}

	/**
	 * Given the library contains tags, when I run the task with a name and no
	 * filter, then root tags are returned.
	 */
	@Test
	public void loadUnfilteredTags() {
		MusicCollection expected
				= new TagCollection("artist", null,
				                    tagDAO.getFilteredTags(null, "artist"));

		// When
		new LoadTagCollectionTask(action, null).execute("artist");

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
				= new TagCollection("album", filter,
				                    tagDAO.getFilteredTags(filter, "album"));

		// When
		new LoadTagCollectionTask(action, filter).execute("album");

		// Then
		assertEquals("Task can load tags with a filter", expected,
		             action.getCollection());
	}

}
