package pconley.vamp.library.action;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import pconley.vamp.R;
import pconley.vamp.library.LibraryActivity;
import pconley.vamp.library.LibraryFragment;
import pconley.vamp.model.LibraryItem;
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

		// Get existing filters and add the new one
		ArrayList<Tag> filters = ((LibraryFragment) fm
				.findFragmentById(R.id.library))
				.copyFilters();
		String fragmentName = filters.isEmpty()
		                      ? LibraryActivity.LIBRARY_ROOT_TAG : null;
		filters.add((Tag) adapter.getItem(position));

		// Create a new fragment
		fm.beginTransaction()
		  .replace(R.id.library, LibraryFragment.newInstance(filters))
		  .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		  .addToBackStack(fragmentName)
		  .commit();
	}
}
