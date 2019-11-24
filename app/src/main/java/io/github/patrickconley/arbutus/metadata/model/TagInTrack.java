package io.github.patrickconley.arbutus.metadata.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = Track.class, parentColumns = "id", childColumns = "trackId",
                    onDelete = CASCADE),
        @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tagId")
}, indices = { @Index("trackId"), @Index("tagId") })
public class TagInTrack {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private final long trackId;
    private final long tagId;

    public TagInTrack(long trackId, long tagId) {
        this.trackId = trackId;
        this.tagId = tagId;
    }

    public TagInTrack(Track track, Tag tag) {
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

        TagInTrack trackTag = (TagInTrack) obj;

        return new EqualsBuilder().append(trackId, trackTag.trackId).append(tagId, trackTag.tagId)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(trackId).append(tagId).toHashCode();
    }
}
