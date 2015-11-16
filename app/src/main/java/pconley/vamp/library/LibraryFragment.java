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
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.action.LibraryActionLocator;
import pconley.vamp.library.action.LibraryPlayAction;
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
	private ListView view;

	private MusicCollection collection;

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

		this.view = (ListView) view.findViewById(R.id.library_contents);
		this.view.setOnItemClickListener(this);

		progress = (ProgressBar) view.findViewById(R.id.library_progress_load);

		MusicCollection parentCollection = null;
		Tag selectedTag = null;

		Bundle arguments = getArguments();
		if (arguments != null) {
			parentCollection = arguments.getParcelable("parentCollection");
			selectedTag = arguments.getParcelable("selectedTag");
		}

		buildCollection(parentCollection, selectedTag);

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
		LibraryActionLocator.findAction(collection.getContents().get(position))
		                    .execute((LibraryActivity) activity,
		                             collection.getContents(),
		                             position);
	}

	/**
	 * Play all the tracks contained in the visible collections.
	 */
	public void playContents() {
		new LoadCollectionTask(null, collection.getFilter(), true).execute();
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

	private void buildCollection(MusicCollection parent, Tag selected) {
		List<Tag> filter = new LinkedList<Tag>();
		String name;

		if (parent == null) {
			name = "artist";
		} else {
			filter.addAll(parent.getFilter());

			switch (parent.getName()) {
				case "artist":
					name = "album";
					break;
				case "album":
					name = null;
					break;
				default:
					throw new IllegalArgumentException(
							"Unknown collection name " + parent.getName());
			}
		}

		if (selected != null) {
			filter.add(selected);
		}

		new LoadCollectionTask(name, filter, false).execute();
	}

	/**
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread.
	 */
	private class LoadCollectionTask
			extends AsyncTask<Void, Void, List<? extends LibraryItem>> {

		private final String name;
		private final List<Tag> filter;
		private final boolean playOnLoad;

		public LoadCollectionTask(String name, List<Tag> filter,
				boolean playOnLoad) {
			this.name = name;
			this.filter = filter == null ? new ArrayList<Tag>() : filter;
			this.playOnLoad = playOnLoad;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(ProgressBar.VISIBLE);

			if (name == null) {
				activity.setTitle(getString(R.string.track));
			} else {
				activity.setTitle(WordUtils.capitalize(name));
			}
		}

		@Override
		protected List<? extends LibraryItem> doInBackground(Void... params) {
			LibraryOpenHelper helper = new LibraryOpenHelper(activity);

			if (name == null) {
				return new TrackDAO(helper).getFilteredTracks(filter);
			} else {
				return new TagDAO(helper).getFilteredTags(filter, name);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(List<? extends LibraryItem> items) {
			if (playOnLoad) {
				new LibraryPlayAction()
						.execute((LibraryActivity) activity, items, 0);
			} else {
				collection = new MusicCollection(name, filter, items);

				if (items.isEmpty()) {
					Toast.makeText(activity, "Filter contains no tracks",
					               Toast.LENGTH_LONG).show();
				}

				ArrayAdapter<? extends LibraryItem> adapter;
				if (name == null) {
					adapter = new ArrayAdapter<Track>(
							activity, R.layout.library_item, R.id.library_item,
							(List<Track>) items);
				} else {
					adapter = new ArrayAdapter<Tag>(
							activity, R.layout.library_item, R.id.library_item,
							(List<Tag>) items);
				}
				view.setAdapter(adapter);
			}

			progress.setVisibility(ProgressBar.INVISIBLE);
		}
	}

}
