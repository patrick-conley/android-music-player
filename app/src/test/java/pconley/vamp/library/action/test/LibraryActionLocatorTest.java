package pconley.vamp.library.action.test;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.library.action.LibraryActionLocator;
import pconley.vamp.library.action.LibraryFilterAction;
import pconley.vamp.library.action.LibraryPlayAction;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.TagCollection;
import pconley.vamp.persistence.model.TrackCollection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryActionLocatorTest {

	/**
	 * When I look for a handler for a Track, then I get an instance of
	 * LibraryPlayAction.
	 */
	@Test
	public void testTrack() {
		// When
		LibraryAction action = new LibraryActionLocator(null)
				.findAction(new TrackCollection(null, null));

		// Then
		assertNotNull("Tracks have a handler", action);
		assertTrue("Track handler is correct",
		           action instanceof LibraryPlayAction);
	}

	/**
	 * When I look for a handler for a Tag, then I get in instance of
	 * LibraryFilterAction.
	 */
	@Test
	public void testTag() {
		// When
		LibraryAction action = new LibraryActionLocator(null)
				.findAction(new TagCollection(null, null, null));

		// Then
		assertNotNull("Tags have a handler", action);
		assertTrue("Tag handler is correct",
		           action instanceof LibraryFilterAction);
	}

	/**
	 * When I look for a handler for a new subclass of LibraryItem, then I get
	 * nothing.
	 */
	@Test
	public void testAnonymous() {
		// When
		LibraryAction action = new LibraryActionLocator(null).findAction(
				new MusicCollection() {
					@Override
					public List<Tag> getFilter() {
						return null;
					}

					@Override
					public List<? extends LibraryItem> getContents() {
						return null;
					}

					@Override
					public int describeContents() {
						return 0;
					}

					@Override
					public void writeToParcel(Parcel dest, int flags) {

					}
				});

		// Then
		assertNull("Anonymous items have no handler", action);
	}
}
