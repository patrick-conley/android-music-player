package pconley.vamp.persistence;

import java.util.List;

import pconley.vamp.library.action.LibraryAction;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.model.TrackCollection;

/**
 * Task to load a collection of tracks. Any arguments to {@link
 * LoadTrackCollectionTask#execute(Object[])} are ignored
 *
 * @see LoadMusicCollectionTask
 */
public class LoadTrackCollectionTask extends LoadMusicCollectionTask {

	public LoadTrackCollectionTask(LibraryAction caller, List<Tag> filter) {
		super(caller, filter);
	}

	@Override
	protected List<? extends LibraryItem> doInBackground(String... params) {
		return new TrackDAO(new LibraryOpenHelper(getCaller().getContext()))
				.getFilteredTracks(getFilter());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPostExecute(List<? extends LibraryItem> items) {
		getCaller().onLoadCollection(
				new TrackCollection(getFilter(), (List<Track>) items));
	}
}
