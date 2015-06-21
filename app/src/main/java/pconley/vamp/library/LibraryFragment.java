package pconley.vamp.library;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.db.TrackDAO;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Tag;

public class LibraryFragment extends Fragment {

	private Activity activity;
	private ProgressBar progress;
	private ListView list;

	private ArrayList<Tag> filters;
	private List<LibraryItem> contents;

	/**
	 * Create a new unfiltered fragment. The library's root tags will be
	 * displayed.
	 */
	public static LibraryFragment newInstance() {
		return new LibraryFragment();
	}

	/**
	 * Create a new fragment, filtering the library against a set of tags.
	 *
	 * @param filters
	 * 		Tags to filter the library against. Filter tags are unordered but
	 * 		required to be in an array due to implementation.
	 */
	public static LibraryFragment newInstance(
			@Nullable ArrayList<Tag> filters) {
		Bundle arguments = new Bundle();
		arguments.putParcelableArrayList("filters", filters);

		LibraryFragment fragment = new LibraryFragment();
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = getActivity();

		Bundle arguments = getArguments();
		if (arguments == null) {
			filters = new ArrayList<Tag>();
		} else {
			filters = arguments.getParcelableArrayList("filters");
		}
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

		new LoadLibraryTask().execute();
		return view;
	}

	/**
	 * Get a list of active filters, in the order applied. The list is copied to
	 * prevent modification of the internal list.
	 *
	 * @return Filters active in this fragment.
	 */
	public ArrayList<Tag> copyFilters() {
		return new ArrayList<Tag>(filters);
	}

	/**
	 * Get the library contents. I rely on it for testing as the filtered
	 * library doesn't appear to be displayed properly by Robolectric.
	 *
	 * @return The items (Tracks or Tags) being displayed in the library.
	 */
	public List<LibraryItem> getContents() {
		return Collections.unmodifiableList(
				contents == null ? new LinkedList<LibraryItem>() : contents);
	}

	/*
	 * Load the contents of the library into a TextView with execute(). Work is
	 * done in a background thread.
	 */
	private class LoadLibraryTask extends AsyncTask<Void, Void, Object> {

		private TrackDAO dao;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(ProgressBar.VISIBLE);
			dao = new TrackDAO(activity);
		}

		@Override
		protected Object doInBackground(Void... params) {
			dao.openReadableDatabase();

			if (filters.isEmpty()) {
				return dao.getTag("album");
			} else {
				return dao.getTracks(filters.get(0));
			}
		}

		protected void onPostExecute(Object items) {
			contents = (List<LibraryItem>) items;

			list.setAdapter(new ArrayAdapter<LibraryItem>(
					activity, R.layout.library_item, R.id.library_item,
					contents));

			dao.close();
			progress.setVisibility(ProgressBar.INVISIBLE);
		}
	}

}
