package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

@Entity(foreignKeys = {
    @ForeignKey(entity = LibraryNode.class, parentColumns = "id", childColumns = "nodeId"),
    @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tagId"),
    @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId")
})
public class LibraryItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long parentId;
    private long nodeId;
    private long tagId;
    private long trackId;

    public LibraryItem(long parentId, long nodeId, long tagId, long trackId) {
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

    public long getParentId() {
        return parentId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public long getTagId() {
        return tagId;
    }

    public long getTrackId() {
        return trackId;
    }

}
