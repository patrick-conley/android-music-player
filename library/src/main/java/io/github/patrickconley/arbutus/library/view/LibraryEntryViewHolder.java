package io.github.patrickconley.arbutus.library.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntryText;
import io.github.patrickconley.arbutus.library.R;

public class LibraryEntryViewHolder extends RecyclerView.ViewHolder {
    private final View view;
    private final TextView entryTextView;
    private LibraryEntryText entry;

    LibraryEntryViewHolder(View view) {
        super(view);
        this.view = view;
        entryTextView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " '" + entryTextView.getText() + "'";
    }

    View getView() {
        return view;
    }

    LibraryEntryText getEntry() {
        return entry;
    }

    void setEntry(LibraryEntryText entry) {
        this.entry = entry;
        entryTextView.setText(entry.getText());
    }
}
