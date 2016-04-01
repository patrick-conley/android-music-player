package pconley.vamp.library.action;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import org.apache.commons.lang3.text.WordUtils;

import java.util.LinkedList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.LoadCollectionTask;
import pconley.vamp.library.view.LibraryActivity;
import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.library.view.TagHistoryView;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;

/**
 * Replace the contents of the library by filtering against the clicked-on tag.
 */
public class LibraryFilterAction implements LibraryAction {

	@Override
	public void execute(Activity activity,
			List<? extends LibraryItem> contents, int position) {
		FragmentManager fm = activity.getFragmentManager();
		LibraryFragment parent = (LibraryFragment) fm.findFragmentById(
				R.id.library_container);

		Tag selected = (contents == null) ? null : (Tag) contents.get(position);

		MusicCollection collection = null;
		if (parent != null) { // Fragment may be null if activity is new
			collection = parent.getCollection();
		}

		// Build the new collection
		LibraryFragment childFragment = new LibraryFragment();
		String childName = buildChildName(selected);
		List<Tag> childFilter = buildChildFilter(collection, selected);

		new LoadCollectionTask(activity, childFragment, childName, childFilter)
				.execute();

		// Create a new fragment
		fm.beginTransaction()
		  .replace(R.id.library_container, childFragment)
				  /* FIXME: add useTransition to settings for unit tests? */
		  .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		  .addToBackStack(parent == null
		                  ? LibraryActivity.LIBRARY_ROOT_TAG
		                  : null)
		  .commit();

		// Update the parent activity
		if (childName == null) {
			activity.setTitle(childFragment.getString(R.string.track));
		} else {
			activity.setTitle(WordUtils.capitalize(childName));
		}

		((TagHistoryView) activity
				.findViewById(R.id.library_tag_history)).push(selected);

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
