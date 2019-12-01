package io.github.patrickconley.arbutus.datastorage.metadata.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.github.patrickconley.arbutus.datastorage.metadata.model.TagInTrack;

@Dao
public abstract class TagInTrackDao {

    @Insert
    abstract long insertForId(TagInTrack trackTag);

    public TagInTrack insert(TagInTrack trackTag) {
        trackTag.setId(insertForId(trackTag));
        return trackTag;
    }

    @Query("delete from TagInTrack")
    public abstract void truncate();

    @Query("select * from TagInTrack")
    @Deprecated // Only use this in unit tests
    public abstract List<TagInTrack> getAll();
}
