package pconley.vamp.library.action;

import android.app.Activity;

import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.TagCollection;
import pconley.vamp.persistence.model.TrackCollection;

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
	 * @param collection
	 * @return An appropriate {@link LibraryAction} based on the type of item
	 * clicked.
	 */
	public LibraryAction findAction(MusicCollection collection) {
		if (collection instanceof TagCollection) {
			return new LibraryFilterAction(activity, (TagCollection) collection);
		} else if (collection instanceof TrackCollection) {
			return new LibraryPlayAction(activity, collection);
		} else {
			return null;
		}
	}
}
