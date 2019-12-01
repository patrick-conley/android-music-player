package io.github.patrickconley.arbutus.datastorage.metadata.dao;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.datastorage.metadata.model.TagInTrack;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class TagInTrackDaoTest {

    private Context context = ApplicationProvider.getApplicationContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TagDao tagDao = db.tagDao();
    private TrackDao trackDao = db.trackDao();
    private TagInTrackDao dao = db.tagInTrackDao();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        Track track = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        Tag tag = tagDao.insert(new Tag("key", "insertShouldReturnValidId"));

        assertThat(dao.insert(new TagInTrack(track, tag)).getId()).isGreaterThan(0);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailOnInvalidTrackId() {
        Track track = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        Tag tag = tagDao.insert(new Tag("key", "insertShouldFailOnInvalidTrackId"));

        dao.insert(new TagInTrack(track.getId() + 1L, tag.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailOnInvalidTagId() {
        Track track = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
        Tag tag = tagDao.insert(new Tag("key", "insertShouldFailOnInvalidTagId"));

        dao.insert(new TagInTrack(track.getId(), tag.getId() + 1L));
    }

}
