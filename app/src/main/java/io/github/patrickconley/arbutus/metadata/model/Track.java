package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class Track {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private final Uri uri;

    public Track(@NonNull Uri uri) {
        this.uri = uri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public @NonNull Uri getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Track track = (Track) obj;

        return new EqualsBuilder().append(uri, track.uri).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uri).toHashCode();
    }
}
