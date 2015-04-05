package pconley.vamp.util;

import java.io.File;

import pconley.vamp.R;
import android.content.Context;
import android.widget.Toast;

public final class FileUtils {

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

		int error = -1;
		if (!dir.exists()) {
			error = R.string.scan_error_no_such_path;
		} else if (!dir.isDirectory()) {
			error = R.string.scan_error_not_a_folder;
		} else if (!dir.canExecute()) {
			error = R.string.scan_error_not_readable;
		}

		if (error >= 0 && context != null) {
			Toast.makeText(context, error, Toast.LENGTH_LONG).show();
		}

		return error < 0;
	}

}
