package io.github.patrickconley.arbutus.domain;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import io.github.patrickconley.arbutus.domain.dao.Converters;
import io.github.patrickconley.arbutus.domain.dao.TagDAO;
import io.github.patrickconley.arbutus.domain.dao.TrackDAO;
import io.github.patrickconley.arbutus.domain.model.Tag;
import io.github.patrickconley.arbutus.domain.model.Track;
import io.github.patrickconley.arbutus.domain.model.TrackTag;

@Database(entities = { Track.class, Tag.class, TrackTag.class }, version = 1)
@TypeConverters({ Converters.class })
public abstract class AppDatabase extends RoomDatabase {

    public abstract TrackDAO trackDao();

    public abstract TagDAO tagDao();
}
