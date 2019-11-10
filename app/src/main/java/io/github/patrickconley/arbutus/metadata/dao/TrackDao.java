package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import io.github.patrickconley.arbutus.metadata.model.Track;

@Dao
public abstract class TrackDao {

    @Insert
    abstract long insertForId(Track track);

    public Track insert(Track track) {
        track.setId(insertForId(track));
        return track;
    }

    @Query("delete from track")
    public abstract void truncate();
}
