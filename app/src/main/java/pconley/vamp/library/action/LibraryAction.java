package pconley.vamp.library.action;

import android.app.Activity;
import android.content.Context;

import pconley.vamp.persistence.model.MusicCollection;

/**
 * Any action that can be performed by selecting an item in a collection.
 * <p/>
 * A class implementing this interface should define a one-argument constructor
 * which takes an Activity.
 */
public abstract class LibraryAction {

	private final Activity activity;

	public LibraryAction(Activity activity) {
		this.activity = activity;
	}

	public Context getContext() {
		return activity;
	}

	/**
	 * Run the task. Result of running an action twice may or may not be defined
	 * for an implementing class.
	 *
	 * @param collection
	 * 		Current contents of the library
	 * @param position
	 * 		Position in the adapter of the item clicked.
	 */
	public abstract void execute(MusicCollection collection, int position);

	/**
	 * If the action needs to load a MusicCollection asynchronously, that
	 * collection is returned to this callback.
	 *
	 * @param collection
	 */
	public abstract void onLoadCollection(MusicCollection collection);
}
