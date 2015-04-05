package pconley.vamp.util.test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import pconley.vamp.model.Track;
import pconley.vamp.util.Playlist;
import pconley.vamp.util.PlaylistIterator;
import android.net.Uri;
import android.test.AndroidTestCase;

public class PlaylistIteratorTest extends AndroidTestCase {

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

	public void setUp() {
		emptyIter = new Playlist().playlistIterator();
		iter = playlist.playlistIterator();

	}

	/**
	 * Given I have an iterator to an empty list, when I try to advance to the
	 * next item, then it throws an exception.
	 */
	public void testNextOnEmptyList() {
		assertFalse("hasNext() is false on empty input", emptyIter.hasNext());

		try {
			emptyIter.next();
		} catch (NoSuchElementException e) {
			return;
		}

		fail("next() fails on empty input");
	}

	/**
	 * Given I have an iterator to an empty list, when I try to reverse to the
	 * previous item, then it throws an exception.
	 */
	public void testPreviousOnEmptyList() {
		assertFalse("hasPrevious() is false on empty input",
				emptyIter.hasPrevious());

		try {
			emptyIter.previous();
		} catch (NoSuchElementException e) {
			return;
		}

		fail("previous() fails on empty input");
	}

	/**
	 * Given I have an iterator to a populated list, when I try to select a
	 * position outside the list, then it throws an exception.
	 */
	public void testSetPositionInvalidIndex() {
		try {
			iter = playlist.playlistIterator(playlist.size());
		} catch (IndexOutOfBoundsException e) {
			return;
		}

		fail("setPosition() fails on invalid input");
	}

	/**
	 * Given I have an iterator to a populated list, when I try to retrieve the
	 * current item before one is available, then it throws an exception.
	 */
	public void testCurrentOutOfRange() {
		try {
			iter.current();
		} catch (IllegalStateException e) {
			return;
		}

		fail("current() fails if next() has not been called");
	}

	/**
	 * Given I have an iterator to a populated list, and it has been advanced to
	 * a valid position, when I try to reverse past the beginning, then it
	 * throws an exception.
	 */
	public void testPreviousAtStart() {
		iter.next();

		assertFalse("Iterator has no previous item", iter.hasPrevious());
		assertTrue("Iterator has a next item", iter.hasNext());

		try {
			iter.previous();
		} catch (IndexOutOfBoundsException e) {
			return;
		}

		fail("previous() fails at the start of the list");
	}

	/**
	 * Given I have an iterator to a populated list, and it has been advanced to
	 * a valid position, when I try to advance past the end, then it throws an
	 * exception.
	 */
	public void testNextAtEnd() {
		iter = playlist.playlistIterator(playlist.size() - 1);
		iter.next(); // Get the last item

		assertTrue("Iterator has a previous item", iter.hasPrevious());
		assertFalse("Iterator has no next item", iter.hasNext());

		try {
			iter.next();
		} catch (IndexOutOfBoundsException e) {
			return;
		}

		fail("previous() fails at the start of the list");
	}

	/**
	 * Given I have an iterator to a populated list, when I advance to the first
	 * item, then it returns the first item.
	 */
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
	public void testSetPositionIsCorrect() {
		for (int i = 0; i < playlist.size(); i++) {
			iter = playlist.playlistIterator(i);

			assertEquals("Item " + String.valueOf(i)
					+ " of iterator is correct", tracks.get(i), iter.next());
		}
	}

	/**
	 * Given I have an iterator to a populated list, and it has been advanced to
	 * a valid position, when I try to remove the current item, then it throws
	 * an exception.
	 */
	public void testRemoveUnsupported() {
		iter.next();

		try {
			iter.remove();
		} catch (UnsupportedOperationException e) {
			return;
		}

		fail("remove() throws an exception.");
	}

}
