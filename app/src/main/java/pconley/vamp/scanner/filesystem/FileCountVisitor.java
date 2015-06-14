package pconley.vamp.scanner.filesystem;

import pconley.vamp.model.filesystem.MediaFile;
import pconley.vamp.model.filesystem.MediaFolder;
import pconley.vamp.model.filesystem.MediaVisitorBase;

/**
 * Visit part of a filesystem, counting media files.
 *
 * @author pconley
 */
public class FileCountVisitor implements MediaVisitorBase {

	private int count = 0;

	@Override
	public void visit(MediaFolder dir) {

	}

	@Override
	public void visit(MediaFile file) {
		count++;
	}

	/**
	 * @return The number of files (but not folders) in the system.
	 */
	public int getCount() {
		return count;
	}

}
