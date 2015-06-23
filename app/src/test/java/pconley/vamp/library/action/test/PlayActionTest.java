package pconley.vamp.library.action.test;

import android.content.Intent;
import android.widget.ArrayAdapter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.io.File;
import java.util.ArrayList;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.action.PlayAction;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Track;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.player.PlayerService;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class PlayActionTest {

	private LibraryActivity activity;
	private ShadowActivity shadow;

	private static ArrayList<Track> tracks;
	private ArrayAdapter<LibraryItem> adapter;

	@BeforeClass
	public static void setUp() {
		tracks = new ArrayList<Track>(3);
		tracks.add(AssetUtils.buildTrack(new File("sample.ogg")));
		tracks.add(AssetUtils.buildTrack(new File("sample.flac")));
		tracks.add(AssetUtils.buildTrack(new File("sample.mp3")));
	}

	@Before
	public void setUpTest() {
		activity = Robolectric.buildActivity(LibraryActivity.class).create()
		                      .start().restart().get();
		shadow = Robolectric.shadowOf(activity);

		adapter = new ArrayAdapter<LibraryItem>(activity,
		                                        R.layout.fragment_library);
	}

	/**
	 * Given the library has a single track, when I run the action, then the
	 * player service & activity are started.
	 */
	@Test
	public void testPlayerStarted() {
		int position = 0;

		ArrayList<Track> track = new ArrayList<Track>();
		track.add(tracks.get(0));

		// Given
		adapter.addAll(track);

		// When
		new PlayAction().execute(activity, adapter, position);

		// Then
		Intent expected = new Intent(activity, PlayerService.class);
		expected.setAction(PlayerService.ACTION_PLAY)
		        .putExtra(PlayerService.EXTRA_START_POSITION, position)
		        .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS, track);
		assertEquals("Play action begins playing", expected,
		             shadow.getNextStartedService());

		expected = new Intent(activity, PlayerActivity.class);
		assertEquals("Play action opens the player", expected,
		             shadow.getNextStartedActivity());
	}

	/**
	 * Given the library has tracks, when I run the action with an invalid
	 * position, then the player service is started. (The player should perform
	 * validation.)
	 */
	@Test
	public void testInvalidPosition() {
		int position = 3;

		// Given
		adapter.addAll(tracks);

		// When
		new PlayAction().execute(activity, adapter, position);

		// Then
		Intent expected = new Intent(activity, PlayerService.class);
		expected.setAction(PlayerService.ACTION_PLAY)
		        .putExtra(PlayerService.EXTRA_START_POSITION, position)
		        .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS,
		                                     tracks);
		assertEquals("Play action begins playing", expected,
		             shadow.getNextStartedService());
	}

	/**
	 * Given the library has no tracks, when I run the action, then the player
	 * service is started. (The player should perform validation.)
	 */
	@Test
	public void testEmptyPlaylist() {
		int position = 0;

		// When
		new PlayAction().execute(activity, adapter, position);

		// Then
		Intent expected = new Intent(activity, PlayerService.class);
		expected.setAction(PlayerService.ACTION_PLAY)
		        .putExtra(PlayerService.EXTRA_START_POSITION, position)
		        .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS,
		                                     new ArrayList<Track>());
		assertEquals("Play action begins playing", expected,
		             shadow.getNextStartedService());
	}
}
