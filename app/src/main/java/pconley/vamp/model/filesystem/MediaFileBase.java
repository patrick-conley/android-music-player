package pconley.vamp.model.filesystem;

import java.io.File;

/**
 * Representation of a file to be used as part of a hierarchical visitor pattern
 * with {@link MediaVisitorBase}.
 * 
 * @author pconley
 *
 */
public abstract class MediaFileBase {

	private File file;

	public MediaFileBase(File file) {
		this.file = file;
	}

	/**
	 * Validate, then visit the file.
	 * 
	 * @param visitor
	 */
	public abstract void accept(MediaVisitorBase visitor);

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
