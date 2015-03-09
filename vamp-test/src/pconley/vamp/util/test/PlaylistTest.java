package pconley.vamp.util.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.model.Track;
import pconley.vamp.util.Playlist;
import android.net.Uri;
import android.test.AndroidTestCase;

public class PlaylistTest extends AndroidTestCase {

	private List<Track> tracks;
	private Playlist playlist;

	public void setUp() {
		tracks = new LinkedList<Track>();
		playlist = new Playlist();

		for (int i = 0; i < 3; i++) {
			Track track = new Track.Builder(i, Uri.parse(String.valueOf(i)))
					.build();

			tracks.add(track);
			playlist.add(track);
		}
	}

	/**
	 * Given the playlist has no tracks, then it is marked as empty.
	 */
	public void testEmptyPlaylistSize() {
		playlist = new Playlist();

		assertTrue("Playlist is empty.", playlist.isEmpty());
		assertEquals("Playlist has 0 tracks.", 0, playlist.size());
	}

	/**
	 * When I add several tracks to the playlist, then its size is correct.
	 */
	public void testPlaylistSize() {
		assertEquals("Playlist has the correct size", tracks.size(),
				playlist.size());
	}

	/**
	 * When I add several tracks to the playlist, then its iterator returns the
	 * expected output.
	 */
	public void testPlaylistContents() {
		Iterator<Track> iter = playlist.iterator();
		for (Track track : tracks) {
			assertTrue("Playlist iterator isn't missing tracks", iter.hasNext());
			assertEquals("Playlist iterator has the correct track", track,
					iter.next());
		}
		assertFalse("Playlist iterator has no surplus tracks", iter.hasNext());
	}

	/**
	 * Given the playlist has several tracks, when I clear it, then it is marked
	 * empty.
	 */
	public void testClear() {
		assertFalse("Playlist is not empty", playlist.isEmpty());
		playlist.clear();
		assertTrue("Playlist is empty", playlist.isEmpty());
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals()} for contracts.
	 */
	public void testHashCodeEqualsContract() {
		Playlist x = new Playlist();
		Playlist y = new Playlist();
		Playlist z = new Playlist();

		Playlist d = new Playlist();
		List<Track> list = new ArrayList<Track>();

		for (Track track : tracks) {
			x.add(track);
			y.add(track);
			z.add(track);
			list.add(track);
		}

		d.add(tracks.get(0));

		/*
		 * Equals is correct
		 */
		assertEquals("(x,y) are equal", x, y);
		assertEquals("(x,z) are equal", x, z);
		assertEquals("(z,y) are equal", z, y);
		assertFalse("(x,d) are not equal", x.equals(d));
		assertFalse("(y,d) are not equal", y.equals(d));
		assertFalse("(z,d) are not equal", z.equals(d));
		assertFalse("Different types are not equal", x.equals(list));

		assertFalse("Null is never equal (x,null)", x.equals(null));
		assertFalse("Null is never equal (d,null)", d.equals(null));

		/*
		 * Equals is reflexive
		 */
		assertTrue("Equals is reflexive (x)", x.equals(x));
		assertTrue("Equals is reflexive (d)", d.equals(d));
		assertEquals("HashCode is equal when equals is true (x,x)",
				x.hashCode(), x.hashCode());
		assertEquals("HashCode is equal when equals is true (d,d)",
				d.hashCode(), d.hashCode());

		/*
		 * Equals is symmetric
		 */
		assertEquals("Equals is symmetric (x,y)", x.equals(y), y.equals(y));
		assertEquals("Equals is symmetric (x,d)", x.equals(d), d.equals(x));
		assertEquals("HashCode is equal when equals is true (x,y)",
				x.hashCode(), y.hashCode());

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y),(y,z),(x,z)", x.equals(z),
				x.equals(y) && y.equals(z));
		assertEquals("Equals is transitive (x,y),(y,d),(x,d)", x.equals(d),
				x.equals(y) && y.equals(d));
		assertEquals("HashCode is equal when equals is true (x,y)",
				x.hashCode(), y.hashCode());
		assertEquals("HashCode is equal when equals is true (y,z)",
				z.hashCode(), y.hashCode());
		assertEquals("HashCode is equal when equals is true (x,z)",
				x.hashCode(), z.hashCode());

	}
}
