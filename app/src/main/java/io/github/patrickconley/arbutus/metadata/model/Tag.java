package io.github.patrickconley.arbutus.metadata.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity(indices = {
        @Index(value = { "key", "value" }, unique = true)
})
public class Tag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private final String key;

    @NonNull
    private final String value;

    public Tag(@NonNull String key, @NonNull String value) {
        this.key = key.toLowerCase();
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Tag tag = (Tag) obj;

        return new EqualsBuilder().append(key, tag.key).append(value, tag.value).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(key).append(value).toHashCode();
    }
}
