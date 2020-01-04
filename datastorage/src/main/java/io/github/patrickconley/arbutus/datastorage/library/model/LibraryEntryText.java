package io.github.patrickconley.arbutus.datastorage.library.model;

import androidx.annotation.NonNull;
import androidx.room.DatabaseView;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;

// FIXME should this be implemented in the 'library' module instead of 'datastorage'?
@DatabaseView("select LibraryEntry.id as entryId, " //
              + "LibraryEntry.parentId as parentId, " //
              + "Tag.value as text " //
              + "from LibraryEntry " //
              + "left outer join tag on LibraryEntry.tagId = Tag.id")
public class LibraryEntryText {

    private final long entryId;
    private final Long parentId;
    private final String text;

    public LibraryEntryText(long entryId, Long parentId, String text) {
        this.entryId = entryId;
        this.parentId = parentId;
        this.text = text;
    }

    public LibraryEntryText(@NonNull LibraryEntry entry, Tag tag) {
        this.entryId = entry.getId();
        this.parentId = entry.getParentId();
        this.text = tag == null ? null : tag.getValue();
    }

    public long getEntryId() {
        return entryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        LibraryEntryText other = (LibraryEntryText) obj;

        return new EqualsBuilder().append(entryId, other.entryId).append(parentId, other.parentId)
                                  .append(text, other.text).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(entryId).append(parentId).append(text)
                                          .toHashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("entryId", entryId).append("parentId", parentId)
                                        .append("text", text).toString();
    }
}
