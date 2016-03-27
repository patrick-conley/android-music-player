package pconley.vamp.scanner.filesystem.model;

import java.io.File;

import pconley.vamp.scanner.filesystem.FileVisitor;

public class MediaFile extends FileSystemItem {

	public MediaFile(File file) {
		super(file);
	}

	/**
	 * Validate the file (it must be readable), then visit it.
	 */
	@Override
	public void accept(FileVisitor visitor) {

		if (!getFile().exists() || !getFile().canRead()) {
			return;
		}

		visitor.visit(this);
	}

}
