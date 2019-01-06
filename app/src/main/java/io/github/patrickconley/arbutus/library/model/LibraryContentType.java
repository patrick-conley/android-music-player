package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class LibraryContentType {

    public enum Type {
        Tag(1), Track(2);

        private long id;

        Type(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    @PrimaryKey
    private long id;

    private String type;

    public LibraryContentType(long id, String type) {
        this.id = id;
        this.type = type;
    }

    public LibraryContentType(Type type) {
        this.id = type.getId();
        this.type = type.name();
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
