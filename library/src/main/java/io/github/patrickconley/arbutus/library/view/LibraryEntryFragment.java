package io.github.patrickconley.arbutus.library.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntryText;
import io.github.patrickconley.arbutus.library.R;
import io.github.patrickconley.arbutus.library.model.LibraryEntryViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class LibraryEntryFragment extends Fragment {

    private OnListFragmentInteractionListener interactionListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            interactionListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new IllegalStateException(
                    context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        final LibraryEntryViewAdapter adapter = new LibraryEntryViewAdapter(interactionListener);

        final RecyclerView view =
                (RecyclerView) inflater.inflate(R.layout.library_entry_list, container, false);
        view.setHasFixedSize(true);
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        view.setAdapter(adapter);

        LibraryEntryViewModel model = ViewModelProviders.of(this).get(LibraryEntryViewModel.class);
        model.getEntries(getContext()).observe(this, adapter);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(LibraryEntryText item);
    }

}
