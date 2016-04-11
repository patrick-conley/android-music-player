package pconley.vamp.player.test;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ServiceController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pconley.vamp.R;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.player.PlayerService;
import pconley.vamp.player.view.PlayerActivity;
import pconley.vamp.util.AssetUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class PlayerActivityTest {

	private Context context;
	private Intent serviceIntent;
	private ServiceController<PlayerService> controller;

	private PlayerActivity activity;
	private PlayerService service;
	private ShadowActivity shadowActivity;

	private ArrayList<Track> tracks;

	@Before
	public void setUpTest() throws IOException {
		context = Robolectric.getShadowApplication().getApplicationContext();

		// Copy the tracks to the music folder so the MediaPlayer can read them
		File musicFolder = AssetUtils.setupMusicFolder(context);
		File oggFile = new File(musicFolder, "sample.ogg");
		File flacFile = new File(musicFolder, "sample.flac");

		tracks = new ArrayList<Track>();
		tracks.add(AssetUtils.addAssetToFolder(context,
		                                       AssetUtils.ROBO_ASSET_PATH +
		                                       AssetUtils.OGG, oggFile));
		tracks.add(AssetUtils.addAssetToFolder(context,
		                                       AssetUtils.ROBO_ASSET_PATH +
		                                       AssetUtils.FLAC, flacFile));

		// Default intent to start the player
		serviceIntent = new Intent(context, PlayerService.class);
		serviceIntent.setAction(PlayerService.ACTION_PLAY);
		serviceIntent.putExtra(PlayerService.EXTRA_COLLECTION,
		                       new MusicCollection(null, null, tracks));

		// Instance of the player service to be returned on bind
		controller = Robolectric.buildService(PlayerService.class).create();
		service = controller.get();

		PlayerService.PlayerBinder binder = mock(
				PlayerService.PlayerBinder.class);
		when(binder.getService()).thenReturn(service);

		Robolectric.getShadowApplication()
		           .setComponentNameAndServiceForBindService(null, binder);
	}

	/**
	 * When I start the activity, then it tries to bind to the player service.
	 */
	@Test
	public void testActivityBindsServiceOnStart() {

		// When
		startActivity();

		// Then
		assertEquals("The player activity binds to the player service",
		             new Intent(activity, PlayerService.class),
		             shadowActivity.getNextStartedService());
	}

	/**
	 * Given the player service is not running, when I start the activity, then
	 * no track details are displayed and position/duration are null.
	 */
	@Test
	public void testNoDetailsWhenNothingPlaying() {

		// Given
		serviceIntent.setAction(null);

		// When
		startActivity();

		// Then
		TextView uriView = (TextView) activity
				.findViewById(R.id.player_view_uri);
		assertEquals("Track URI is visible", View.VISIBLE,
		             uriView.getVisibility());
		assertEquals("Track URI is correct", "", uriView.getText());

		TextView tagView = (TextView) activity
				.findViewById(R.id.player_view_tags);
		assertEquals("Track URI is visible", View.VISIBLE,
		             tagView.getVisibility());
		assertEquals("Track URI is correct", "", tagView.getText());

		TextView positionView = (TextView) activity
				.findViewById(R.id.player_view_position);
		assertEquals("Track URI is visible", View.VISIBLE,
		             positionView.getVisibility());
		assertEquals("Track URI is correct",
		             context.getString(R.string.player_blank_time),
		             positionView.getText());

		TextView durationView = (TextView) activity
				.findViewById(R.id.player_view_duration);
		assertEquals("Track URI is visible", View.VISIBLE,
		             durationView.getVisibility());
		assertEquals("Track URI is correct",
		             context.getString(R.string.player_blank_time),
		             durationView.getText());

	}

	/**
	 * Given the player service is running, when I start the activity, then
	 * details about the current track are displayed and position/duration are
	 * set.
	 *
	 * @throws IOException
	 */
	@Test
	public void testDetailsWhenTrackPlaying() throws IOException {

		// Given
		// Nothing to do (setUp prepares the service to play)

		// When
		startActivity();

		assertTrue(service.isPlaying());

		// Then
		TextView uriView = (TextView) activity
				.findViewById(R.id.player_view_uri);
		assertEquals("Track URI is visible", View.VISIBLE,
		             uriView.getVisibility());
		assertEquals("Track URI is correct", tracks.get(0).getUri().toString(),
		             uriView.getText());

		// Then
		TextView detailView = (TextView) activity
				.findViewById(R.id.player_view_tags);
		assertEquals("Track tags are visible", View.VISIBLE,
		             detailView.getVisibility());
		assertEquals("Track tags are correct", tracks.get(0).tagsToString(),
		             detailView.getText());

		TextView positionView = (TextView) activity
				.findViewById(R.id.player_view_position);
		assertEquals("Track position is visible", View.VISIBLE,
		             positionView.getVisibility());
		assertNotEquals("Track position is correct",
		                context.getString(R.string.player_blank_time),
		                positionView.getText());

		TextView durationView = (TextView) activity
				.findViewById(R.id.player_view_duration);
		assertEquals("Track duration is visible", View.VISIBLE,
		             durationView.getVisibility());
		assertNotEquals("Track duration is correct",
		                context.getString(R.string.player_blank_time),
		                durationView.getText());
	}

	/**
	 * Given the player is playing, when I click play/pause, then the player is
	 * paused.
	 */
	@Test
	public void testPlayPauseButtonPauses() {

		// Given
		startActivity();

		assertTrue("Player is initially playing", service.isPlaying());

		// When
		activity.findViewById(R.id.player_button_playpause).performClick();

		// Then
		assertFalse("Clicking play/pause pauses the player",
		            service.isPlaying());
	}

	/**
	 * Given the player is paused, when I click play/pause, then the player is
	 * playing.
	 */
	@Test
	public void testPlayPauseButtonPlays() {
		// Given
		startActivity();
		service.pause();

		assertFalse("Player is initially paused", service.isPlaying());

		// When
		activity.findViewById(R.id.player_button_playpause).performClick();

		// Then
		assertTrue("Clicking play/pauses plays the player",
		           service.isPlaying());
	}

	/**
	 * Given the player is playing the first track of a playlist, when I click
	 * next, then the player plays the next track and details are updated.
	 */
	@Test
	public void testNextButtonPlaysNext() {

		// Given
		startActivity();

		// When
		activity.findViewById(R.id.player_button_next).performClick();

		// Then
		TextView uriView = (TextView) activity
				.findViewById(R.id.player_view_uri);
		assertEquals("Track URI is visible", View.VISIBLE,
		             uriView.getVisibility());
		assertEquals("Track URI is correct", tracks.get(1).getUri().toString(),
		             uriView.getText());

		// Then
		TextView detailView = (TextView) activity
				.findViewById(R.id.player_view_tags);
		assertEquals("Track tags are visible", View.VISIBLE,
		             detailView.getVisibility());
		assertEquals("Track tags are correct", tracks.get(1).tagsToString(),
		             detailView.getText());
	}

	/**
	 * Given the player is paused in the first track of a playlist, when I click
	 * next, then the player is paused in the next track and times are redrawn.
	 */
	@Test
	public void testNextButtonDrawsTimesWhenPaused() {
		CharSequence unexpected = "invalid";

		// Given
		startActivity();
		service.pause();

		TextView positionView = (TextView) activity
				.findViewById(R.id.player_view_position);
		positionView.setText(unexpected);

		TextView durationView = (TextView) activity
				.findViewById(R.id.player_view_duration);
		durationView.setText(unexpected);

		// When
		activity.findViewById(R.id.player_button_next).performClick();

		// Then
		assertNotEquals("Track position is correct", unexpected,
		                positionView.getText());
		assertNotEquals("Track duration is correct", unexpected,
		                durationView.getText());

	}

	/**
	 * Given the player is playing the second track of a playlist, when I click
	 * previous, then the player plays the previous track and details are
	 * updated.
	 */
	@Test
	public void testPreviousButtonPlaysPrevious() {
		// Given
		serviceIntent.putExtra(PlayerService.EXTRA_START_POSITION, 1);
		startActivity();

		// When
		activity.findViewById(R.id.player_button_prev).performClick();

		// Then
		TextView uriView = (TextView) activity
				.findViewById(R.id.player_view_uri);
		assertEquals("Track URI is visible", View.VISIBLE,
		             uriView.getVisibility());
		assertEquals("Track URI is correct", tracks.get(0).getUri().toString(),
		             uriView.getText());

		// Then
		TextView detailView = (TextView) activity
				.findViewById(R.id.player_view_tags);
		assertEquals("Track tags are visible", View.VISIBLE,
		             detailView.getVisibility());
		assertEquals("Track tags are correct", tracks.get(0).tagsToString(),
		             detailView.getText());
	}

	/**
	 * Given the player the last track of a playlist, when the track completes,
	 * then the activity closes.
	 */
	@Test
	public void testOnCompletionWithoutMoreTracks() {
		// Given
		serviceIntent.putExtra(PlayerService.EXTRA_START_POSITION, 1);
		startActivity();

		// When
		service.onCompletion(null);

		// Then
		assertTrue("Activity is closed by completed track",
		           shadowActivity.isFinishing());

	}

	/**
	 * Given the player is playing the first track of a playlist, when the track
	 * completes, then the player plays the next track.
	 */
	@Test
	public void testOnCompletionWithMoreTracks() {
		// Given
		startActivity();

		// When
		service.onCompletion(null);

		// Then
		TextView uriView = (TextView) activity
				.findViewById(R.id.player_view_uri);
		assertEquals("Track URI is visible", View.VISIBLE,
		             uriView.getVisibility());
		assertEquals("Track URI is correct", tracks.get(1).getUri().toString(),
		             uriView.getText());

		// Then
		TextView detailView = (TextView) activity
				.findViewById(R.id.player_view_tags);
		assertEquals("Track tags are visible", View.VISIBLE,
		             detailView.getVisibility());
		assertEquals("Track tags are correct", tracks.get(1).tagsToString(),
		             detailView.getText());
	}

	/**
	 * Given the player is paused, when I adjust the seek bar, then the times
	 * are updated (but duration is not changed).
	 */
	@Test
	public void testSeekingUpdatesTimes() {
		CharSequence unexpected = "invalid";

		// Given
		startActivity();
		service.pause(); // countdown could be advancing if not paused.

		SeekBar seekBar = (SeekBar) activity.findViewById(R.id.player_seek);
		OnSeekBarChangeListener onChangeListener = Robolectric
				.shadowOf(seekBar).getOnSeekBarChangeListener();

		TextView positionView = (TextView) activity
				.findViewById(R.id.player_view_position);
		positionView.setText(unexpected);

		TextView durationView = (TextView) activity
				.findViewById(R.id.player_view_duration);
		CharSequence originalDuration = durationView.getText();

		// When
		onChangeListener.onProgressChanged(null, -2, true);

		// Then
		assertNotEquals("Positions are updated after seek", unexpected,
		                positionView.getText());
		assertEquals("Durations are not updated after seek", originalDuration,
		             durationView.getText());
	}

	private void startActivity() {
		controller.withIntent(serviceIntent).startCommand(0, 0).get();

		activity = Robolectric.buildActivity(PlayerActivity.class).create()
		                      .start().resume().visible().get();
		shadowActivity = Robolectric.shadowOf(activity);
	}

}
