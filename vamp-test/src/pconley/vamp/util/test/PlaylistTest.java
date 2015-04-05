package pconley.vamp.util.test;

import static android.test.MoreAsserts.assertEmpty;
import static android.test.MoreAsserts.assertNotEmpty;
import static android.test.MoreAsserts.checkEqualsAndHashCodeMethods;

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

		assertEmpty(playlist);
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
		assertNotEmpty(playlist);
		playlist.clear();
		assertEmpty("Playlist is empty after clearing it", playlist);
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

		checkEqualsAndHashCodeMethods("Different types are not equal", x, list,
				false);
		checkEqualsAndHashCodeMethods("Null is never equal (x,null)", x, null,
				false);
		checkEqualsAndHashCodeMethods("Null is never equal (d,null)", d, null,
				false);

		/*
		 * Equals is reflexive
		 */
		checkEqualsAndHashCodeMethods("Equals is reflexive (x)", x, x, true);
		checkEqualsAndHashCodeMethods("Equals is reflexive (d)", d, d, true);

		/*
		 * Equals is symmetric
		 */
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,y)", x, y, true);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,z)", x, z, true);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,d)", x, d, false);

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y,z)", x.equals(z), x.equals(y)
				&& y.equals(z));
		assertEquals("Equals is transitive (x,y,d)", x.equals(d), x.equals(y)
				&& y.equals(d));

	}
}
