package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.github.patrickconley.arbutus.metadata.model.TrackTag;

@Dao
public abstract class TrackTagDao {

    @Insert
    abstract long insertForId(TrackTag trackTag);

    public TrackTag insert(TrackTag trackTag) {
        trackTag.setId(insertForId(trackTag));
        return trackTag;
    }

    @Query("delete from tracktag")
    public abstract void truncate();

    @Query("select * from tracktag")
    @Deprecated // Only use this in unit tests
    public abstract List<TrackTag> getAll();
}
