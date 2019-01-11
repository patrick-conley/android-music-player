package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

@Entity(foreignKeys = {
        @ForeignKey(entity = LibraryItem.class, parentColumns = "id", childColumns = "parentId"),
        @ForeignKey(entity = LibraryNode.class, parentColumns = "id", childColumns = "nodeId"),
        @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tagId"),
        @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId")
})
public class LibraryItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    // FIXME should parentId/nodeId/tagId be a unique tuple?
    private Long parentId;
    private long nodeId;
    private Long tagId;
    private Long trackId;

    public LibraryItem(Long parentId, long nodeId, Long tagId, Long trackId) {
        this.parentId = parentId;
        this.nodeId = nodeId;
        this.tagId = tagId;
        this.trackId = trackId;
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

}
