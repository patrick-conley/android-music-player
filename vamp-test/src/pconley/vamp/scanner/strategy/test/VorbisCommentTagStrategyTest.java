package pconley.vamp.scanner.strategy.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import pconley.vamp.library.model.Track;
import pconley.vamp.scanner.strategy.TagStrategy;
import pconley.vamp.scanner.strategy.VorbisCommentTagStrategy;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

public class VorbisCommentTagStrategyTest extends InstrumentationTestCase {

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
	public void testOgg() throws Exception {
		File ogg = new File(musicFolder, "sample.ogg");
		TagStrategy strategy = new VorbisCommentTagStrategy();

		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.OGG, ogg);

		// When
		Map<String, List<String>> tags = strategy.getTags(ogg);

		// Then
		assertEquals("Vorbis comments are read correctly in Ogg streams.",
				expected, TagStrategyUtils.buildTrack(Uri.fromFile(ogg), tags));
	}

	/**
	 * Given a single Flac Vorbis file, when I scan the file, then the database
	 * contains the file and its tags.
	 */
	public void testFlac() throws Exception {
		File flac = new File(musicFolder, "sample.flac");
		TagStrategy strategy = new VorbisCommentTagStrategy();

		// Given
		Track expected = AssetUtils.addAssetToFolder(testContext,
				AssetUtils.FLAC, flac);

		// When
		Map<String, List<String>> tags = strategy.getTags(flac);

		// Then
		assertEquals("Vorbis comments are read correctly in FLAC files",
				expected, TagStrategyUtils.buildTrack(Uri.fromFile(flac), tags));
	}

}
