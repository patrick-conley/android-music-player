package io.github.patrickconley.arbutus.library.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntryText;
import io.github.patrickconley.arbutus.library.R;
import io.github.patrickconley.arbutus.library.view.LibraryEntryFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LibraryEntryText} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class LibraryEntryViewAdapter extends RecyclerView.Adapter<LibraryEntryViewHolder>
        implements Observer<List<LibraryEntryText>> {

    private List<LibraryEntryText> contents = new LinkedList<>();
    private final OnListFragmentInteractionListener listener;

    LibraryEntryViewAdapter(@NonNull OnListFragmentInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChanged(List<LibraryEntryText> entries) {
        this.contents.addAll(entries);
    }

    @NonNull
    @Override
    public LibraryEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.library_entry_view, parent, false);
        return new LibraryEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LibraryEntryViewHolder holder, int position) {
        holder.setEntry(contents.get(position));
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                listener.onListFragmentInteraction(holder.getEntry());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

}
