package io.github.patrickconley.arbutus.library.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import io.github.patrickconley.arbutus.library.R;
import io.github.patrickconley.arbutus.library.view.LibraryEntryFragment.OnListFragmentInteractionListener;
import io.github.patrickconley.arbutus.library.view.dummy.DummyContent.DummyItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}. TODO: Replace the implementation with code
 * for your data type.
 */
public class LibraryEntryViewAdapter extends RecyclerView.Adapter<LibraryEntryViewHolder> {

    private final List<DummyItem> contents;
    private final OnListFragmentInteractionListener listener;

    LibraryEntryViewAdapter(
            List<DummyItem> contents, @NonNull OnListFragmentInteractionListener listener
    ) {
        this.contents = new LinkedList<>(contents);
        this.listener = listener;
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
        holder.setItem(contents.get(position));
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                listener.onListFragmentInteraction(holder.getItem());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

}
