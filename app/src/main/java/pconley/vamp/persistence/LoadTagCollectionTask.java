package pconley.vamp.persistence;

import java.util.List;

import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.TagCollection;

/**
 * Task to load a collection of tracks. The collection's name must be passed as
 * a String to {@link LoadTagCollectionTask#execute(Object[])}.
 *
 * @see LoadMusicCollectionTask
 */
public class LoadTagCollectionTask extends LoadMusicCollectionTask {

	private String name;

	public LoadTagCollectionTask(LibraryAction caller, List<Tag> filter) {
		super(caller, filter);
	}

	@Override
	protected List<? extends LibraryItem> doInBackground(String... params) {
		this.name = params[0];

		return new TagDAO(new LibraryOpenHelper(getCaller().getContext()))
				.getFilteredTags(getFilter(), name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPostExecute(List<? extends LibraryItem> items) {
		getCaller().onLoadCollection(
				new TagCollection(name, getFilter(), (List<Tag>) items));
	}
}
