package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId",
                    onDelete = CASCADE),
        @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tagId")
}, indices = { @Index("trackId"), @Index("tagId") })
public class TrackTag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private final long trackId;
    private final long tagId;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TrackTag trackTag = (TrackTag) obj;

        return new EqualsBuilder().append(trackId, trackTag.trackId).append(tagId, trackTag.tagId)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(trackId).append(tagId).toHashCode();
    }
}
