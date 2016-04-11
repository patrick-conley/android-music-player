package pconley.vamp.library.view;

import android.app.Activity;
import android.app.Fragment;
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

import java.util.List;

import pconley.vamp.R;
import pconley.vamp.library.action.LibraryActionLocator;
import pconley.vamp.library.action.LibraryPlayAction;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

public class LibraryFragment extends Fragment
		implements AdapterView.OnItemClickListener {

	private Activity activity;
	private ProgressBar progress;
	private ListView view;

	private LibraryActionLocator actionLocator;
	private MusicCollection collection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = getActivity();
		actionLocator = new LibraryActionLocator(activity);
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

		// FIXME: check if coll'n is nonnull here & update views in case of racing

		this.view = (ListView) view.findViewById(R.id.library_contents);
		this.view.setOnItemClickListener(this);

		progress = (ProgressBar) view.findViewById(R.id.library_progress_load);

		if (collection == null) {
			progress.setVisibility(ProgressBar.VISIBLE);
		} else {
			progress.setVisibility(ProgressBar.INVISIBLE);
			updateView();
		}

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
		actionLocator.findAction(getCollection().getContents().get(position))
		             .execute(getCollection(), position);
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
	 * Callback used to display the collection returned by LoadCollectionTask.
	 *
	 * @param collection
	 */
	public void setCollection(MusicCollection collection) {
		this.collection = collection;

		if (collection.getContents().isEmpty()) {
			Toast.makeText(activity, "Filter contains no tracks",
			               Toast.LENGTH_LONG).show();
		}

		if (progress != null) {
			progress.setVisibility(ProgressBar.INVISIBLE);
		}

		if (view != null) {
			updateView();
		}
	}

	@SuppressWarnings("unchecked")
	private void updateView() {
		ArrayAdapter<? extends LibraryItem> adapter;
		if (getCollection().getName() == null) {
			adapter = new ArrayAdapter<Track>(
					activity, R.layout.library_item, R.id.library_item,
					(List<Track>) collection.getContents());
		} else {
			adapter = new ArrayAdapter<Tag>(
					activity, R.layout.library_item, R.id.library_item,
					(List<Tag>) collection.getContents());
		}
		view.setAdapter(adapter);
	}

	/**
	 * Play all the tracks contained in the visible collections.
	 */
	public void playContents() {
		/* FIXME: don't display a progress bar? Or hide it somehow after load? */
		progress.setVisibility(ProgressBar.VISIBLE);
		new LibraryPlayAction(activity).execute(getCollection(), 0);
	}
}
