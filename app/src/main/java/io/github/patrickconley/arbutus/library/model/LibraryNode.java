package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(foreignKeys = {
        @ForeignKey(entity = LibraryContentType.class, parentColumns = "id", childColumns = "contentTypeId")
})
public class LibraryNode {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long parentId;
    private long contentTypeId;
    private @NonNull String name;

    public LibraryNode(long parentId, long contentTypeId, @NonNull String name) {
        this.parentId = parentId;
        this.contentTypeId = contentTypeId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getParentId() {
        return parentId;
    }

    public long getContentTypeId() {
        return contentTypeId;
    }

    @NonNull
    public String getName() {
        return name;
    }

}
