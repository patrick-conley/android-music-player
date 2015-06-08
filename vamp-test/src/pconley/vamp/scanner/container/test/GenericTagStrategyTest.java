package pconley.vamp.scanner.container.test;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import pconley.vamp.library.model.Tag;
import pconley.vamp.library.model.Track;
import pconley.vamp.scanner.container.GenericTagStrategy;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

public class GenericTagStrategyTest extends InstrumentationTestCase {

	private Context testContext;

	private File musicFolder;
	private GenericTagStrategy strategy;

	public void setUp() throws Exception {
		super.setUp();

		testContext = getInstrumentation().getContext();
		Context targetContext = new RenamingDelegatingContext(getInstrumentation()
				.getTargetContext(), Constants.DB_PREFIX);

		musicFolder = AssetUtils.setupMusicFolder(targetContext);
		strategy = new GenericTagStrategy();
	}

	public void tearDown() throws Exception {
		strategy.release();
		FileUtils.deleteDirectory(musicFolder);
	}

	/**
	 * Given a single Ogg Vorbis file, when I scan the file, then the database
	 * contains the file and its tags.
	 */
	public void testMp3() throws Exception {
		File mp3 = new File(musicFolder, "sample.mp3");

		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.MP3, mp3);

		// When
		List<Tag> tags = strategy.getTags(mp3);

		// Then
		assertEquals("MP3 comments are read correctly.", expected,
				TagStrategyUtils.buildTrack(Uri.fromFile(mp3), tags));
	}
}
