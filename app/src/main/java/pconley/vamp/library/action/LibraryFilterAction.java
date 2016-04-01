package pconley.vamp.library.action;

import android.app.Activity;
import android.app.FragmentManager;

import org.apache.commons.lang3.text.WordUtils;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.persistence.LoadCollectionTask;
import pconley.vamp.library.view.LibraryActivity;
import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.library.view.TagHistoryView;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;

/**
 * Replace the contents of the library by filtering against the clicked-on tag.
 */
public class LibraryFilterAction extends LibraryAction {

	private LibraryFragment childFragment = new LibraryFragment();

	public LibraryFilterAction(Activity activity) {
		super(activity);
	}

	@Override
	public void execute(MusicCollection collection, int position) {
		Activity activity = (Activity) getContext();
		FragmentManager fm =  activity.getFragmentManager();
		LibraryFragment parent = (LibraryFragment) fm.findFragmentById(
				R.id.library_container);

		if (collection != null && collection.getName() == null) {
			throw new IllegalArgumentException("Can't filter a list of tracks.");
		}

		Tag selected = (collection == null)
		               ? null
		               : (Tag) collection.getContents().get(position);

		// Build the new collection
		String childName = buildChildName(selected);
		List<Tag> childFilter = buildChildFilter(collection, selected);

		new LoadCollectionTask(this, childName, childFilter)
				.execute();

		// Create a new fragment
		fm.beginTransaction()
				.replace(R.id.library_container, childFragment)
				  /* FIXME: add useTransition to settings for unit tests? */
//				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(parent == null
				                ? LibraryActivity.LIBRARY_ROOT_TAG
				                : null)
				.commit();

		// Update the parent activity
		if (childName == null) {
			activity.setTitle(activity.getString(R.string.track));
		} else {
			activity.setTitle(WordUtils.capitalize(childName));
		}

		((TagHistoryView) activity
				.findViewById(R.id.library_tag_history)).push(selected);

	}

	@Override
	public void onLoadCollection(MusicCollection collection) {
		childFragment.setCollection(collection);
	}

	private String buildChildName(Tag selected) {
		if (selected == null) {
			return "artist";
		} else if (selected.getName() == null) {
			throw new IllegalArgumentException("Tracks can't have children");
		} else {

			switch (selected.getName()) {
				case "artist":
					return "album";
				case "album":
					return null;
				default:
					throw new IllegalArgumentException(
							"Unknown tag name " + selected.getName());
			}
		}
	}

	private List<Tag> buildChildFilter(MusicCollection parent, Tag selected) {
		List<Tag> filter = new LinkedList<Tag>();

		if (parent != null) {
			filter.addAll(parent.getFilter());
		}
		if (selected != null) {
			filter.add(selected);
		}

		return filter;
	}
}
