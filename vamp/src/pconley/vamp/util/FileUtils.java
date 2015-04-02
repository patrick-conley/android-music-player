package pconley.vamp.util;

import java.io.File;

import android.content.Context;
import android.widget.Toast;

public class FileUtils {

	/**
	 * Private constructor: static methods only.
	 */
	private FileUtils() {

	}

	/**
	 * Check if the given directory is valid. If the optional Context is
	 * non-null, display a Toast to the user.
	 * 
	 * @param context
	 *            Activity context for the Toast
	 * @param dir
	 *            Directory to validate
	 * @return {@code dir.exists && dir.isDirectory && dir.canExecute}
	 */
	public static boolean validateDirectory(File dir, Context context) {

		String error = null;
		if (!dir.exists()) {
			error = "Path does not exist";
		} else if (!dir.isDirectory()) {
			error = "Path doesn't point to a folder";
		} else if (!dir.canExecute()) {
			error = "Directory contents aren't readable";
		}

		if (error != null && context != null) {
			Toast.makeText(context, error, Toast.LENGTH_LONG).show();
		}

		return error == null;
	}

}
