package pconley.vamp.library.action;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.library.TagHistoryView;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;

/**
 * Replace the contents of the library by filtering against the clicked-on tag.
 */
public class LibraryFilterAction implements LibraryAction {

	@Override
	public void execute(LibraryActivity activity,
			List<? extends LibraryItem> contents, int position) {

		Tag tag = (Tag) contents.get(position);

		FragmentManager fm = activity.getFragmentManager();

		MusicCollection collection = ((LibraryFragment) fm.findFragmentById(
				R.id.library_container)).getCollection();

		String fragmentName = collection.getName() == null
		                      ? LibraryActivity.LIBRARY_ROOT_TAG : null;

		// Create a new fragment
		fm.beginTransaction()
		  .replace(R.id.library_container,
		           LibraryFragment.newInstance(collection, tag))
		  .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		  .addToBackStack(fragmentName)
		  .commit();

		// Add the tag to the history list
		((TagHistoryView) activity
				.findViewById(R.id.library_tag_history)).push(tag);

	}
}
