package pconley.vamp.scanner.filesystem.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.scanner.filesystem.model.MediaFile;
import pconley.vamp.scanner.filesystem.model.MediaFolder;
import pconley.vamp.scanner.ScannerEvent;
import pconley.vamp.scanner.filesystem.FileScanVisitor;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.BroadcastConstants;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class FileScanVisitorTest {

	private static ScannerReceiver receiver;
	private static List<ScannerEvent> broadcastEvents;
	private static String finalBroadcastMessage;
	private static int finalBroadcastProgress;
	private static int finalBroadcastTotal;

	private static final int count = 10;

	private Context context;

	private File musicFolder;
	private TrackDAO dao;
	private FileScanVisitor visitor;

	@BeforeClass
	public static void setUp() {
		receiver = new ScannerReceiver();
		broadcastEvents = new LinkedList<ScannerEvent>();
	}

	@Before
	public void setUpTest() throws IOException {
		context = Robolectric.getShadowApplication().getApplicationContext();

		musicFolder = AssetUtils.setupMusicFolder(context);
		dao = new TrackDAO(new LibraryOpenHelper(context));
		visitor = new FileScanVisitor(musicFolder, context, count);

		IntentFilter filter
				= new IntentFilter(BroadcastConstants.FILTER_SCANNER);
		LocalBroadcastManager.getInstance(context)
		                     .registerReceiver(receiver, filter);

		broadcastEvents.clear();
		finalBroadcastMessage = null;
		finalBroadcastProgress = 0;
		finalBroadcastTotal = 0;
	}

	@After
	public void tearDownTest() {
		LocalBroadcastManager.getInstance(context).unregisterReceiver
				(receiver);

		dao.wipeDatabase();
	}

	/**
	 * When I visit the root music folder, then I receive an update with an
	 * empty message.
	 */
	@Test
	public void testVisitRoot() {
		// When
		visitor.visit(new MediaFolder(musicFolder));

		// Then
		assertEquals("Music folder is broadcast", "", finalBroadcastMessage);
		assertEquals("Music folder sends count", count, finalBroadcastTotal);
	}

	/**
	 * Given the music folder contains another folder, when I visit that folder,
	 * then I receive an update with the folder's name.
	 */
	@Test
	public void testVisitChildFolder() {
		// Given
		String name = "album";
		File file = new File(musicFolder, name);
		file.mkdir();

		// When
		visitor.visit(new MediaFolder(file));

		// Then
		assertEquals("Child folder name is broadcast", name,
		             finalBroadcastMessage);
		assertEquals("Child folder sends count", count, finalBroadcastTotal);
	}

	/**
	 * Given the music folder contains a non-music file, when I visit it, then I
	 * receive a progress broadcast but nothing is added to the database.
	 */
	@Test
	public void testNonMediaFile() throws IOException {
		// Given
		File file = new File(musicFolder, "sample.jpg");
		file.createNewFile();

		// When
		visitor.visit(new MediaFile(file));

		// Then
		assertEquals("Progress broadcast received",
		             Arrays.asList(new ScannerEvent[] { ScannerEvent.UPDATE }),
		             broadcastEvents);
		assertEquals("Correct number of files scanned", 1,
		             finalBroadcastProgress);
		assertEquals(
				"No files are scanned from a directory without media files", 0,
				dao.getAllTracks().size());
	}

	/**
	 * Given the music folder contains a non-music file, when I visit it several
	 * times, then I receive a progress broadcast each time.
	 */
	@Test
	public void testMultipleVisits() throws IOException {
		// Given
		File file = new File(musicFolder, "sample.jpg");
		file.createNewFile();

		// When
		List<ScannerEvent> events = new LinkedList<ScannerEvent>();
		MediaFile media = new MediaFile(file);
		visitor.visit(media);
		events.add(ScannerEvent.UPDATE);
		visitor.visit(media);
		events.add(ScannerEvent.UPDATE);
		visitor.visit(media);
		events.add(ScannerEvent.UPDATE);

		// Then
		assertEquals("Progress broadcasts received", events, broadcastEvents);
		assertEquals("Correct number of files scanned", 3,
		             finalBroadcastProgress);
	}

	/**
	 * Given the music folder contains a music file, when I visit it twice, then
	 * I receive a broadcast with an error message.
	 */
	@Test
	public void testDuplicateVisit() throws IOException {
		// Given
		File file = new File(musicFolder, "sample.ogg");
		AssetUtils.addAssetToFolder(context, AssetUtils.ROBO_ASSET_PATH
		                                     + AssetUtils.OGG, file);

		// When
		List<ScannerEvent> events = new LinkedList<ScannerEvent>();
		MediaFile media = new MediaFile(file);
		visitor.visit(media);
		events.add(ScannerEvent.UPDATE);
		visitor.visit(media);
		events.add(ScannerEvent.UPDATE);
		events.add(ScannerEvent.ERROR);

		// Then
		assertEquals("Error broadcast received", events, broadcastEvents);
	}

	/**
	 * When I visit a file, then I receive a progress broadcast and its tags are
	 * scanned.
	 */
	@Test
	public void testVisitVorbis() throws IOException {
		// Given
		File file = new File(musicFolder, "sample.ogg");
		Track expected
				= AssetUtils.addAssetToFolder(context,
				                              AssetUtils.ROBO_ASSET_PATH +
				                              AssetUtils.OGG, file);

		// When
		visitor.visit(new MediaFile(file));

		// Then
		assertEquals("Progress broadcast received",
		             Collections.singletonList(ScannerEvent.UPDATE),
		             broadcastEvents);
		assertEquals("Correct number of files scanned", 1,
		             finalBroadcastProgress);
		assertEquals("Vorbis comments are scanned correctly",
		             Collections.singletonList(expected), dao.getAllTracks());
	}

	private static class ScannerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			broadcastEvents.add((ScannerEvent) intent
					.getSerializableExtra(BroadcastConstants.EXTRA_EVENT));
			finalBroadcastMessage = intent
					.getStringExtra(BroadcastConstants.EXTRA_MESSAGE);
			finalBroadcastProgress = intent.getIntExtra(
					BroadcastConstants.EXTRA_PROGRESS, -1);
			finalBroadcastTotal = intent
					.getIntExtra(BroadcastConstants.EXTRA_TOTAL, -1);
		}

	}

}
