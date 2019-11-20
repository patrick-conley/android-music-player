package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.github.patrickconley.arbutus.metadata.model.TagInTrack;

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
