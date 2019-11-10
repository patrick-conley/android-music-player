package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Track;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TrackDAOTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TrackDAO dao = db.trackDao();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        assertTrue(0 < dao.insert(new Track(Uri.parse("file:///sample.ogg"))));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = Exception.class)
    public void insertShouldFailWithMissingUri() {
        dao.insert(new Track(null));
    }

}
