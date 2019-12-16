package io.github.patrickconley.arbutus.datastorage;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryContentTypeDao;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryNode;
import io.github.patrickconley.arbutus.datastorage.metadata.dao.Converters;
import io.github.patrickconley.arbutus.datastorage.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.datastorage.metadata.dao.TagInTrackDao;
import io.github.patrickconley.arbutus.datastorage.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.datastorage.metadata.model.TagInTrack;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

@Database(entities = {
        LibraryContentType.class, LibraryEntry.class, LibraryNode.class, Tag.class, Track.class,
        TagInTrack.class
}, version = 1, exportSchema = false)
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

    public abstract TagInTrackDao tagInTrackDao();

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
                executorService.awaitTermination(10L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(AppDatabase.class.getName(), "Interrupted", e);
            }
        }
    }
}
