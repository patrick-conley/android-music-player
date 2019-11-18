package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId",
                    onDelete = CASCADE),
        @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tagId")
}, indices = { @Index("trackId"), @Index("tagId") })
public class TrackTag {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long trackId;
    private long tagId;

    public TrackTag(long trackId, long tagId) {
        this.trackId = trackId;
        this.tagId = tagId;
    }

    public TrackTag(Track track, Tag tag) {
        this.trackId = track.getId();
        this.tagId = tag.getId();
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
