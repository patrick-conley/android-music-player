package io.github.patrickconley.arbutus.domain.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import io.github.patrickconley.arbutus.domain.model.TrackTag;

@Dao
public interface TrackTagDAO {

    @Insert
    void insert(TrackTag trackTag);

    @Query("delete from tracktag")
    void truncate();

}
