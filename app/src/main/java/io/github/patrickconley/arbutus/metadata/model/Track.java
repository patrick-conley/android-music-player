package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

@Entity
public class Track {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "track_id")
    private long trackId;

    @NonNull
    private Uri uri;

    public Track(@NonNull Uri uri) {
        this.uri = uri;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public @NonNull Uri getUri() {
        return uri;
    }

    public void setUri(@NonNull Uri uri) {
        this.uri = uri;
    }
}
