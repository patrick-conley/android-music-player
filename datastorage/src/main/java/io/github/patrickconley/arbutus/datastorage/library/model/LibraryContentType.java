package io.github.patrickconley.arbutus.datastorage.library.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LibraryContentType {

    public enum Type {
        TAG(1L), TRACK(2L);

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
