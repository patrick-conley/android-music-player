package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Track;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class TrackDaoTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TrackDao dao = db.trackDao();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        assertThat(dao.insert(new Track(Uri.parse("file:///sample.ogg"))).getId()).isGreaterThan(0);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = Exception.class)
    public void insertShouldFailWithMissingUri() {
        dao.insert(new Track(null));
    }

}
