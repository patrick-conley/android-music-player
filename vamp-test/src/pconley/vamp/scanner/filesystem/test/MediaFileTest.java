package pconley.vamp.scanner.filesystem.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import pconley.vamp.scanner.filesystem.MediaFile;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

public class MediaFileTest extends InstrumentationTestCase {

	private AccessCountVisitor visitor;
	private File musicFolder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Context targetContext = new RenamingDelegatingContext(
				getInstrumentation().getTargetContext(), Constants.DB_PREFIX);

		visitor = new AccessCountVisitor();
		musicFolder = AssetUtils.setupMusicFolder(targetContext);

	}

	@Override
	protected void tearDown() throws Exception {
		FileUtils.deleteDirectory(musicFolder);

		super.tearDown();
	}

	/**
	 * getFile returns the correct file.
	 */
	public void testGetFile() {
		File expected = new File("sample.ogg");

		MediaFile file = new MediaFile(expected);

		assertEquals("getFile returns the correct file", expected,
				file.getFile());
	}

	/**
	 * toString returns the file.
	 */
	public void testToString() {
		File expected = new File("sample.ogg");

		MediaFile file = new MediaFile(expected);

		assertEquals("toString returns the file", expected.toString(), file.toString());
	}

	/**
	 * Given the file doesn't exist, when I accept it, then the visitor is not
	 * called.
	 */
	public void testMissingFile() {
		// Given
		File file = new File(musicFolder, "sample.ogg");

		// When
		new MediaFile(file).accept(visitor);

		// Then
		assertEquals("No folders are visited when no folders exist", 0,
				visitor.getFolderVisits());
		assertEquals("No files are visited when no files exist", 0,
				visitor.getFileVisits());
	}

	/**
	 * Given the file exists, when I accept it, then the visitor is called.
	 */
	public void testRealFile() throws IOException {
		// Given
		File file = new File(musicFolder, "sample.ogg");
		file.createNewFile();

		// When
		new MediaFile(file).accept(visitor);

		// Then
		assertEquals("No folders are visited when no folders exist", 0,
				visitor.getFolderVisits());
		assertEquals("No files are visited when no files exist", 1,
				visitor.getFileVisits());
	}

}
