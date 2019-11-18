package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

@Entity
public class Track {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private Uri uri;

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

    public void setUri(@NonNull Uri uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
