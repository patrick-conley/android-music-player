package pconley.vamp.library.action;

import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;

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
			return new TagFilterAction();
		} else if (item instanceof Track) {
			return new PlayAction();
		} else {
			return null;
		}
	}
}
