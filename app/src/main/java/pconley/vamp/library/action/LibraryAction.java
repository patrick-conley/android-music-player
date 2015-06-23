package pconley.vamp.library.action;

import android.widget.ArrayAdapter;

import pconley.vamp.library.LibraryActivity;
import pconley.vamp.model.LibraryItem;

/**
 * After clicking an item in the library, replace the library or its contents.
 */
public interface LibraryAction {

	/**
	 * @param activity
	 * 		Current instance of the library
	 * @param adapter
	 * 		Contents of the library
	 * @param position
	 * 		Position in the adapter of the item clicked.
	 */
	void execute(LibraryActivity activity,
			ArrayAdapter<LibraryItem> adapter,
			int position);
}
