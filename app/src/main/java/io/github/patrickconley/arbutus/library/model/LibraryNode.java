package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity(foreignKeys = {
        @ForeignKey(entity = LibraryContentType.class, parentColumns = "id",
                    childColumns = "contentTypeId"),
        @ForeignKey(entity = LibraryNode.class, parentColumns = "id", childColumns = "parentId")
}, indices = {
        @Index(value = "parentId")
})
public class LibraryNode {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Long parentId;
    private long contentTypeId;
    @NonNull
    private String name;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LibraryNode that = (LibraryNode) o;

        return new EqualsBuilder().append(contentTypeId, that.contentTypeId)
                                  .append(parentId, that.parentId).append(name, that.name)
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
