package io.github.patrickconley.arbutus.metadata.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.github.patrickconley.arbutus.metadata.model.Tag;

@Dao
public abstract class TagDao {

    @Insert
    abstract long insertForId(Tag tag);

    public Tag insert(Tag tag) {
        tag.setId(insertForId(tag));
        return tag;
    }

    @Query("delete from tag")
    public abstract void truncate();

    @Query("select * from tag where \"key\" = :key and value = :value")
    abstract Tag getTagByKeyValue(String key, String value);

    public Tag getTag(Tag tag) {
        return getTagByKeyValue(tag.getKey(), tag.getValue());
    }

    @Query("select * from tag")
    @Deprecated // Only use this in unit tests
    public abstract List<Tag> getAll();
}
