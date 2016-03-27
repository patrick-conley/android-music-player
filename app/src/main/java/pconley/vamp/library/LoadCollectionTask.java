package pconley.vamp.library;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;

/**
 * Load the contents of the library into a TextView with execute(). Work is done
 * in a background thread.
 */
public class LoadCollectionTask
		extends AsyncTask<Void, Void, List<? extends LibraryItem>> {

	private final String name;
	private final List<Tag> filter;
	private final LibraryFragment fragment;

	public LoadCollectionTask(LibraryFragment fragment, String name,
			List<Tag> filter) {
		this.fragment = fragment;
		this.name = name;
		this.filter = filter == null ? new ArrayList<Tag>() : filter;
	}

	@Override
	protected List<? extends LibraryItem> doInBackground(Void... params) {
		LibraryOpenHelper helper =
				new LibraryOpenHelper(fragment.getActivity());

		if (name == null) {
			return new TrackDAO(helper).getFilteredTracks(filter);
		} else {
			return new TagDAO(helper).getFilteredTags(filter, name);
		}
	}

	@Override
	protected void onPostExecute(List<? extends LibraryItem> items) {
		fragment.onLoadCollection(new MusicCollection(name, filter, items));
	}
}
