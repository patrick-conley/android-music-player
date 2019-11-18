package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class TrackTagDaoTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TagDao tagDao = db.tagDao();
    private TrackDao trackDao = db.trackDao();
    private TrackTagDao dao = db.trackTagDao();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        Track track = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        Tag tag = tagDao.insert(new Tag("key", "insertShouldReturnValidId"));

        assertThat(dao.insert(new TrackTag(track, tag)).getId()).isGreaterThan(0);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailOnInvalidTrackId() {
        Track track = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        Tag tag = tagDao.insert(new Tag("key", "insertShouldFailOnInvalidTrackId"));

        dao.insert(new TrackTag(track.getId() + 1L, tag.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailOnInvalidTagId() {
        Track track = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        Tag tag = tagDao.insert(new Tag("key", "insertShouldFailOnInvalidTagId"));

        dao.insert(new TrackTag(track.getId(), tag.getId() + 1L));
    }

}
