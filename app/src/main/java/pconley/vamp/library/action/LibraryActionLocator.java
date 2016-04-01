package pconley.vamp.library.action;

import android.app.Activity;

import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

/**
 * Locator for types of actions that can be performed after clicking an item in
 * the library.
 */
public class LibraryActionLocator {

	private Activity activity;

	public LibraryActionLocator(Activity activity) {
		this.activity = activity;
	}

	/**
	 * @param item
	 * @return An appropriate {@link LibraryAction} based on the type of item
	 * clicked.
	 */
	public LibraryAction findAction(LibraryItem item) {
		if (item instanceof Tag) {
			return new LibraryFilterAction(activity);
		} else if (item instanceof Track) {
			return new LibraryPlayAction(activity);
		} else {
			return null;
		}
	}
}
