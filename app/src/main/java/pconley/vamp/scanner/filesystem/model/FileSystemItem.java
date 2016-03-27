package pconley.vamp.scanner.filesystem.model;

import java.io.File;

import pconley.vamp.scanner.filesystem.FileVisitor;

/**
 * Representation of a file to be used as part of a hierarchical visitor pattern
 * with {@link FileVisitor}.
 *
 * @author pconley
 */
public abstract class FileSystemItem {

	private File file;

	public FileSystemItem(File file) {
		this.file = file;
	}

	/**
	 * Validate, then visit the file.
	 *
	 * @param visitor
	 */
	public abstract void accept(FileVisitor visitor);

	/**
	 * @return The File underlying this object.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return The string representation of the underlying file.
	 */
	public String toString() {
		return file.toString();
	}

}
