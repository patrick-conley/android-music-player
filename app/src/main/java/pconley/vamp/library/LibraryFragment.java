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

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.MusicCollection;
import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;

public class LibraryFragment extends Fragment {

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
	 * @param parent
	 * 		The collection used in this fragment's parent. Should be null if this
	 * 		is the root fragment.
	 * @param tag
	 * 		The item clicked in the parent fragment. Null if this is the root
	 * 		fragment.
	 */
	public static LibraryFragment newInstance(MusicCollection parent, Tag tag) {

		Bundle arguments = new Bundle();
		arguments.putParcelable("parent", parent);
		arguments.putParcelable("tag", tag);

		LibraryFragment fragment = new LibraryFragment();
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = getActivity();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.fragment_library, container, false);

		list = (ListView) view.findViewById(R.id.library_contents);
		list.setOnItemClickListener((AdapterView.OnItemClickListener) activity);

		progress = (ProgressBar) view.findViewById(R.id.library_progress_load);

		MusicCollection parent = null;
		Tag tag = null;

		Bundle arguments = getArguments();
		if (arguments != null) {
			parent = arguments.getParcelable("parent");
			tag = arguments.getParcelable("tag");

			if (parent == null && tag != null) {
				// FIXME: I see no solid reason to prohibit this case -
				// FIXME: deal with it in the AsyncTask when it's generalized.
				throw new IllegalArgumentException(
						"Parent collection is unset");
			} else if (parent != null && tag == null) {
				throw new IllegalArgumentException("Selected tag is unset");
			}

		}

		new LoadCollectionTask(parent, tag).execute();

		return view;
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

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread.
	 */
	private class LoadCollectionTask
			extends AsyncTask<Void, Void, List<? extends LibraryItem>> {

		private MusicCollection coll;
		private final MusicCollection parent;
		private final Tag tag;

		private TrackDAO dao;

		public LoadCollectionTask(MusicCollection parent, Tag tag) {
			this.parent = parent;
			this.tag = tag;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(ProgressBar.VISIBLE);
			dao = new TrackDAO(activity);
		}

		@Override
		protected List<? extends LibraryItem> doInBackground(Void... params) {
			dao.openReadableDatabase();

			if (parent == null) {
				coll = new MusicCollection(null, "artist");
				return dao.getTags(coll);
			} else {
				ArrayList<Tag> tags = new ArrayList<Tag>(parent.getTags());
				tags.add(tag);

				if (parent.getName() == null) {
					coll = new MusicCollection(tags, null);
					return dao.getTracks(coll);
				}

				switch (parent.getName()) {
					case "artist":
						coll = new MusicCollection(tags, "album");
						return dao.getTags(coll);
					case "album":
						coll = new MusicCollection(tags, null);
						return dao.getTracks(coll);
					default:
						throw new IllegalArgumentException(
								"Unexpected tag name");
				}
			}
		}

		@SuppressWarnings(value = "unchecked")
		protected void onPostExecute(List<? extends LibraryItem> items) {
			collection = coll;
			contents = items;

			ArrayAdapter<? extends LibraryItem> adapter;
			if (collection.getName() == null) {
				adapter = new ArrayAdapter<Track>(
						activity, R.layout.library_item, R.id.library_item,
						(List<Track>) items);
			} else {
				adapter = new ArrayAdapter<Tag>(
						activity, R.layout.library_item, R.id.library_item,
						(List<Tag>) items);

			}

			list.setAdapter(adapter);

			dao.close();
			progress.setVisibility(ProgressBar.INVISIBLE);
		}
	}

}
