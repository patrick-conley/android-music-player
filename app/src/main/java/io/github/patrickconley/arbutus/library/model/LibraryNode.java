package io.github.patrickconley.arbutus.library.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity(foreignKeys = {
        @ForeignKey(entity = LibraryContentType.class, parentColumns = "id",
                    childColumns = "contentTypeId"),
        @ForeignKey(entity = LibraryNode.class, parentColumns = "id", childColumns = "parentId")
}, indices = {
        @Index("parentId"), @Index("contentTypeId")
})
public class LibraryNode {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private final Long parentId;
    private final long contentTypeId;
    @NonNull
    private final String name;

    public LibraryNode(Long parentId, long contentTypeId, @NonNull String name) {
        this.parentId = parentId;
        this.contentTypeId = contentTypeId;
        this.name = name;
    }

    public LibraryNode(
            LibraryNode parent, @NonNull LibraryContentType.Type contentType, @NonNull String name
    ) {
        this.parentId = parent == null ? null : parent.getId();
        this.contentTypeId = contentType.getId();
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public long getContentTypeId() {
        return contentTypeId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        LibraryNode other = (LibraryNode) obj;

        return new EqualsBuilder().append(contentTypeId, other.contentTypeId)
                                  .append(parentId, other.parentId).append(name, other.name)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(parentId).append(contentTypeId).append(name)
                                          .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("parentId", parentId)
                                        .append("contentTypeId", contentTypeId).append("name", name)
                                        .toString();
    }
}
