package io.github.patrickconley.arbutus.metadata;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import io.github.patrickconley.arbutus.metadata.dao.Converters;
import io.github.patrickconley.arbutus.metadata.dao.TagDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDAO;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;

@Database(entities = { Track.class, Tag.class, TrackTag.class }, version = 1)
@TypeConverters({ Converters.class })
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "arbutus.db";

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    DATABASE_NAME).build();
        }
        return instance;
    }

    public abstract TrackDAO trackDao();

    public abstract TagDAO tagDao();

    public abstract TrackTagDAO trackTagDAO();

}
