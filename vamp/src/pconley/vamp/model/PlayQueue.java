package pconley.vamp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A collection of tracks, supporting the operations needed for a music player.
 * A PlayQueue is similar to a List plus ListIterator, but can return the
 * current element without advancing the cursor.
 * 
 * @author pconley
 */
public class PlayQueue implements ListIterator<Track> {

	private List<Track> contents;
	private int position;

	/**
	 * Constructs a new, empty PlayQueue.
	 */
	public PlayQueue() {
		contents = new ArrayList<Track>();
		position = 0;
	}

	/**
	 * Adds the specified track to the list.
	 * 
	 * @param track
	 *            the track to add
	 */
	public void add(Track track) {
		contents.add(track);
	}

	/**
	 * Set the current track.
	 * 
	 * @param location
	 *            New position in the list.
	 * @throws IndexOutOfBoundsException
	 *             if {@code location < 0 || location >= size}
	 */
	public void position(int location) throws IndexOutOfBoundsException {
		if (location < 0 || location > contents.size()) {
			throw new IndexOutOfBoundsException();
		}

		position = location;
	}

	/**
	 * @return Whether there are more elements to iterate
	 */
	public boolean hasNext() {
		return position < contents.size();
	}

	/**
	 * @return Whether there are previous elements to iterate
	 */
	public boolean hasPrevious() {
		return position > 0;
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
		if (contents.isEmpty()) {
			throw new NoSuchElementException();
		} else if (position == contents.size() - 1) {
			throw new IndexOutOfBoundsException();
		}

		position++;

		return contents.get(position);
	}

	/**
	 * @return The track under the cursor. The cursor is not advanced.
	 * @throws NoSuchElementException
	 *             if the PlayQueue is empty.
	 */
	public Track current() throws NoSuchElementException {
		if (contents.isEmpty()) {
			throw new NoSuchElementException();
		}

		return contents.get(position);
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
		if (contents.isEmpty()) {
			throw new NoSuchElementException();
		} else if (position == 0) {
			throw new IndexOutOfBoundsException();
		}

		position--;

		return contents.get(position);
	}

	/**
	 * @see java.util.ListIterator#nextIndex()
	 */
	@Override
	public int nextIndex() {
		return position + 1;
	}

	/**
	 * @see java.util.ListIterator#previousIndex()
	 */
	@Override
	public int previousIndex() {
		return position - 1;
	}

	/**
	 * Unimplemented
	 */
	@Override
	public void remove() throws IndexOutOfBoundsException {
	}

	/**
	 * Unimplemented
	 */
	@Override
	public void set(Track object) {
	}

}
