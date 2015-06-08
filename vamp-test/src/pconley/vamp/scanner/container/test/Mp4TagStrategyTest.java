package pconley.vamp.scanner.container.test;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import pconley.vamp.library.model.Tag;
import pconley.vamp.library.model.Track;
import pconley.vamp.scanner.container.Mp4TagStrategy;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

public class Mp4TagStrategyTest extends InstrumentationTestCase {

	private Context testContext;

	private File musicFolder;

	public void setUp() throws Exception {
		super.setUp();

		testContext = getInstrumentation().getContext();
		Context targetContext = new RenamingDelegatingContext(
				getInstrumentation().getTargetContext(), Constants.DB_PREFIX);

		musicFolder = AssetUtils.setupMusicFolder(targetContext);
	}

	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(musicFolder);
	}

	/**
	 * Given a single Ogg Vorbis file, when I scan the file, then the database
	 * contains the file and its tags.
	 */
	public void testMp4() throws Exception {
		File mp4 = new File(musicFolder, "sample.m4a");

		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.MP4, mp4);

		// When
		List<Tag> tags = new Mp4TagStrategy().getTags(mp4);

		// Then
		assertEquals("MP4 comments are read correctly.", expected,
				TagStrategyUtils.buildTrack(Uri.fromFile(mp4), tags));
	}
}
