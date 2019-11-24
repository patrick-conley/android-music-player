package io.github.patrickconley.arbutus.metadata.dao;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Tag;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class TagDaoTest {

    private Context context = ApplicationProvider.getApplicationContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TagDao dao = db.tagDao();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        assertThat(dao.insert(new Tag("key", "insertShouldReturnValidId")).getId())
                .isGreaterThan(0);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldNotReplaceDuplicates() {
        Tag tag = new Tag("key", "insertShouldNotReplaceDuplicates");
        dao.insert(tag);
        dao.insert(tag);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void insertShouldFailWithMissingKey() {
        dao.insert(new Tag(null, "value"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithMissingValue() {
        dao.insert(new Tag("key", null));
    }

    @Test
    public void getShouldReturnNothingOnEmpty() {
        Tag tag = new Tag("key", "getShouldReturnNothingAfterTruncate");
        dao.insert(tag);
        dao.truncate();
        assertNull(dao.getTag(tag));
    }

    @Test
    public void getShouldReturnNothingOnMissing() {
        dao.insert(new Tag("key", "getShouldReturnNothingOnMissing"));
        assertNull(dao.getTag(new Tag("missing", "missing")));
    }

    @Test
    public void getTag() {
        assertEquals(dao.insert(new Tag("key", "getTag")), dao.getTag(new Tag("key", "getTag")));
    }
}
