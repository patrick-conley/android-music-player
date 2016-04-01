package pconley.vamp.library.action;

import android.app.Activity;

import java.util.List;

import pconley.vamp.persistence.model.LibraryItem;

/**
 * After clicking an item in the library, replace the library or its contents.
 */
public interface LibraryAction {

	/**
	 * @param activity
	 * 		Current instance of the library
	 * @param contents
	 * 		Contents of the library
	 * @param position
	 * 		Position in the adapter of the item clicked.
	 */
	void execute(Activity activity,
			List<? extends LibraryItem> contents,
			int position);
}
