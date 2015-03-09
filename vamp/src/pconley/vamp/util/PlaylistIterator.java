package pconley.vamp.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import pconley.vamp.model.Track;

/**
 * An extended {@link Iterator}, specific to {@link Track}s. It adds the concept
 * of a current element, allowing it to return that element repeatedly without
 * advancing the cursor.
 * 
 * @author pconley
 *
 */
public class PlaylistIterator implements Iterator<Track> {

	private List<Track> playlist;
	private int position = -1;

	/**
	 * Constructs a new iterator.
	 * 
	 * @param playlist
	 *            List of tracks to iterate across.
	 */
	protected PlaylistIterator(List<Track> playlist) {
		this.playlist = playlist;
	}

	/**
	 * Constructs a new iterator.
	 * 
	 * @param playlist
	 *            List of tracks to iterate across.
	 * @param position
	 *            Index of the first element to be returned by a call to
	 *            {@link #next()}
	 */
	protected PlaylistIterator(List<Track> playlist, int position) {
		if (position < 0 || position >= playlist.size()) {
			throw new IndexOutOfBoundsException();
		}

		this.playlist = playlist;
		this.position = position - 1;
	}

	/**
	 * @return Whether there are previous elements to iterate
	 */
	public boolean hasPrevious() {
		return position > 0;
	}

	/**
	 * Back up the cursor to the previous track, then return that track.
	 * 
	 * @return The previous track in the list.
	 * @throws NoSuchElementException
	 *             if the list is empty.
	 * @throws IndexOutOfBoundsException
	 *             if the current element is at the beginning of the list.
	 */
	public Track previous() throws NoSuchElementException,
			IndexOutOfBoundsException {
		if (playlist.isEmpty()) {
			throw new NoSuchElementException();
		} else if (position == 0) {
			throw new IndexOutOfBoundsException();
		}

		position--;

		return playlist.get(position);
	}

	/**
	 * @return The track under the cursor. The cursor is not advanced.
	 * @throws IllegalStateException
	 *             if neither {@link #next()} nor {@link #setPosition(int)} has
	 *             been called.
	 */
	public Track current() throws NoSuchElementException {
		if (position == -1) {
			throw new IllegalStateException();
		}

		return playlist.get(position);
	}

	/**
	 * @return Whether there are more elements to iterate
	 */
	public boolean hasNext() {
		return position < playlist.size() - 1;
	}

	/**
	 * Advance the cursor to the next track, then return that track.
	 * 
	 * @return The next track in the list
	 * @throws NoSuchElementException
	 *             if the list is empty.
	 * @throws IndexOutOfBoundsException
	 *             if the current element is at the end of the list.
	 */
	public Track next() throws NoSuchElementException,
			IndexOutOfBoundsException {
		if (playlist.isEmpty()) {
			throw new NoSuchElementException();
		} else if (position == playlist.size() - 1) {
			throw new IndexOutOfBoundsException();
		}

		position++;

		return playlist.get(position);
	}

	/**
	 * @throws UnsupportedOperationException
	 *             whenever called.
	 */
	@Override
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
