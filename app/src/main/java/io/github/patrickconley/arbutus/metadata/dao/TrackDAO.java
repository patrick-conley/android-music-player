package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import io.github.patrickconley.arbutus.metadata.model.Track;

@Dao
public interface TrackDAO {

    @Insert
    long insert(Track track);

    @Query("delete from track")
    void truncate();
}
