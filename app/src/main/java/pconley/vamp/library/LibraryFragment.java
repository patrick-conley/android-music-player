package pconley.vamp.library;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.action.LibraryActionLocator;
import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.dao.TagDAO;
import pconley.vamp.persistence.dao.TrackDAO;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

public class LibraryFragment extends Fragment
		implements AdapterView.OnItemClickListener {

	private Activity activity;
	private ProgressBar progress;
	private ListView list;

	MusicCollection collection;
	List<? extends LibraryItem> contents;

	/**
	 * Create a new unfiltered fragment. The library's root tags will be
	 * displayed.
	 */
	public static LibraryFragment newInstance() {
		return newInstance(null, null);
	}

	/**
	 * Create a new fragment, filtering the library against a set of tags.
	 *
	 * @param parentCollection
	 * 		The collection used in this fragment's parent. Should be null if this
	 * 		is the root fragment.
	 * @param selected
	 * 		The item clicked in the parent fragment. Null if this is the root
	 * 		fragment.
	 */
	public static LibraryFragment newInstance(MusicCollection parentCollection,
			Tag selected) {

		Bundle arguments = new Bundle();
		arguments.putParcelable("parentCollection", parentCollection);
		arguments.putParcelable("selectedTag", selected);

		LibraryFragment fragment = new LibraryFragment();
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = getActivity();
	}

	/**
	 * Initialize views, parse the fragment's arguments (parent collection and
	 * just-clicked tag), and load data
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.fragment_library, container, false);

		list = (ListView) view.findViewById(R.id.library_contents);
		list.setOnItemClickListener(this);

		progress = (ProgressBar) view.findViewById(R.id.library_progress_load);

		MusicCollection parentCollection = null;
		Tag selectedTag = null;

		Bundle arguments = getArguments();
		if (arguments != null) {
			parentCollection = arguments.getParcelable("parentCollection");
			selectedTag = arguments.getParcelable("selectedTag");

			if (parentCollection == null && selectedTag != null) {
				// FIXME: I see no solid reason to prohibit this case -
				// FIXME: deal with it in the AsyncTask when it's generalized.
				throw new IllegalArgumentException(
						"Parent collection is unset");
			} else if (parentCollection != null && selectedTag == null) {
				throw new IllegalArgumentException("Selected tag is unset");
			}

		}

		new LoadCollectionTask(parentCollection, selectedTag).execute();

		return view;
	}

	/**
	 * Callback for items selected in a {@Link LibraryFragment}. Selecting a Tag
	 * will replace the fragment with a new, filtered fragment; selecting a
	 * Track will play the fragment's contents.
	 *
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	@Override
	@SuppressWarnings(value = "unchecked")
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ArrayAdapter<LibraryItem> adapter
				= (ArrayAdapter<LibraryItem>) parent.getAdapter();

		LibraryActionLocator.findAction(adapter.getItem(position))
		                    .execute((LibraryActivity) activity, adapter,
		                             position);
	}

	/**
	 * Get the collection this fragment represents.
	 *
	 * @return Collection used to build this part of the library.
	 * @throws IllegalStateException
	 * 		If this method is called before the LoadCollectionTask returns from its
	 * 		background thread. The library activity must obstruct the UI to prevent
	 * 		this.
	 */
	public MusicCollection getCollection() throws IllegalStateException {
		if (collection == null) {
			throw new IllegalStateException("Collection has not been built");
		}
		return collection;
	}

	/**
	 * Get the library contents. I rely on it for testing as the filtered
	 * library doesn't appear to be displayed properly by Robolectric.
	 *
	 * @return The items being displayed in the library.
	 * @throws IllegalStateException
	 * 		If this is called before the collection has been obtained from the DB.
	 */
	public List<? extends LibraryItem> getContents() {
		if (collection == null) {
			throw new IllegalStateException("Contents have not been built");
		}
		return contents;
	}

	/**
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread.
	 */
	private class LoadCollectionTask
			extends AsyncTask<Void, Void, List<? extends LibraryItem>> {

		private MusicCollection coll;
		private final MusicCollection parentCollection;
		private final Tag selectedTag;

		private TrackDAO trackDAO;
		private TagDAO tagDAO;

		public LoadCollectionTask(MusicCollection parentCollection,
				Tag selectedTag) {
			this.parentCollection = parentCollection;
			this.selectedTag = selectedTag;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(ProgressBar.VISIBLE);

			LibraryOpenHelper helper = new LibraryOpenHelper(activity);
			trackDAO = new TrackDAO(helper);
			tagDAO = new TagDAO(helper);
		}

		@Override
		protected List<? extends LibraryItem> doInBackground(Void... params) {
			if (parentCollection == null) {
				coll = new MusicCollection(null, "artist");
				return tagDAO.getTagsInCollection(coll);
			} else {
				List<Tag> history = new ArrayList<Tag>(
						parentCollection.getHistory());
				history.add(selectedTag);

				if (parentCollection.getSelection() == null) {
					throw new IllegalStateException("Wut? Duplicate state");
//					coll = new MusicCollection(history, null);
//					return dao.getTracksWithCollection(coll);
				}

				switch (parentCollection.getSelection()) {
					case "artist":
								coll = new MusicCollection(history, "album");
								return tagDAO.getTagsInCollection(coll);
					case "album":
						coll = new MusicCollection(history, null);
						return trackDAO.getTracksWithCollection(coll);
					default:
						throw new IllegalArgumentException(
								"Unexpected tag name " +
								parentCollection.getSelection());
				}

			}
		}

		@SuppressWarnings(value = "unchecked")
		protected void onPostExecute(List<? extends LibraryItem> items) {
			collection = coll;
			contents = items;

			ArrayAdapter<? extends LibraryItem> adapter;
			if (collection.getSelection() == null) {
				activity.setTitle(getString(R.string.track));
				adapter = new ArrayAdapter<Track>(
						activity, R.layout.library_item, R.id.library_item,
						(List<Track>) items);
			} else {
				activity.setTitle(WordUtils.capitalize(
						collection.getSelection()));
				adapter = new ArrayAdapter<Tag>(
						activity, R.layout.library_item, R.id.library_item,
						(List<Tag>) items);

			}

			list.setAdapter(adapter);

/*
			// Skip through solitary items
			// FIXME: Uncomment after solving issues w. history when going back
			if (items.size() == 1 && collection.getSelection() != null) {
				new LibraryFilterAction().execute(
						(LibraryActivity) LibraryFragment.this.activity,
						(ArrayAdapter<LibraryItem>) adapter, items.size() - 1);
			}
*/

			progress.setVisibility(ProgressBar.INVISIBLE);
		}
	}

}
