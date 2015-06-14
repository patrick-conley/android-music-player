package pconley.vamp.model.test;

import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.model.Playlist;
import pconley.vamp.model.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class PlaylistTest {

	private List<Track> tracks;
	private Playlist playlist;

	@Before
	public void setUp() {
		tracks = new LinkedList<Track>();

		for (int i = 0; i < 3; i++) {
			Track track = new Track.Builder(i, Uri.parse(String.valueOf(i)))
					.build();

			tracks.add(track);
		}

		playlist = new Playlist(tracks);
	}

	@After
	public void tearDown() {
		Playlist.setInstance(null);
	}

	/**
	 * Given the playlist has no tracks, then it is marked as empty.
	 */
	@Test
	public void testEmptyPlaylistSize() {
		playlist = new Playlist();

		assertEquals("Playlist is empty", 0, playlist.size());
		assertEquals("Playlist has 0 tracks.", 0, playlist.size());
	}

	/**
	 * When I add several tracks to the playlist, then its size is correct.
	 */
	@Test
	public void testPlaylistSize() {
		assertFalse("Playlist is empty", playlist.size() == 0);
		assertEquals("Playlist has the correct size", tracks.size(),
				playlist.size());
	}

	/**
	 * When I add several tracks to the playlist, then its iterator returns the
	 * expected output.
	 */
	@Test
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
	 * Given the playlist has been constructed from a list of tracks, when I
	 * remove a track from the list, then the playlist is not modified.
	 */
	@Test
	public void testPlaylistSourceModified() {
		// Given
		playlist = new Playlist(tracks);
		int expected = playlist.size();

		// When
		tracks.remove(0);

		// Then
		assertEquals("Playlist is not changed when its source is", expected,
				playlist.size());
	}

	/**
	 * When I set the global playlist and create a new playlist instance, then I
	 * can recover the original global instance.
	 */
	@Test
	public void testStaticInstanceIsPersistent() {

		// When
		Playlist.setInstance(playlist);
		new Playlist();

		// Then
		assertSame("Global instance can be recovered", playlist, Playlist.getInstance());
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals(Object)} for
	 * contracts.
	 */
	@Test
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
		assertTrue("Equals is correct (x,y)", x.equals(y));
		assertTrue("Equals is correct (x,z)", x.equals(z));
		assertFalse("Equals is correct (x,d)", x.equals(d));

		assertEquals("HashCode is correct (x,y)", x.hashCode(), y.hashCode());
		assertEquals("HashCode is correct (x,z)", x.hashCode(), z.hashCode());

		assertFalse("Different types are not equal", x.equals(list));
		assertFalse("Null is never equal (x,null)", x.equals(null));
		assertFalse("Null is never equal (d,null)", d.equals(null));

		/*
		 * Equals is reflexive
		 */
		assertTrue("Equals is reflexive (x)", x.equals(x));
		assertEquals("HashCode() is consistent (x)", x.hashCode(), x.hashCode());
		assertTrue("Equals is reflexive (d)", d.equals(d));
		assertEquals("HashCode() is consistent (d)", d.hashCode(), d.hashCode());

		/*
		 * Equals is symmetric
		 */
		assertEquals("Equals is symmetric (x,y)", x.equals(y), y.equals(x));
		assertEquals("Equals is symmetric (x,z)", x.equals(z), z.equals(x));
		assertEquals("Equals is symmetric (x,d)", x.equals(d), d.equals(x));

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y,z)", x.equals(z), x.equals(y)
				&& y.equals(z));
		assertEquals("Equals is transitive (x,y,d)", x.equals(d), x.equals(y)
				&& y.equals(d));

	}
}
