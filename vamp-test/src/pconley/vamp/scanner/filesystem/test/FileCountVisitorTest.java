package pconley.vamp.scanner.filesystem.test;

import java.io.File;

import pconley.vamp.model.filesystem.MediaFile;
import pconley.vamp.model.filesystem.MediaFolder;
import pconley.vamp.scanner.filesystem.FileCountVisitor;
import android.test.AndroidTestCase;

public class FileCountVisitorTest extends AndroidTestCase {

	private File file = new File("foo");

	/**
	 * When I visit a folder, then the counter is not incremented.
	 */
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
	public void testFileThenFolder() {
		FileCountVisitor visitor = new FileCountVisitor();

		// When
		visitor.visit(new MediaFile(file));
		visitor.visit(new MediaFolder(file));

		// Then
		assertEquals("Folders don't reset the count", 1, visitor.getCount());
	}

}
