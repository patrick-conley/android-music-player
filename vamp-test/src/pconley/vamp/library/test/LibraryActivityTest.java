package pconley.vamp.library.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.tester.android.view.TestMenuItem;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.library.model.Track;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.player.PlayerService;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.scanner.ScannerProgressDialogFragment;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Playlist;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "../vamp/AndroidManifest.xml")
public class LibraryActivityTest {

	private LibraryActivity activity;
	private ShadowActivity shadowActivity;

	private Context context;

	private static File ogg;
	private static File flac;

	@BeforeClass
	public static void setUp() {
		ogg = new File("sample.ogg");
		flac = new File("sample.flac");
	}

	@Before
	public void setUpTest() {
		context = Robolectric.getShadowApplication().getApplicationContext();

	}

	@After
	public void tearDownTest() {
		TrackDAO dao = new TrackDAO(context).openWritableDatabase();
		dao.wipeDatabase();
		dao.close();
	}

	/**
	 * Given the library is empty, when I start the activity, then no tracks are
	 * displayed.
	 */
	@Test
	public void testStartWithEmptyLibrary() {
		// Given
		// Nothing to do

		// When
		startActivity();

		// Then
		ListView trackListView = (ListView) activity
				.findViewById(R.id.library_view_tracks);
		assertEquals("Track list is showing", View.VISIBLE,
				trackListView.getVisibility());
		assertEquals("Track list is empty", 0, trackListView.getAdapter()
				.getCount());
	}

	/**
	 * Given the library is not empty, when I start the activity, then the
	 * correct tracks are displayed.
	 */
	@Test
	public void testStartWithLibrary() {
		// Given
		AssetUtils.addTracksToDb(context, new File[] { ogg, flac });

		// When
		startActivity();

		// Then
		ListView trackListView = (ListView) activity
				.findViewById(R.id.library_view_tracks);
		Adapter adapter = ((ListView) activity
				.findViewById(R.id.library_view_tracks)).getAdapter();

		List<Track> tracks = new LinkedList<Track>();
		tracks.add(AssetUtils.getTrack(ogg));
		tracks.add(AssetUtils.getTrack(flac));

		assertEquals("Track list is showing", View.VISIBLE,
				trackListView.getVisibility());
		assertEquals("Track list contains the right number of tracks",
				tracks.size(), adapter.getCount());

		List<Track> actual = new LinkedList<Track>();
		actual.add((Track) adapter.getItem(0));
		actual.add((Track) adapter.getItem(1));

		assertEquals("Track list contains the contents of the DB", tracks,
				actual);

		assertEquals("Global playlist contains the DB contents", new Playlist(
				tracks), Playlist.getInstance());
	}

	/**
	 * Given the activity is running, when I click "Player", then the Player
	 * activity is launched without an action.
	 */
	@Test
	public void testPlayerLaunchedOnClick() {
		// Given
		startActivity();

		// When
		activity.onOptionsItemSelected(new TestMenuItem(R.id.action_player));

		// Then
		Intent expected = new Intent(activity, PlayerActivity.class);
		assertEquals("Clicking \"Player\" opens the player", expected,
				shadowActivity.getNextStartedActivity());
	}

	/**
	 * Given the activity is running, when I click "Settings", then the Settings
	 * activity is launched
	 */
	@Test
	public void testSettingsLaunchedOnClick() {
		// Given
		startActivity();

		// When
		activity.onOptionsItemSelected(new TestMenuItem(R.id.action_settings));

		// Then
		Intent expected = new Intent(activity, SettingsActivity.class);
		assertEquals("Clicking \"Settings\" opens the settings", expected,
				shadowActivity.getNextStartedActivity());
	}

	/**
	 * Given the activity is running, when I click "Rebuild library", then the
	 * ScannerService is launched and a dialog is displayed.
	 */
	@Test
	public void testScannerLaunchedOnClick() {
		// Given
		startActivity();

		// When
		activity.onOptionsItemSelected(new TestMenuItem(R.id.action_rescan));

		// Then
		Intent expected = new Intent(activity, ScannerService.class);
		assertEquals("Clicking \"Rebuild Library\" launches a scanner",
				expected, shadowActivity.getNextStartedService());

		assertNotNull(
				"Progress dialog is started with the scanner",
				activity.getFragmentManager().findFragmentByTag(
						ScannerProgressDialogFragment.TAG));
	}

	/**
	 * Given the activity is running and the library is not empty, when I click
	 * on a track, then the player (service and activity) is started at the
	 * clicked track.
	 */
	@Test
	public void testClickTrackLaunchesPlayer() {
		int item = 1;

		// Given
		AssetUtils.addTracksToDb(context, new File[] { ogg, flac });

		startActivity();

		// When
		ListView trackListView = (ListView) activity
				.findViewById(R.id.library_view_tracks);
		trackListView.performItemClick(
				trackListView.getAdapter().getView(item, null, null), item,
				item);

		// Then
		Intent expected = new Intent(activity, PlayerService.class);
		expected.putExtra(PlayerService.EXTRA_START_POSITION, item);
		expected.setAction(PlayerService.ACTION_PLAY);
		assertEquals("Clicking a track launches the player service", expected,
				shadowActivity.getNextStartedService());

		expected = new Intent(activity, PlayerActivity.class);
		assertEquals("Clicking a track opens the player", expected,
				shadowActivity.getNextStartedActivity());
	}

	private void startActivity() {
		activity = Robolectric.buildActivity(LibraryActivity.class).create()
				.start().resume().visible().get();
		shadowActivity = Robolectric.shadowOf(activity);
	}
}
