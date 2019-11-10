package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

@Entity(foreignKeys = {
        @ForeignKey(entity = LibraryEntry.class, parentColumns = "id", childColumns = "parentId"),
        @ForeignKey(entity = LibraryNode.class, parentColumns = "id", childColumns = "nodeId"),
        @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tagId"),
        @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId")
})
public class LibraryEntry {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Long parentId;
    private long nodeId;
    private Long tagId;
    private Long trackId;

    public LibraryEntry(Long parentId, long nodeId, Long tagId, Long trackId) {
        this.parentId = parentId;
        this.nodeId = nodeId;
        this.tagId = tagId;
        this.trackId = trackId;
    }

    public LibraryEntry(LibraryEntry parent, @NonNull LibraryNode node, Tag tag, Track track) {
        this.parentId = parent == null ? null : parent.getId();
        this.nodeId = node.getId();
        this.tagId = tag == null ? null : tag.getId();
        this.trackId = track == null ? null : track.getId();
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

    public long getNodeId() {
        return nodeId;
    }

    public Long getTagId() {
        return tagId;
    }

    public Long getTrackId() {
        return trackId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LibraryEntry that = (LibraryEntry) o;

        return new EqualsBuilder().append(nodeId, that.nodeId).append(parentId, that.parentId)
                                  .append(tagId, that.tagId).append(trackId, that.trackId)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(parentId).append(nodeId).append(tagId)
                                          .append(trackId).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("parentId", parentId).append("nodeId", nodeId)
                                        .append("tagId", tagId).append("trackId", trackId)
                                        .toString();
    }
}
