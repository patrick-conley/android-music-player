package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import io.github.patrickconley.arbutus.metadata.model.TrackTag;

@Dao
public interface TrackTagDao {

    @Insert
    long insert(TrackTag trackTag);

    @Query("delete from tracktag")
    void truncate();

}
