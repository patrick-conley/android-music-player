package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.patrickconley.arbutus.metadata.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TrackTagDAOTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TagDAO tagDao = db.tagDao();
    private TrackDAO trackDao = db.trackDao();
    private TrackTagDAO dao = db.trackTagDAO();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        long trackId = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        long tagId = tagDao.insert(new Tag("key", "insertShouldReturnValidId"));

        assertTrue(0 < dao.insert(new TrackTag(trackId, tagId)));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailOnInvalidTrackId() {
        long trackId = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        long tagId = tagDao.insert(new Tag("key", "insertShouldFailOnInvalidTrackId"));

        dao.insert(new TrackTag(trackId + 1, tagId));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailOnInvalidTagId() {
        long trackId = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        long tagId = tagDao.insert(new Tag("key", "insertShouldFailOnInvalidTagId"));

        dao.insert(new TrackTag(trackId, tagId + 1));
    }

}
