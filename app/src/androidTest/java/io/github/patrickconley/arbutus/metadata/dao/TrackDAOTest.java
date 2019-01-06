package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.metadata.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Track;

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
    public void simple() {
        Track track = new Track();
        track.setUri(Uri.parse("file:///sample.ogg"));

    }
}
