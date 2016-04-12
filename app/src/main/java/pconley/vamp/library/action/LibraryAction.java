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
	private MusicCollection collection;

	public LibraryAction(Activity activity, MusicCollection parent) {
		this.activity = activity;
		this.collection = parent;
	}

	public Context getContext() {
		return activity;
	}

	public MusicCollection getCollection() {
		return collection;
	}

	/**
	 * Run the task. Result of running an action twice may or may not be defined
	 * for an implementing class.
	 *
	 * @param position
	 * 		Position in the adapter of the item clicked.
	 */
	public abstract void execute(int position);

	/**
	 * If the action needs to load a MusicCollection asynchronously, that
	 * collection is returned to this callback.
	 *
	 * @param child
	 */
	public abstract void onLoadCollection(MusicCollection child);
}
