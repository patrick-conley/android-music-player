package io.github.patrickconley.arbutus.library.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(foreignKeys = {
        @ForeignKey(entity = LibraryContentType.class, parentColumns = "id", childColumns = "contentTypeId"),
        @ForeignKey(entity = LibraryNode.class, parentColumns = "id", childColumns = "parentId")
}, indices = {
        @Index(value = "parentId")
})
public class LibraryNode {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Long parentId;
    private long contentTypeId;
    @NonNull
    private String name;

    public LibraryNode(Long parentId, long contentTypeId, @NonNull String name) {
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

    public Long getParentId() {
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
