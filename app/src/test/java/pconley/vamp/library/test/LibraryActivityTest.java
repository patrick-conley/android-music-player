package pconley.vamp.library.test;

import android.content.Context;
import android.content.Intent;

import org.junit.After;
import org.junit.Before;
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
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.preferences.SettingsActivity;
import pconley.vamp.scanner.ScannerProgressDialogFragment;
import pconley.vamp.scanner.ScannerService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LibraryActivityTest {

	private LibraryActivity activity;
	private ShadowActivity shadowActivity;

	private Context context;

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
	 * Given the context is running, when I click "Player", then the Player
	 * context is launched without an action.
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
	 * Given the context is running, when I click "Settings", then the Settings
	 * context is launched
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
	 * Given the context is running, when I click "Rebuild library", then the
	 * ScannerService is launched and a dialog is displayed.
	 */
	@Test
	public void testScannerLaunchedOnClick() {
		// Given
		startActivity();

		assertNull(
				"Progress dialog is not visible by default",
				activity.getFragmentManager().findFragmentByTag(
						ScannerProgressDialogFragment.TAG));

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

	private void startActivity() {
		activity = Robolectric.buildActivity(LibraryActivity.class).create()
				.start().resume().visible().get();
		shadowActivity = Robolectric.shadowOf(activity);
	}
}
