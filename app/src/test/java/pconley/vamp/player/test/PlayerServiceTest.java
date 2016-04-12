package pconley.vamp.player.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAudioManager;
import org.robolectric.shadows.ShadowMediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import pconley.vamp.R;
import pconley.vamp.persistence.model.TagCollection;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.model.TrackCollection;
import pconley.vamp.player.PlayerEvent;
import pconley.vamp.player.PlayerFactory;
import pconley.vamp.player.PlayerService;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.BroadcastConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class PlayerServiceTest {

	private static BroadcastReceiver receiver;
	private static String latestBroadcastMessage;
	private static List<PlayerEvent> broadcastEvents;
	private static List<PlayerEvent> normalStartEvents;

	private Context context;
	private Intent serviceIntent;

	private PlayerService service;
	private static PlayerFactory factory;
	private ShadowMediaPlayer player;
	private ShadowAudioManager audioManager;

	private File musicFolder;

	private ArrayList<Track> tracks;
	private ArrayList<Track> missing;

	@BeforeClass
	public static void setUp() {
		receiver = new PlayerReceiver();

		broadcastEvents = new LinkedList<PlayerEvent>();
		normalStartEvents = new LinkedList<PlayerEvent>();
		normalStartEvents.add(PlayerEvent.NEW_TRACK);
		normalStartEvents.add(PlayerEvent.PLAY);

		factory = mock(PlayerFactory.class);
		PlayerFactory.setInstance(factory);

	}

	@Before
	public void setUpTest() throws IOException {
		context = Robolectric.getShadowApplication().getApplicationContext();

		serviceIntent = new Intent(context, PlayerService.class);

		audioManager = Robolectric.shadowOf((AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE));

		IntentFilter filter = new IntentFilter(
				BroadcastConstants.FILTER_PLAYER_EVENT);
		LocalBroadcastManager.getInstance(context)
		                     .registerReceiver(receiver, filter);

		// Set up and move files
		musicFolder = AssetUtils.setupMusicFolder(context);
		File ogg = new File(musicFolder, "sample.ogg");
		File flac = new File(musicFolder, "sample.flac");
		File missingFile = new File(musicFolder, "missing.mp3");

		tracks = new ArrayList<Track>();
		tracks.add(AssetUtils.addAssetToFolder(context,
		                                       AssetUtils.ROBO_ASSET_PATH +
		                                       AssetUtils.OGG, ogg));
		tracks.add(AssetUtils.addAssetToFolder(context,
		                                       AssetUtils.ROBO_ASSET_PATH +
		                                       AssetUtils.FLAC, flac));

		missing = new ArrayList<Track>();
		missing.add(AssetUtils.buildTrack(missingFile));

		// Shadow the service's MediaPlayer
		MediaPlayer mp = new MediaPlayer();
		when(factory.createMediaPlayer()).thenReturn(mp);
		player = Robolectric.shadowOf(mp);
	}

	@After
	public void tearDownTest() throws IOException {
		LocalBroadcastManager.getInstance(context).unregisterReceiver
				(receiver);

		if (service != null) {
			service.stop();
		}

		FileUtils.deleteDirectory(musicFolder);

		latestBroadcastMessage = null;
		broadcastEvents.clear();
	}

	@AfterClass
	public static void tearDown() {
		PlayerFactory.resetInstance();
	}

	/**
	 * Given the service is not running, when I bind to it, then it is not in
	 * the Playing state and it has no current track/position/duration.
	 */
	@Test
	public void testBind() {
		// When
		bindService();

		// Then
		assertPlayerState("Un-started service is not playing", false,
		                  new LinkedList<PlayerEvent>(), null, false);
	}

	/**
	 * Given the service is not running and I am bound to it, when I try to
	 * pause, then it returns false.
	 */
	@Test
	public void testBindCantPause() {
		// Given
		bindService();

		// When/Then
		assertFalse("Un-started service can't be paused", service.pause());
	}

	/**
	 * Given the service is not running and I am bound to it, when I try to
	 * play, then it returns false.
	 */
	@Test
	public void testBindCantPlay() {
		// Given
		bindService();

		// When/Then
		assertFalse("Un-started service can't play", service.play());
	}

	/**
	 * Given the service is not running and I am bound to it, when I go to the
	 * next track, then it returns false.
	 */
	@Test
	public void testBindCantGoToNext() {
		// Given
		bindService();

		// When/Then
		assertFalse("Un-started service can't go to the next track",
		            service.next());
	}

	/**
	 * Given the service is not running and I am bound to it, when I go to the
	 * previous track, then it returns false.
	 */
	@Test
	public void testBindCantGoToPrevious() {
		// Given
		bindService();

		// When/Then
		assertFalse("Un-started service can't go to the previous track",
		            service.previous());
	}

	/**
	 * Given the service is not running and I am bound to it, when I seek, then
	 * it returns false.
	 */
	@Test
	public void testBindCantSeek() {
		// Given
		bindService();

		// When/Then
		assertFalse("Un-started service can't seek", service.seekTo(0));
	}

	/**
	 * When I start the service with an empty intent, then it does not broadcast
	 * an event.
	 */
	@Test
	public void testEmptyStartIntent() throws InterruptedException {

		// When
		startService();

		// Then
		assertTrue("Empty start intent sends no broadcasts",
		           broadcastEvents.isEmpty());
	}

	/**
	 * Given the service is not running, when I start it with a PAUSE action,
	 * then it does not broadcast an event.
	 */
	@Test
	public void testStartWithPauseIntent() throws InterruptedException {
		serviceIntent.setAction(PlayerService.ACTION_PAUSE);

		// When
		startService();

		// Then
		assertTrue("Pause start intent sends no broadcasts when not playing",
		           broadcastEvents.isEmpty());
	}

	/**
	 * When I start the service with a PLAY action and without any tracks, then
	 * it fails to start.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testStartWithPlayIntentWithoutTracks()
			throws InterruptedException {
		serviceIntent.setAction(PlayerService.ACTION_PLAY);

		// When
		startService();
	}

	/**
	 * When I start the service with a collection of tags, then an exception is
	 * thrown.
	 */
	@Test(expected = ClassCastException.class)
	public void testStartWithTagCollection() {
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_COLLECTION,
		                       new TagCollection(null, null, null));

		// When
		startService();
	}

	/**
	 * When I start the service with a PLAY action and an empty list of track
	 * IDs, then it fails to start.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testPlayIntentWithZeroTracks() throws InterruptedException {
		serviceIntent.setAction(PlayerService.ACTION_PLAY)
		             .putExtra(PlayerService.EXTRA_COLLECTION,
		                       new TrackCollection(null, null));

		// When
		startService();
	}

	/**
	 * Given there is a track in the database but not on disk, when I start the
	 * service with a PLAY action and that track, then it broadcasts an error.
	 */
	@Test
	public void testPlayIntentWithMissingTrack() throws Exception {

		// Given
		serviceIntent.setAction(PlayerService.ACTION_PLAY)
		             .putExtra(PlayerService.EXTRA_COLLECTION,
		                       new TrackCollection(null, missing));

		// When
		startService();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player stops when sent a missing track", false,
		                  events, null, false);

		assertTrue("Player can't read missing tracks", Pattern.matches(
				context.getString(R.string.player_error_read, "\\S*"),
				latestBroadcastMessage));
	}

	/**
	 * Given the database contains a track and the playlist start position is
	 * invalid, when I start the service with a PLAY action, then it fails to
	 * start.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testPlayIntentWithInvalidPosition()
			throws InterruptedException, IOException {

		// Given
		prepareService(1);
		serviceIntent.putExtra(PlayerService.EXTRA_START_POSITION, 2);

		// When
		startService();
	}

	/**
	 * Given the database contains a track and I can obtain audio focus, when I
	 * start the service with a PLAY action, then it is in the Playing state and
	 * it has the correct track/position/duration and it broadcasts the new
	 * track and play events.
	 */
	@Test
	public void testCanObtainAudioFocus() {
		// Given
		ArrayList<Track> single = new ArrayList<Track>();
		single.add(tracks.get(0));
		serviceIntent.setAction(PlayerService.ACTION_PLAY)
		             .putExtra(PlayerService.EXTRA_COLLECTION,
		                       new TrackCollection(null, single));

		audioManager.setNextFocusRequestResponse(
				AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

		// When
		startService();

		// Then
		assertPlayerState("Player plays when it can focus", true,
		                  normalStartEvents, tracks.get(0), true);
	}

	/**
	 * Given the database contains a track and I cannot obtain audio focus, when
	 * I start the service with a PLAY action, then it not in the Playing state
	 * and it has the correct track/position/duration and it broadcasts the new
	 * track and pause events.
	 */
	@Test
	public void testCantObtainAudioFocus() {
		// Given
		ArrayList<Track> single = new ArrayList<Track>();
		single.add(tracks.get(0));
		serviceIntent.setAction(PlayerService.ACTION_PLAY)
		             .putExtra(PlayerService.EXTRA_COLLECTION,
		                       new TrackCollection(null, single));

		audioManager.setNextFocusRequestResponse(
				AudioManager.AUDIOFOCUS_REQUEST_FAILED);

		// When
		startService();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.NEW_TRACK);
		events.add(PlayerEvent.PAUSE);
		assertPlayerState("Player doesn't play when it can't focus", false,
		                  events, tracks.get(0), true);
	}

	/**
	 * Given the service is playing, when I pause it, then it is not in the
	 * Playing state and it has the correct track and it has the
	 * correct/position/duration and it broadcasts the pause event.
	 */
	@Test
	public void testPauseWhilePlaying() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.pause();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PAUSE);
		assertPlayerState("Player pauses on demand", false, events,
		                  tracks.get(0), true);
	}

	/**
	 * Given the service is playing, when I start it with a PAUSE action, then
	 * it is not in the Playing state and it has the correct
	 * track/position/duration and it broadcasts a pause event.
	 */
	@Test
	public void testPauseIntentWhilePlaying() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		Intent pauseIntent = new Intent(context, PlayerService.class);
		pauseIntent.setAction(PlayerService.ACTION_PAUSE);

		service.onStartCommand(pauseIntent, 0, 1);

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PAUSE);
		assertPlayerState("Player pauses from intents", false, events,
		                  tracks.get(0), true);
	}

	/**
	 * Given the service is playing, when I play, then it returns true and does
	 * not broadcast an event.
	 */
	@Test
	public void testPlayWhilePlaying() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		boolean status = service.play();

		// Then
		assertPlayerState("Play does nothing when playing", true,
		                  new LinkedList<PlayerEvent>(), tracks.get(0), true);
		assertTrue("Player can play while already playing", status);
	}

	/**
	 * Given the service is playing, when I start the service with a PLAY
	 * action, then it is in the Playing state etc.
	 */
	@Test
	public void testPlayIntentWhilePlaying() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		prepareService(1);
		startService();

		// Then
		assertPlayerState("Player can be restarted with a new intent", true,
		                  normalStartEvents, tracks.get(0), true);
	}

	/**
	 * Given the player is playing, when I send a PLAY action and a distinct
	 * playlist, then it plays the new track.
	 */
	@Test
	public void testPlayIntentWithNewPlaylist() {
		// Given
		prepareService(1);
		startService();

		assertTrue("The service is playing", service.isPlaying());

		Track initial = service.getCurrentTrack();

		// When
		ArrayList<Track> newTracks = new ArrayList<Track>();
		newTracks.add(tracks.get(1));

		serviceIntent.putExtra(
				PlayerService.EXTRA_COLLECTION,
				new TrackCollection(null, newTracks));
		audioManager.setNextFocusRequestResponse(
				AudioManager.AUDIOFOCUS_REQUEST_FAILED);
		startService();

		// Then
		assertNotEquals("Playing track is changed by a new playlist", initial,
		                service.getCurrentTrack());
	}

	/**
	 * Given the service is paused, when I pause, then it returns true and does
	 * not broadcast an event.
	 */
	@Test
	public void testPauseWhilePaused() {
		// Given
		prepareService(1);
		startService();
		service.pause();
		broadcastEvents.clear();

		assertFalse("The service is paused", service.isPlaying());

		// When
		boolean status = service.pause();

		// Then
		assertPlayerState("Pause does nothing when paused", false,
		                  new LinkedList<PlayerEvent>(), tracks.get(0), true);
		assertTrue("Player can pause from pause", status);
	}

	/**
	 * Given the service is paused, when I start it with a PAUSE action, then it
	 * does not broadcast an event.
	 */
	@Test
	public void testPauseIntentWhilePaused() {
		// Given
		prepareService(1);
		startService();
		service.pause();
		broadcastEvents.clear();

		assertFalse("The service is playing", service.isPlaying());

		// When
		Intent pauseIntent = new Intent(context, PlayerService.class);
		pauseIntent.setAction(PlayerService.ACTION_PAUSE);

		service.onStartCommand(pauseIntent, 0, 1);

		// Then
		assertPlayerState("Pause intent does nothing when already paused",
		                  false, new LinkedList<PlayerEvent>(), tracks.get(0),
		                  true);
	}

	/**
	 * Given the service is paused, when I play, then it is in the Playing state
	 * and it broadcasts the play event.
	 */
	@Test
	public void testPlayWhilePaused() {
		// Given
		prepareService(1);
		startService();
		service.pause();
		broadcastEvents.clear();

		audioManager.setNextFocusRequestResponse(
				AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

		assertFalse("The service is paused", service.isPlaying());

		// When
		boolean status = service.play();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PLAY);
		assertPlayerState("Player plays from pause", true, events,
		                  tracks.get(0), true);
		assertTrue("Player can play from pause", status);
	}

	/**
	 * Given the service is playing, when I start the service with a PLAY
	 * action, then it is in the Playing state etc.
	 */
	@Test
	public void testPlayIntentWhilePaused() {
		// Given
		prepareService(1);
		startService();
		service.pause();
		broadcastEvents.clear();

		// When
		prepareService(1);
		startService();

		// Then
		assertPlayerState(
				"Player can be restarted with a new intent while paused", true,
				normalStartEvents, tracks.get(0), true);
	}

	/**
	 * Given the service is playing a single track, when I go to the next track,
	 * then it is not in the Playing state and it has no current
	 * track/position/duration and it broadcasts a stop event.
	 */
	@Test
	public void testNextOnSingleTrack() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.next();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player stops when there is no next track", false,
		                  events, null, false);
	}

	/**
	 * Given the service is playing a single track, when I go to the previous
	 * track, then it is in the Playing state and it has the same current
	 * track.
	 */
	@Test
	public void testPreviousOnSingleTrack() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		player.setCurrentPosition(0);
		service.previous();

		// Then
		LinkedList<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PLAY);
		assertPlayerState("Player restarts first track on previous", true,
		                  events, tracks.get(0), true);
	}

	/**
	 * Given the service is playing a single track and the position is above the
	 * restart limit, when I go to the previous track, then it is in the Playing
	 * state and it has the same current track.
	 */
	@Test
	public void testRestartTrack() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		player.setCurrentPosition(
				(PlayerService.PREV_RESTART_LIMIT + 1) * 1000);
		service.previous();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PLAY);
		assertPlayerState("Player can restart a track", true, events,
		                  tracks.get(0), true);
	}

	/**
	 * Given the service is playing a single track, when I seek, then it is in
	 * the Playing state and it returns true.
	 */
	@Test
	public void testSeekWhilePlaying() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		boolean status = service.seekTo(1000);

		// Then
		assertTrue("Player can seek", status);

		assertPlayerState("Player seeks within a track", true,
		                  new LinkedList<PlayerEvent>(), tracks.get(0), true);
	}

	/**
	 * Given the service is paused in a single track, when I seek, then it is in
	 * the Paused state and it returns true.
	 */
	@Test
	public void testSeekWhilePaused() {
		// Given
		prepareService(1);
		startService();
		service.pause();
		broadcastEvents.clear();

		assertFalse("The service is paused", service.isPlaying());

		// When
		boolean status = service.seekTo(1000);

		// Then
		assertTrue("Player can seek while paused", status);

		assertPlayerState("Player seeks within a track when paused", false,
		                  new LinkedList<PlayerEvent>(), tracks.get(0), true);
	}

	/**
	 * Given the service is playing a single track, when an error occurs, then
	 * it is not in the Playing state etc. and it broadcasts a stop event
	 * containing the correct error codes.
	 */
	@Test
	public void testOnError() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.onError(null, 7, 13);
		String errorString
				= context.getString(R.string.player_error_MediaPlayer, 7, 13);

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player stops when an error occurs", false, events,
		                  null, false);

		assertTrue("Player sends the appropriate error message",
		           Pattern.matches(errorString, latestBroadcastMessage));
	}

	/**
	 * Given the service is playing a single track, when the track ends, then it
	 * is not in the Playing state etc. and it broadcasts a stop event.
	 */
	@Test
	public void testOnCompletion() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.onCompletion(null);

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player stops when finished", false, events, null,
		                  false);
	}

	/**
	 * Given the service is playing a single track, when it loses audio focus
	 * temporarily, then it is not in the Playing state and it has the correct
	 * track and it broadcasts a pause event.
	 */
	@Test
	public void testAudioFocusLossTransient() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PAUSE);
		assertPlayerState("Player pauses on temporary focus loss", false,
		                  events, tracks.get(0), true);
	}

	/**
	 * Given the service is playing a single track, when it loses audio focus
	 * permanently, then it is not in the Playing state and it has no current
	 * track and it broadcasts a stop event.
	 */
	@Test
	public void testAudioFocusLoss() {
		// Given
		prepareService(1);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player pauses on temporary focus loss", false,
		                  events, null, false);

		assertEquals("Player sends the appropriate error message",
		             context.getString(R.string.player_focus_lost),
		             latestBroadcastMessage);
	}

	/**
	 * Given the service is playing a single track and it has lost audio focus
	 * temporarily, when it regains audio focus, then it is in the Playing state
	 * etc. and broadcasts a play event.
	 */
	@Test
	public void testAudioFocusGainAfterLoss() {
		// Given
		prepareService(1);
		startService();
		service.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
		broadcastEvents.clear();

		assertFalse("The service is not playing", service.isPlaying());

		// When
		service.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PLAY);
		assertPlayerState("Player resumes playing on audio focus gain", true,
		                  events, tracks.get(0), true);
	}

	/**
	 * Given the service is paused in a single track and it has not lost audio
	 * focus, when it regains audio focus, then it is in the Paused state and
	 * does not send any events.
	 */
	@Test
	public void testAudioFocusGain() {
		// Given
		prepareService(1);
		startService();
		service.pause();
		broadcastEvents.clear();

		assertFalse("The service is not playing", service.isPlaying());

		// When
		service.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);

		// Then
		assertPlayerState(
				"Player doesn't play on focus gain if it never lost focus",
				false, new LinkedList<PlayerEvent>(), tracks.get(0), true);
	}

	/**
	 * When I start the service with a PLAY action, two valid tracks, and no
	 * playlist position, then it is in the Playing state and the first track is
	 * current.
	 */
	@Test
	public void testPlayTwoTracks() {
		// Given
		prepareService(2);

		// When
		startService();

		// Then
		assertPlayerState("Player can play two tracks", true,
		                  normalStartEvents, tracks.get(0), true);
	}

	/**
	 * When I start the service with a PLAY action, two valid tracks, and a
	 * pointer to the second track, then it is in the Playing state and the
	 * second track is current.
	 */
	@Test
	public void testPlayTwoStartAtSecondTrack() {
		// Given
		prepareService(2);

		// When
		serviceIntent.putExtra(PlayerService.EXTRA_START_POSITION, 1);
		startService();

		// Then
		assertPlayerState("Player can start from the second track", true,
		                  normalStartEvents, tracks.get(1), true);
	}

	/**
	 * Given the service's playlist contains two valid tracks and it is playing
	 * the first, when I go to the next track, then it is in the Playing state
	 * and the second track is current and it broadcasts new track and play
	 * events.
	 */
	@Test
	public void testNextWithTwoTracks() {
		// Given
		prepareService(2);
		startService();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		service.next();

		// Then
		assertPlayerState("Player moves to the next track", true,
		                  normalStartEvents, tracks.get(1), true);
	}

	/**
	 * Given the service's playlist contains two valid tracks and it is playing
	 * the second and it has not reached the restart limit, when I go to the
	 * previous track, then it is in the Playing state and the first track is
	 * current and it broadcasts new track and play events.
	 */
	@Test
	public void testPreviousWithTwoTracks() {
		// Given
		prepareService(2);
		startService();
		service.next();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		player.setCurrentPosition(0);
		service.previous();

		// Then
		assertPlayerState("Player moves to the previous track", true,
		                  normalStartEvents, tracks.get(0), true);
	}

	/**
	 * Given the service's playlist contains two valid tracks and it is playing
	 * the second and it has passed the restart limit, when I go to the previous
	 * track, then it is in the Playing state and the second track is current
	 * and it broadcasts nothing.
	 */
	@Test
	public void testRestartWithTwoTracks() {
		// Given
		prepareService(2);
		startService();
		service.next();
		broadcastEvents.clear();

		assertTrue("The service is playing", service.isPlaying());

		// When
		player.setCurrentPosition((PlayerService.PREV_RESTART_LIMIT + 1) *
		                          1000);
		service.previous();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PLAY);
		assertPlayerState("Player can restart track when there are two", true,
		                  events, tracks.get(1), true);
	}

	/**
	 * Given the service's playlist contains two valid tracks and it is paused
	 * in the first track, when I go to the next track, then it is in the Paused
	 * state and the second track is current and it broadcasts a new track
	 * event.
	 */
	@Test
	public void testNextWhilePaused() {
		// Given
		prepareService(2);
		startService();
		service.pause();
		broadcastEvents.clear();

		assertFalse("The service is not playing", service.isPlaying());

		// When
		service.next();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.NEW_TRACK);
		assertPlayerState("Player can move to next while paused", false,
		                  events, tracks.get(1), true);
	}

	/**
	 * Given the service's playlist contains two valid tracks and it is paused
	 * in the second track and it has not reached the restart limit, when I go
	 * to the previous track, then it is in the Paused state and the first track
	 * is current and it broadcasts a new track event.
	 */
	@Test
	public void testPreviousWhilePaused() {
		// Given
		prepareService(2);
		startService();
		service.pause();
		service.next();
		broadcastEvents.clear();

		assertFalse("The service is not playing", service.isPlaying());

		// When
		player.setCurrentPosition(0);
		service.previous();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.NEW_TRACK);
		assertPlayerState("Player can move to previous track when paused",
		                  false, events, tracks.get(0), true);
	}

	/**
	 * Given the service's playlist contains two valid tracks and it is paused
	 * in the second track and it has passed the restart limit, when I go to the
	 * previous track, then it is in the Paused state and the second track is
	 * current and it broadcasts nothing.
	 */
	@Test
	public void testRestartWhilePaused() {
		// Given
		prepareService(2);
		startService();
		service.pause();
		service.next();
		broadcastEvents.clear();

		assertFalse("The service is not playing", service.isPlaying());

		// When
		player.setCurrentPosition((PlayerService.PREV_RESTART_LIMIT + 1) *
		                          1000);
		service.previous();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.NEW_TRACK);
		assertPlayerState("Player can restart a track while paused", false,
		                  events, tracks.get(1), true);
	}

	/**
	 * When I start the service with an invalid track followed by a valid track,
	 * then it is in the Playing state and the second track is current and it
	 * broadcasts a single new track and play event.
	 */
	@Test
	public void testInvalidFirstTrack() {
		// Given
		prepareService(1);

		ArrayList<Track> trackList = new ArrayList<Track>();
		trackList.add(missing.get(0));
		trackList.add(tracks.get(0));

		serviceIntent.putExtra(
				PlayerService.EXTRA_COLLECTION,
				new TrackCollection(null, trackList));

		// When
		startService();

		// Then
		assertPlayerState("Player skips invalid tracks", true,
		                  normalStartEvents, tracks.get(0), true);
	}

	/**
	 * Start the PlayerService with the prepared intent
	 *
	 * @return The service
	 */
	private void startService() {
		service = Robolectric.buildService(PlayerService.class)
		                     .withIntent(serviceIntent).create()
		                     .startCommand(0, 0).get();
	}

	/**
	 * Bind to the service without starting it
	 *
	 * @return The service
	 */
	private void bindService() {
		service = Robolectric.buildService(PlayerService.class)
		                     .withIntent(serviceIntent).create().bind().get();
	}

	/**
	 * Set up the library and allow the player to gain audio focus.
	 *
	 * @param nTracks
	 * 		Number of tracks to put in the playlist
	 */
	private void prepareService(int nTracks) {
		ArrayList<Track> preparedTracks = new ArrayList<Track>(nTracks);
		for (int i = 0; i < nTracks; i++) {
			preparedTracks.add(tracks.get(i));
		}

		serviceIntent.setAction(PlayerService.ACTION_PLAY)
		             .putExtra(PlayerService.EXTRA_COLLECTION,
		                       new TrackCollection(null, preparedTracks));
		audioManager.setNextFocusRequestResponse(
				AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	private void assertPlayerState(String message, boolean isPlaying,
			List<PlayerEvent> events, Track track,
			boolean areTimesValid) {
		if (track != null) {
			assertEquals("Current track is set", track,
			             service.getCurrentTrack());
		} else {
			assertNull("Player has no current track",
			           service.getCurrentTrack());
		}

		assertEquals(message, events, broadcastEvents);

		if (isPlaying) {
			assertTrue("Player is playing", service.isPlaying());
		} else {
			assertFalse("Player is not playing", service.isPlaying());
		}

		if (areTimesValid) {
			assertTrue("Position is valid", service.getPosition() >= 0);
			assertTrue("Duration is valid", service.getDuration() >= 0);
		} else {
			assertEquals("Position is not set", -1, service.getPosition());
			assertEquals("Duration is not set", -1, service.getDuration());
		}
	}

	private static class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			broadcastEvents.add((PlayerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT));
			latestBroadcastMessage = intent
					.getStringExtra(BroadcastConstants.EXTRA_MESSAGE);
		}
	}
}
