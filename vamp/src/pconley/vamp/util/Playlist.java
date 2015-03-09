package pconley.vamp.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pconley.vamp.model.Track;

/**
 * A list of tracks. Built around a {@link List}, but with its {@link Iterator}
 * replaced with a {@link PlaylistIterator}.
 *
 * @author pconley
 *
 */
public class Playlist implements Iterable<Track> {

	private List<Track> tracks;

	public Playlist() {
		tracks = new ArrayList<Track>();
	}

	/**
	 * Add a track to the playlist.
	 *
	 * @param track
	 *            The track to add.
	 * @return Whether the track was added (always true).
	 */
	public boolean add(Track track) {
		return tracks.add(track);
	}

	/**
	 * Remove all tracks in the playlist.
	 */
	public void clear() {
		tracks.clear();
	}

	/**
	 * @return Whether the playlist is empty
	 */
	public boolean isEmpty() {
		return tracks.isEmpty();
	}

	/**
	 * @return an {@link Iterator} to the tracks in the playlist.
	 */
	@Override
	public Iterator<Track> iterator() {
		return tracks.iterator();
	}

	/**
	 * @return A {@link PlaylistIterator} to the tracks in the playlist.
	 * @throws IndexOutOfBoundsException
	 *             If the start location is out of bounds.
	 */
	public PlaylistIterator playlistIterator() {
		return new PlaylistIterator(tracks);
	}

	/**
	 * @param location
	 *            Index of the first element to be returned by a call to
	 *            {@link Iterator#next()}.
	 * @return A {@link PlaylistIterator} to the tracks in the playlist.
	 * @throws IndexOutOfBoundsException
	 *             If the start location is out of bounds.
	 */
	public PlaylistIterator playlistIterator(int location)
			throws IndexOutOfBoundsException {
		return new PlaylistIterator(tracks, location);
	}

	/**
	 * @return Number of tracks in the playlist.
	 */
	public int size() {
		return tracks.size();
	}

	@Override
	public int hashCode() {
		return tracks.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Playlist other = (Playlist) obj;
		if (tracks == null) {
			if (other.tracks != null)
				return false;
		} else if (!tracks.equals(other.tracks))
			return false;
		return true;
	}

}
