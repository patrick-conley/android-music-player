package pconley.vamp.scanner.filesystem.test;

import java.io.File;

import pconley.vamp.scanner.filesystem.model.MediaFile;
import pconley.vamp.scanner.filesystem.model.MediaFolder;
import pconley.vamp.scanner.filesystem.FileCountVisitor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileCountVisitorTest {

	private File file = new File("foo");

	/**
	 * When I visit a folder, then the counter is not incremented.
	 */
	@Test
	public void testFolder() {
		FileCountVisitor visitor = new FileCountVisitor();

		// When
		visitor.visit(new MediaFolder(file));

		// Then
		assertEquals("A folder is not counted", 0, visitor.getCount());
	}

	/**
	 * When I visit a file, then the counter is incremented.
	 */
	@Test
	public void testFile() {
		FileCountVisitor visitor = new FileCountVisitor();

		// When
		visitor.visit(new MediaFile(file));

		// Then
		assertEquals("A file is counted", 1, visitor.getCount());
	}

	/**
	 * Given I have visited a file, when I visit a folder, then the counter is
	 * not reset.
	 */
	@Test
	public void testFileThenFolder() {
		FileCountVisitor visitor = new FileCountVisitor();

		// When
		visitor.visit(new MediaFile(file));
		visitor.visit(new MediaFolder(file));

		// Then
		assertEquals("Folders don't reset the count", 1, visitor.getCount());
	}

}
