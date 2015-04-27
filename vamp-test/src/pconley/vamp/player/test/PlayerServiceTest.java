package pconley.vamp.player.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.library.model.Track;
import pconley.vamp.player.PlayerEvent;
import pconley.vamp.player.PlayerFactory;
import pconley.vamp.player.PlayerService;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.BroadcastConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "../vamp/AndroidManifest.xml")
public class PlayerServiceTest {

	// Robolectric gets its assets relative to vamp/assets/
	private static final String ASSET_PATH = "../../vamp-test/assets/";

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
	private File ogg;
	private File flac;
	private File missing;
	private long missingId;

	private static long[] trackIds;
	private Track[] tracks;

	@BeforeClass
	public static void setUp() {
		receiver = new PlayerReceiver();

		broadcastEvents = new LinkedList<PlayerEvent>();
		normalStartEvents = new LinkedList<PlayerEvent>();
		normalStartEvents.add(PlayerEvent.NEW_TRACK);
		normalStartEvents.add(PlayerEvent.PLAY);

		factory = mock(PlayerFactory.class);
		PlayerFactory.setInstance(factory);

		trackIds = new long[] { 0, 2 };
	}

	@Before
	public void setUpTest() throws IOException {
		context = Robolectric.getShadowApplication().getApplicationContext();

		serviceIntent = new Intent(context, PlayerService.class);

		audioManager = Robolectric.shadowOf((AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE));

		LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
				new IntentFilter(BroadcastConstants.FILTER_PLAYER_EVENT));

		// Set up and move files
		musicFolder = AssetUtils.setupMusicFolder(context);
		ogg = new File(musicFolder, "sample.ogg");
		flac = new File(musicFolder, "sample.flac");
		missing = new File(musicFolder, "missing.mp3");
		missingId = 5;

		tracks = new Track[3];
		tracks[0] = AssetUtils.addAssetToFolder(context, ASSET_PATH
				+ AssetUtils.OGG, ogg);
		tracks[1] = AssetUtils.addAssetToFolder(context, ASSET_PATH
				+ AssetUtils.FLAC, flac);
		tracks[2] = AssetUtils.getTrack(missing);

		// Shadow the service's MediaPlayer
		MediaPlayer mp = new MediaPlayer();
		when(factory.createMediaPlayer()).thenReturn(mp);
		player = Robolectric.shadowOf(mp);

		// Mock the database
		TrackDAO dao = mock(TrackDAO.class);
		when(factory.createDAO()).thenReturn(dao);
		when(dao.getTrack(trackIds[0])).thenReturn(tracks[0]);
		when(dao.getTrack(trackIds[1])).thenReturn(tracks[1]);
		when(dao.getTrack(missingId)).thenReturn(tracks[2]);
	}

	@After
	public void tearDownTest() throws IOException {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);

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
		assertPlayerState("Unstarted service is not playing", false,
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
		assertFalse("Unstarted service can't be paused", service.pause());
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
		assertFalse("Unstarted service can't play", service.play());
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
		assertFalse("Unstarted service can't go to the next track",
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
		assertFalse("Unstarted service can't go to the previous track",
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
		assertFalse("Unstarted service can't seek", service.seekTo(0));
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
	 * When I start the service with a PLAY action and an empty list of track
	 * IDs, then it fails to start.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testPlayIntentWithZeroTracks() throws InterruptedException {
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_TRACKS, new long[] {});

		// When
		startService();
	}

	/**
	 * When I start the service with a PLAY action and an invalid track ID, then
	 * it broadcasts an error.
	 */
	@Test
	public void testPlayIntentWithInvalidTrackId() throws InterruptedException {
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_TRACKS, new long[] { 3 });

		// When
		startService();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player stops when sent invalid track IDs", false,
				events, null, false);

		assertEquals("Playlist is illegal",
				context.getString(R.string.player_error_invalid_playlist),
				latestBroadcastMessage);
	}

	/**
	 * Given there is a track in the database but not on disk, when I start the
	 * service with a PLAY action and that track, then it broadcasts an error.
	 */
	@Test
	public void testPlayIntentWithMissingTrack() throws Exception {

		// Given
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_TRACKS,
				new long[] { missingId });

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
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_TRACKS,
				new long[] { trackIds[0] });

		audioManager
				.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

		// When
		startService();

		// Then
		assertPlayerState("Player plays when it can focus", true,
				normalStartEvents, tracks[0], true);
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
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_TRACKS,
				new long[] { trackIds[0] });

		audioManager
				.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

		// When
		startService();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.NEW_TRACK);
		events.add(PlayerEvent.PAUSE);
		assertPlayerState("Player doesn't play when it can't focus", false,
				events, tracks[0], true);
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
		assertPlayerState("Player pauses on demand", false, events, tracks[0],
				true);
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
				tracks[0], true);
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
				new LinkedList<PlayerEvent>(), tracks[0], true);
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
				normalStartEvents, tracks[0], true);
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
				new LinkedList<PlayerEvent>(), tracks[0], true);
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
				false, new LinkedList<PlayerEvent>(), tracks[0], true);
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

		audioManager
				.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

		assertFalse("The service is paused", service.isPlaying());

		// When
		boolean status = service.play();

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.PLAY);
		assertPlayerState("Player plays from pause", true, events, tracks[0],
				true);
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
				normalStartEvents, tracks[0], true);
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
	 * track, then it is in the Playing state and it has the same current track;
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
		assertPlayerState("Player restarts first track on previous", true,
				new LinkedList<PlayerEvent>(), tracks[0], true);
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
		player.setCurrentPosition((PlayerService.PREV_RESTART_LIMIT + 1) * 1000);
		service.previous();

		// Then
		assertPlayerState("Player can restart a track", true,
				new LinkedList<PlayerEvent>(), tracks[0], true);
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
				new LinkedList<PlayerEvent>(), tracks[0], true);
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
				new LinkedList<PlayerEvent>(), tracks[0], true);
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

		// Then
		List<PlayerEvent> events = new LinkedList<PlayerEvent>();
		events.add(PlayerEvent.STOP);
		assertPlayerState("Player stops when an error occurs", false, events,
				null, false);

		assertTrue("Player sends the appropriate error message",
				Pattern.matches(context.getString(
						R.string.player_error_MediaPlayer, 7, 13),
						latestBroadcastMessage));
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
				events, tracks[0], true);
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
				events, tracks[0], true);
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
				false, new LinkedList<PlayerEvent>(), tracks[0], true);
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
				normalStartEvents, tracks[0], true);
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
				normalStartEvents, tracks[1], true);
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
				normalStartEvents, tracks[1], true);
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
				normalStartEvents, tracks[0], true);
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
		player.setCurrentPosition((PlayerService.PREV_RESTART_LIMIT + 1) * 1000);
		service.previous();

		// Then
		assertPlayerState("Player can restart track when there are two", true,
				new LinkedList<PlayerEvent>(), tracks[1], true);
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
				events, tracks[1], true);
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
				false, events, tracks[0], true);
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
		player.setCurrentPosition((PlayerService.PREV_RESTART_LIMIT + 1) * 1000);
		service.previous();

		// Then
		assertPlayerState("Player can restart a track while paused", false,
				new LinkedList<PlayerEvent>(), tracks[1], true);
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

		long[] ids = new long[2];
		ids[0] = missingId;
		ids[1] = trackIds[0];
		serviceIntent.putExtra(PlayerService.EXTRA_TRACKS, ids);

		// When
		startService();

		// Then
		assertPlayerState("Player skips invalid tracks", true,
				normalStartEvents, tracks[0], true);
	}

	/**
	 * Start the PlayerService with the prepared intent
	 * 
	 * @return The service
	 */
	private void startService() {
		service = Robolectric.buildService(PlayerService.class)
				.withIntent(serviceIntent).create().startCommand(0, 0).get();
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
	 *            Tracks for {@link #setUpLibrary(int)}
	 * @return Tracks from {@link #setUpLibrary(int)}
	 */
	private void prepareService(int nTracks) {

		serviceIntent.setAction(PlayerService.ACTION_PLAY);

		if (nTracks == 1) {
			serviceIntent.putExtra(PlayerService.EXTRA_TRACKS,
					new long[] { trackIds[0] });
		} else {
			serviceIntent.putExtra(PlayerService.EXTRA_TRACKS, trackIds);
		}

		audioManager
				.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	private void assertPlayerState(String message, boolean isPlaying,
			List<PlayerEvent> events, Track track, boolean areTimesValid) {
		if (track != null) {
			assertEquals("Current track is set", track,
					service.getCurrentTrack());
		} else {
			assertNull("Player has no current track", service.getCurrentTrack());
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
