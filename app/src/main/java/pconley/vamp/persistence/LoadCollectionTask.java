package pconley.vamp.persistence;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;

/**
 * Load the contents of the library. Work is done in a background thread.
 */
public class LoadCollectionTask
		extends AsyncTask<Void, Void, List<? extends LibraryItem>> {

	private final String name;
	private final List<Tag> filter;
	private final LibraryAction caller;

	/**
	 * @param caller
	 * 		Calling action. New collection is returned asynchronously in the
	 * 		onLoadCollection callback.
	 * @param name
	 * 		Name of tags to return. Return tracks if null
	 * @param filter
	 */
	public LoadCollectionTask(LibraryAction caller,
			String name, List<Tag> filter) {
		if (caller == null) {
			throw new IllegalArgumentException("Calling action not set");
		}

		this.caller = caller;
		this.name = name;
		this.filter = filter == null ? new ArrayList<Tag>() : filter;
	}

	@Override
	protected List<? extends LibraryItem> doInBackground(Void... params) {
		LibraryOpenHelper helper = new LibraryOpenHelper(caller.getContext());

		if (name == null) {
			return new TrackDAO(helper).getFilteredTracks(filter);
		} else {
			return new TagDAO(helper).getFilteredTags(filter, name);
		}
	}

	@Override
	protected void onPostExecute(List<? extends LibraryItem> items) {
		caller.onLoadCollection(new MusicCollection(name, filter, items));
	}
}
