package io.github.patrickconley.arbutus.library.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.patrickconley.arbutus.library.R;
import io.github.patrickconley.arbutus.library.view.dummy.DummyContent;

public class LibraryEntryViewHolder extends RecyclerView.ViewHolder {
    private final View view;
    private final TextView idView;
    private final TextView contentView;
    private DummyContent.DummyItem item;

    LibraryEntryViewHolder(View view) {
        super(view);
        this.view = view;
        idView = view.findViewById(R.id.item_number);
        contentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " '" + contentView.getText() + "'";
    }

    View getView() {
        return view;
    }

    DummyContent.DummyItem getItem() {
        return item;
    }

    void setItem(DummyContent.DummyItem item) {
        this.item = item;
        idView.setText(item.id);
        contentView.setText(item.content);
    }
}
