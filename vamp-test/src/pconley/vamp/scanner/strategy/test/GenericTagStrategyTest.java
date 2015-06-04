package pconley.vamp.scanner.strategy.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import pconley.vamp.library.model.Track;
import pconley.vamp.scanner.strategy.GenericTagStrategy;
import pconley.vamp.scanner.strategy.TagStrategy;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

public class GenericTagStrategyTest extends InstrumentationTestCase {

	private Context testContext;
	private Context targetContext;

	private File musicFolder;
	private MediaMetadataRetriever metadataRetriever;

	public void setUp() throws Exception {
		super.setUp();

		testContext = getInstrumentation().getContext();
		targetContext = new RenamingDelegatingContext(getInstrumentation()
				.getTargetContext(), Constants.DB_PREFIX);

		musicFolder = AssetUtils.setupMusicFolder(targetContext);
		metadataRetriever = new MediaMetadataRetriever();
	}

	public void tearDown() throws Exception {
		metadataRetriever.release();
		FileUtils.deleteDirectory(musicFolder);
	}

	/**
	 * Given a single Ogg Vorbis file, when I scan the file, then the database
	 * contains the file and its tags.
	 */
	// FIXME: MediaMetadataRetriever doesn't consistently read tags
	/* public void testMp4() throws Exception {
		File mp4 = new File(musicFolder, "sample.mp4");
		TagStrategy strategy = new GenericTagStrategy(metadataRetriever);

		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.MP4, mp4);

		// When
		Map<String, List<String>> tags = strategy.getTags(mp4);

		// Then
		assertEquals("MP4 comments are read correctly.",
				expected, TagStrategyUtils.buildTrack(Uri.fromFile(mp4), tags));
	}
	*/
}
