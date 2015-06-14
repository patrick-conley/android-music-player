package pconley.vamp.model.test;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import pconley.vamp.model.Playlist;
import pconley.vamp.model.PlaylistIterator;
import pconley.vamp.model.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class PlaylistIteratorTest {

	private List<Track> tracks;
	private Playlist playlist;

	private PlaylistIterator iter;
	private PlaylistIterator emptyIter;

	/*
	 * Create a list of simple tracks.
	 */
	public PlaylistIteratorTest() {
		tracks = new ArrayList<Track>();
		playlist = new Playlist();

		for (int i = 0; i < 5; i++) {
			Track track = new Track.Builder(i, Uri.parse(String.valueOf(i)))
					.build();

			tracks.add(track);
			playlist.add(track);
		}
	}

	@Before
	public void setUp() {
		emptyIter = new Playlist().playlistIterator();
		iter = playlist.playlistIterator();

	}

	/**
	 * Given I have an iterator to an empty list, when I advance to the next
	 * item, then it throws an exception.
	 */
	@Test(expected = NoSuchElementException.class)
	public void testNextOnEmptyList() throws NoSuchElementException {
		assertFalse("hasNext() is false on empty input", emptyIter.hasNext());

		emptyIter.next();
	}

	/**
	 * Given I have an iterator to an empty list, when I reverse to the previous
	 * item, then it throws an exception.
	 */
	@Test(expected = NoSuchElementException.class)
	public void testPreviousOnEmptyList() throws NoSuchElementException {
		assertFalse("hasPrevious() is false on empty input",
				emptyIter.hasPrevious());

		emptyIter.previous();
	}

	/**
	 * Given I have an iterator to a populated list, when I select a position
	 * outside the list, then it throws an exception.
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testSetPositionInvalidIndex() throws IndexOutOfBoundsException {
		iter = playlist.playlistIterator(playlist.size());
	}

	/**
	 * Given I have an iterator to a populated list, when I retrieve the current
	 * item before one is available, then it throws an exception.
	 */
	@Test(expected = IllegalStateException.class)
	public void testCurrentOutOfRange() throws IllegalStateException {
		iter.current();
	}

	/**
	 * Given I have an iterator to a populated list, and it has been advanced to
	 * a valid position, when I reverse past the beginning, then it throws an
	 * exception.
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testPreviousAtStart() throws IndexOutOfBoundsException {
		iter.next();

		assertFalse("Iterator has no previous item", iter.hasPrevious());
		assertTrue("Iterator has a next item", iter.hasNext());

		iter.previous();
	}

	/**
	 * Given I have an iterator to a populated list, and it has been advanced to
	 * a valid position, when I advance past the end, then it throws an
	 * exception.
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testNextAtEnd() throws IndexOutOfBoundsException {
		iter = playlist.playlistIterator(playlist.size() - 1);
		iter.next(); // Get the last item

		assertTrue("Iterator has a previous item", iter.hasPrevious());
		assertFalse("Iterator has no next item", iter.hasNext());

		iter.next();
	}

	/**
	 * Given I have an iterator to a populated list, when I advance to the first
	 * item, then it returns the first item.
	 */
	@Test
	public void testNextIsCorrect() {
		for (int i = 0; iter.hasNext(); i++) {
			Track track = iter.next();

			assertEquals("Item " + String.valueOf(i)
					+ " of iterator is correct", tracks.get(i), track);
		}
	}

	/**
	 * Given I have an iterator to a populated list, when I advance within the
	 * list and retrieve the current item, then both methods return the same
	 * item.
	 */
	@Test
	public void testCurrentMatchesNext() {
		for (int i = 0; iter.hasNext(); i++) {
			Track track = iter.next();

			assertEquals("Item " + String.valueOf(i)
					+ " of iterator matches current", track, iter.current());
		}
	}

	/**
	 * Given I have an iterator to a populated list, when I reverse within the
	 * list and retrieve the current item, then both methods return the same
	 * item.
	 */
	@Test
	public void testCurrentMatchesPrevious() {
		iter = playlist.playlistIterator(playlist.size() - 1);

		// The last element isn't checked as I can't go backwards to it.
		for (int i = playlist.size() - 2; iter.hasPrevious(); i--) {
			Track track = iter.previous();

			assertEquals("Item " + String.valueOf(i)
					+ " of iterator is correct", track, iter.current());
		}
	}

	/**
	 * Given I have an iterator to a populated list, when I select a position
	 * within the list and retrieve the current item, then it returns the
	 * correct item.
	 */
	@Test
	public void testSetPositionIsCorrect() {
		for (int i = 0; i < playlist.size(); i++) {
			iter = playlist.playlistIterator(i);

			assertEquals("Item " + String.valueOf(i)
					+ " of iterator is correct", tracks.get(i), iter.next());
		}
	}

	/**
	 * Given I have an iterator to a populated list, and it has been advanced to
	 * a valid position, when I remove the current item, then it throws an
	 * exception.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testRemoveUnsupported() throws UnsupportedOperationException {
		iter.next();

		iter.remove();
		fail("remove() throws an exception.");
	}

}
