package pconley.vamp.scanner.filesystem;

import pconley.vamp.scanner.filesystem.model.FileSystemItem;
import pconley.vamp.scanner.filesystem.model.MediaFile;
import pconley.vamp.scanner.filesystem.model.MediaFolder;

/**
 * Traverse a hierarchical filesystem. Methods in subclasses of
 * {@link FileVisitor} should only be called from {@link MediaFolder} and
 * {@link MediaFile}.
 *
 * @author pconley
 */
public interface FileVisitor {

	/**
	 * Do some work on a directory. The directory is guaranteed to exist and to
	 * not contain a .nomedia file.
	 * <p/>
	 * This method should *not* call
	 * {@link FileSystemItem#accept(FileVisitor)} on the directory's
	 * children - {@link MediaFolder#accept(FileVisitor)} does that itself
	 * after this method returns.
	 *
	 * @param dir
	 */
	void visit(MediaFolder dir);

	/**
	 * Do some work on a file. The file is guaranteed to be readable.
	 *
	 * @param file
	 */
	void visit(MediaFile file);

}
