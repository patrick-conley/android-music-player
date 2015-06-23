package pconley.vamp.library.action.test;

import android.net.Uri;
import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.library.action.LibraryActionLocator;
import pconley.vamp.library.action.PlayAction;
import pconley.vamp.library.action.TagFilterAction;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryActionLocatorTest {

	/**
	 * When I look for a handler for a Track, then I get an instance of
	 * PlayAction.
	 */
	@Test
	public void testTrack() {
		// When
		LibraryAction action = LibraryActionLocator
				.findAction(new Track.Builder(0, Uri.parse("/")).build());

		// Then
		assertNotNull("Tracks have a handler", action);
		assertTrue("Track handler is correct", action instanceof PlayAction);
	}

	/**
	 * When I look for a handler for a Tag, then I get in instance of
	 * TagFilterAction.
	 */
	@Test
	public void testTag() {
		// When
		LibraryAction action = LibraryActionLocator
				.findAction(new Tag("foo", "bar"));

		// Then
		assertNotNull("Tags have a handler", action);
		assertTrue("Tag handler is correct", action instanceof TagFilterAction);
	}

	/**
	 * When I look for a handler for a new subclass of LibraryItem, then I get
	 * nothing.
	 */
	@Test
	public void testAnonymous() {
		// When
		LibraryAction action = LibraryActionLocator.findAction(
				new LibraryItem() {
					@Override
					public int describeContents() {
						return 0;
					}

					@Override
					public void writeToParcel(Parcel dest, int flags) {
					}

					@Override
					public long getId() {
						return 0;
					}
				});

		// Then
		assertNull("Anonymous items have no handler", action);
	}
}
