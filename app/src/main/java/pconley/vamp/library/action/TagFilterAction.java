package pconley.vamp.library.action;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.ArrayAdapter;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.MusicCollection;
import pconley.vamp.model.Tag;

/**
 * Replace the contents of the library by filtering against the clicked-on tag.
 */
public class TagFilterAction implements LibraryAction {

	// FIXME: split into prepare/execute methods
	@Override
	public void execute(LibraryActivity activity,
			ArrayAdapter<LibraryItem> adapter, int position) {

		FragmentManager fm = activity.getFragmentManager();

		MusicCollection collection = ((LibraryFragment) fm.findFragmentById(
				R.id.library)).getCollection();

		// Add a tag if this will be the first fragment on top of the root.
		String fragmentName = collection.getTags().isEmpty()
		                      ? LibraryActivity.LIBRARY_ROOT_TAG : null;

		// Create a new fragment
		fm.beginTransaction()
		  .replace(R.id.library,
		           LibraryFragment.newInstance(collection,
		                                       (Tag) adapter.getItem(position)))
		  .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		  .addToBackStack(fragmentName)
		  .commit();
	}
}
