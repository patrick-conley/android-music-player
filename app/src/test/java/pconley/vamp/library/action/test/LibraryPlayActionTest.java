package pconley.vamp.library.action.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

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
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.library.action.LibraryPlayAction;
import pconley.vamp.library.view.MockLibraryActivity;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.player.PlayerService;
import pconley.vamp.player.view.PlayerActivity;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryPlayActionTest {

	private Activity activity;
	private ShadowActivity shadow;

	private static ArrayList<Track> tracks;

	@BeforeClass
	public static void setUp() {
		tracks = new ArrayList<Track>(3);
		tracks.add(AssetUtils.buildTrack(new File("sample.ogg")));
		tracks.add(AssetUtils.buildTrack(new File("sample.flac")));
		tracks.add(AssetUtils.buildTrack(new File("sample.mp3")));
	}

	@Before
	public void setUpTest() {
		activity = Robolectric.buildActivity(MockLibraryActivity.class).create()
		                      .start().restart().get();
		shadow = Robolectric.shadowOf(activity);
	}

	/**
	 * Given the library has a single track, when I run the action, then the
	 * player service & activity are started.
	 */
	@Test
	public void testPlayerStarted() {
		int position = 0;

		// Given
		ArrayList<Track> singleTrack = new ArrayList<Track>();
		singleTrack.add(tracks.get(0));

		// When
		new LibraryPlayAction(activity).execute(
				new MusicCollection(null, null, singleTrack), position);

		// Then
		Intent expected = new Intent(activity, PlayerService.class);
		expected.setAction(PlayerService.ACTION_PLAY)
		        .putExtra(PlayerService.EXTRA_START_POSITION, position)
		        .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS,
		                                     singleTrack);
		assertEquals("Play action begins playing", expected,
		             shadow.getNextStartedService());

		expected = new Intent(activity, PlayerActivity.class);
		assertEquals("Play action opens the player", expected,
		             shadow.getNextStartedActivity());
	}

	/**
	 * Given the library has tracks, when I run the action with a valid
	 * position, then the player service is started. (The player should perform
	 * validation.)
	 */
	@Test
	public void testValidPosition() {
		int position = 1;

		// When
		new LibraryPlayAction(activity).execute(
				new MusicCollection(null, null, tracks), position);

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
	 * Given the library has tracks, when I run the action with an invalid
	 * position, then the player service is started. (The player should perform
	 * validation.)
	 */
	@Test
	public void testInvalidPosition() {
		int position = 3;

		// When
		new LibraryPlayAction(activity).execute(
				new MusicCollection(null, null, tracks), position);

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
		new LibraryPlayAction(activity).execute(
				new MusicCollection(null, null, new ArrayList<Track>()),
				position);

		// Then
		Intent expected = new Intent(activity, PlayerService.class);
		expected.setAction(PlayerService.ACTION_PLAY)
		        .putExtra(PlayerService.EXTRA_START_POSITION, position)
		        .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS,
		                                     new ArrayList<Track>());
		assertEquals("Play action begins playing", expected,
		             shadow.getNextStartedService());
	}

	/**
	 * Given a library with tracks, when I run the action on a tag in those
	 * tracks, then the tracks are loaded.
	 */
	@Test
	public void clickPlayContentsWithTags() {
		TrackDAO trackDAO = new TrackDAO(new LibraryOpenHelper(activity));

		// Given
		Tag album = new Tag("album", "foo");
		ArrayList<Tag> contents = new ArrayList<>();
		contents.add(album);

		List<Track> expected = new LinkedList<Track>();
		Track track = new Track.Builder(0, Uri.parse("one"))
				.add(album)
				.add(new Tag("title", "foo"))
				.build();
		expected.add(track);
		trackDAO.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("two"))
				.add(album)
				.add(new Tag("title", "bar"))
				.build();
		expected.add(track);
		trackDAO.insertTrack(track);

		track = new Track.Builder(0, Uri.parse("three"))
				.add(album)
				.add(new Tag("title", "baz"))
				.build();
		expected.add(track);
		trackDAO.insertTrack(track);

		// When
		new LibraryPlayAction(activity)
				.execute(new MusicCollection("album", null, contents), 0);
		Robolectric.runBackgroundTasks();
		Robolectric.runUiThreadTasks();

		// Then
		assertEquals("Play All loads tracks", expected,
		             shadow.peekNextStartedService()
		                   .getParcelableArrayListExtra(
				                   PlayerService.EXTRA_TRACKS));
	}

}
