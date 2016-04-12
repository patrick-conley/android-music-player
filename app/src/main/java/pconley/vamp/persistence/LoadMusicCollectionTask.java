package pconley.vamp.persistence;

import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.Tag;

/**
 * Load the contents of the library. Work is done in a background thread.
 */
public abstract class LoadMusicCollectionTask
		extends AsyncTask<String, Void, List<? extends LibraryItem>> {

	private final LibraryAction caller;
	private final List<Tag> filter;

	/**
	 * @param caller
	 * 		Calling action. New collection is returned asynchronously in the {@link
	 * 		LibraryAction#onLoadCollection} callback.
	 * @param filter
	 */
	public LoadMusicCollectionTask(LibraryAction caller, List<Tag> filter) {
		if (caller == null) {
			throw new IllegalArgumentException("Calling action not set");
		}

		this.caller = caller;
		this.filter = filter == null ? new LinkedList<Tag>() : filter;
	}

	public LibraryAction getCaller() {
		return caller;
	}

	public List<Tag> getFilter() {
		return filter;
	}
}
