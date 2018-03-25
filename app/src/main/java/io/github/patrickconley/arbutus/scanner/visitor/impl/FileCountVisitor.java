package io.github.patrickconley.arbutus.scanner.visitor.impl;

import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;

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
