package io.github.patrickconley.arbutus.datastorage;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.patrickconley.arbutus.library.dao.LibraryContentTypeDao;
import io.github.patrickconley.arbutus.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.dao.Converters;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;

@Database(entities = {
        LibraryContentType.class, LibraryEntry.class, LibraryNode.class, Tag.class, Track.class,
        TrackTag.class
}, version = 1)
@TypeConverters({ Converters.class })
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "arbutus.db";

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(final Context context) {
        if (instance == null) {
            instance = buildDatabase(context);
        }
        return instance;
    }

    private static AppDatabase buildDatabase(final Context context) {

        return Room
                .databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                .addCallback(new HandlePopulateDatabase(context)).build();
    }

    public abstract LibraryContentTypeDao libraryContentTypeDao();

    public abstract LibraryEntryDao libraryEntryDao();

    public abstract LibraryNodeDao libraryNodeDao();

    public abstract TagDao tagDao();

    public abstract TrackDao trackDao();

    public abstract TrackTagDao trackTagDao();

    // TODO test this by extracting the callback and adding that to the test framework's in-memory DB
    private static class HandlePopulateDatabase extends Callback {
        private final Context context;

        HandlePopulateDatabase(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            AppDatabase appDb = getInstance(context);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new LibraryContentTypePopulator(appDb));
            executorService.execute(new LibraryNodePopulator(appDb));
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(AppDatabase.class.getName(), "Interrupted", e);
            }
        }
    }
}
