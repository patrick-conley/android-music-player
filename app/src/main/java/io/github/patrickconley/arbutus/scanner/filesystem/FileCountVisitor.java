package io.github.patrickconley.arbutus.scanner.filesystem;

import io.github.patrickconley.arbutus.scanner.filesystem.model.MediaFile;
import io.github.patrickconley.arbutus.scanner.filesystem.model.MediaFolder;

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
