package pconley.vamp.scanner.filesystem.model;

import android.util.Log;

import java.io.File;

import pconley.vamp.scanner.filesystem.FileVisitor;

/**
 * A visitable directory.
 *
 * @author pconley
 */
public class MediaFolder extends FileSystemItem {
	private static final String TAG = "FilesystemScanner";

	public MediaFolder(File file) {
		super(file);
	}

	/**
	 * Validate the directory (it must be a readable directory that
	 * does not contain a .nomedia file), then visit it and call its children's
	 * appropriate accept methods.
	 */
	@Override
	public void accept(FileVisitor visitor) {

		// Check the directory is readable
		if (!getFile().exists() || !getFile().isDirectory()
				|| !getFile().canExecute()) {
			Log.w(TAG, "Directory is invalid");
			return;
		}

		Log.d(TAG, "Scanning directory " + getFile().toString());

		// Check the directory allows media scanning
		if (new File(getFile(), ".nomedia").exists()
				|| new File(getFile(), ".NOMEDIA").exists()) {
			Log.d(TAG, "Skipping directory (.nomedia)");
			return;
		}

		visitor.visit(this);

		File[] contents = getFile().listFiles();
		for (File file : contents) {
			if (file.isDirectory()) {
				new MediaFolder(file).accept(visitor);
			} else {
				new MediaFile(file).accept(visitor);
			}
		}
	}

}