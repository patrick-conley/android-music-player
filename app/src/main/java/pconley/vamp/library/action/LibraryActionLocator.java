package pconley.vamp.library.action;

import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

/**
 * Locator for types of actions that can be performed after clicking an item in
 * the library.
 */
public class LibraryActionLocator {

	/**
	 * @param item
	 * @return An appropriate {@link LibraryAction} based on the type of item
	 * clicked.
	 */
	public static LibraryAction findAction(LibraryItem item) {
		if (item instanceof Tag) {
			return new LibraryFilterAction();
		} else if (item instanceof Track) {
			return new LibraryPlayAction();
		} else {
			return null;
		}
	}
}
