package pconley.vamp.scanner.filesystem.model.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import pconley.vamp.scanner.filesystem.model.MediaFolder;
import pconley.vamp.scanner.filesystem.test.AccessCountVisitor;
import pconley.vamp.util.AssetUtils;
import pconley.vamp.util.Constants;
import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;

public class MediaFolderTest extends InstrumentationTestCase {

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
		File expected = new File("music/");

		MediaFolder folder = new MediaFolder(expected);

		assertEquals("getFile returns the correct file", expected,
				folder.getFile());
	}

	/**
	 * toString returns the file.
	 */
	public void testToString() {
		File expected = new File("music/");

		MediaFolder file = new MediaFolder(expected);

		assertEquals("toString returns the file", expected.toString(),
				file.toString());
	}

	/**
	 * Given the folder doesn't exist, when I accept it, then its visitor is not
	 * called.
	 */
	public void testMissingFolder() {
		// Given
		File dir = new File(musicFolder, "album");

		// When
		new MediaFolder(dir).accept(visitor);

		// Then
		assertEquals("No folders are visited when no folders exist", 0,
				visitor.getFolderVisits());
		assertEquals("No files are visited when no files exist", 0,
				visitor.getFileVisits());
	}

	/**
	 * Given this has been created with a file, not a folder, when I accept it,
	 * then its visitor is not called.
	 */
	public void testFileNotFolder() throws IOException {
		// Given
		File file = new File(musicFolder, "sample.ogg");
		file.createNewFile();

		// When
		new MediaFolder(file).accept(visitor);

		// Then
		assertEquals("No folders are visited when no folders exist", 0,
				visitor.getFolderVisits());
		assertEquals("No files are visited when input is not a folder", 0,
				visitor.getFileVisits());
	}

	/**
	 * Given the folder exists but has a .nomedia file, when I accept it, then
	 * its visitor is not called.
	 */
	public void testNoMedia() throws IOException {
		// Given
		new File(musicFolder, ".nomedia").createNewFile();

		// When
		new MediaFolder(musicFolder).accept(visitor);

		// Then
		assertEquals("No folders are visited when no folders exist", 0,
				visitor.getFolderVisits());
		assertEquals("No files are visited when .nomedia exists", 0,
				visitor.getFileVisits());
	}

	/**
	 * Given the folder exists but has a .nomedia file, when I accept it, then
	 * media files in the folder are not visited.
	 */
	public void testNoMediaAborts() throws IOException {
		// Given
		new File(musicFolder, "sample.ogg").createNewFile();
		new File(musicFolder, ".nomedia").createNewFile();

		// When
		new MediaFolder(musicFolder).accept(visitor);

		// Then
		assertEquals("No folders are visited when no folders exist", 0,
				visitor.getFolderVisits());
		assertEquals("No files are visited when .nomedia exists", 0,
				visitor.getFileVisits());
	}

	/**
	 * Given the folder exists, when I accept it, then its visitor is called.
	 */
	public void testRealFolder() {
		// Given - nothing to do

		// When
		new MediaFolder(musicFolder).accept(visitor);

		// Then
		assertEquals("The music folder is visited", 1,
				visitor.getFolderVisits());
		assertEquals("No files are visited when no files exist", 0,
				visitor.getFileVisits());
	}

	/**
	 * Given the folder exists and contains a file, when I accept it, then it
	 * and its child are both visited.
	 */
	public void testRealFolderAndFile() throws IOException {
		// Given
		new File(musicFolder, "sample.ogg").createNewFile();
		new File(musicFolder, "sample.mp3").createNewFile();

		// When
		new MediaFolder(musicFolder).accept(visitor);

		// Then
		assertEquals("The music folder is visited", 1,
				visitor.getFolderVisits());
		assertEquals("Music files are visited", 2, visitor.getFileVisits());
	}

	/**
	 * Given the folder exists and contains a folder, when I accept it, then it
	 * and its child are both visited.
	 */
	public void testNestedFolders() throws IOException {
		// Given
		new File(musicFolder, "album").mkdir();

		// When
		new MediaFolder(musicFolder).accept(visitor);

		// Then
		assertEquals("The music folder and its child are visited", 2,
				visitor.getFolderVisits());
		assertEquals("No files are visited when no files exist", 0,
				visitor.getFileVisits());
	}

}
