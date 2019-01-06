package io.github.patrickconley.arbutus.datastorage;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import io.github.patrickconley.arbutus.library.dao.LibraryContentTypeDAO;
import io.github.patrickconley.arbutus.library.dao.LibraryItemDAO;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDAO;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryItem;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.dao.Converters;
import io.github.patrickconley.arbutus.metadata.dao.TagDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDAO;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Database(entities = { LibraryContentType.class, LibraryItem.class, LibraryNode.class, Tag.class, Track.class, TrackTag
        .class },
          version = 1)
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

        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                   .addCallback(new Callback() {
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
                   }).build();
    }

    public abstract LibraryContentTypeDAO libraryContentTypeDao();

    public abstract LibraryItemDAO libraryItemDao();

    public abstract LibraryNodeDAO libraryNodeDao();

    public abstract TagDAO tagDao();

    public abstract TrackDAO trackDao();

    public abstract TrackTagDAO trackTagDAO();

}
