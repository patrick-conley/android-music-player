package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.*;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = Track.class, parentColumns = "track_id", childColumns = "track_id", onDelete = CASCADE),
        @ForeignKey(entity = Tag.class, parentColumns = "tag_id", childColumns = "tag_id")
}, indices = {
        @Index("tag_id"),
        @Index(value = { "track_id", "tag_id" }, unique = true)
})
public class TrackTag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "track_id")
    private long trackId;

    @ColumnInfo(name = "tag_id")
    private long tagId;

    public TrackTag(long trackId, long tagId) {
        this.trackId = trackId;
        this.tagId = tagId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrackId() {
        return trackId;
    }

    public long getTagId() {
        return tagId;
    }
}
