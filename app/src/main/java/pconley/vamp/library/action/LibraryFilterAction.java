package pconley.vamp.library.action;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.ArrayAdapter;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.library.TagHistoryView;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.MusicCollection;
import pconley.vamp.model.Tag;

/**
 * Replace the contents of the library by filtering against the clicked-on tag.
 */
public class LibraryFilterAction implements LibraryAction {

	// FIXME: split into prepare/execute methods
	@Override
	public void execute(LibraryActivity activity,
			ArrayAdapter<LibraryItem> adapter, int position) {

		Tag tag = (Tag) adapter.getItem(position);

		FragmentManager fm = activity.getFragmentManager();

		MusicCollection collection = ((LibraryFragment) fm.findFragmentById(
				R.id.library_container)).getCollection();

		String fragmentName = collection.getTags().isEmpty()
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
