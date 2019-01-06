package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(indices = {
        @Index(value = { "key", "value" }, unique = true)
})
public class Tag {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tag_id")
    private long tagId;

    private @NonNull String key;
    private @NonNull String value;

    public Tag(@NonNull String key, @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    public void setValue(@NonNull String value) {
        this.value = value;
    }

}
