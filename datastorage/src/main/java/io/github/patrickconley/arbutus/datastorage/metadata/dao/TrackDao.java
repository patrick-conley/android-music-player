package io.github.patrickconley.arbutus.datastorage.metadata.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

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

    @Query("select * from track")
    @Deprecated // Only use this in unit tests
    public abstract List<Track> getAll();
}
