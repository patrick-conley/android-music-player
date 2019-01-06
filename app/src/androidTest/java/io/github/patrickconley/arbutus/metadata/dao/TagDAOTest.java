package io.github.patrickconley.arbutus.metadata.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.patrickconley.arbutus.metadata.AppDatabase;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TagDAOTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private TagDAO dao = db.tagDao();

    @After
    public void after() {
        db.close();
    }

    @Test
    public void insertShouldReturnValidId() {
        assertTrue(0 < dao.insert(new Tag("key", "insertShouldReturnValidId")));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldNotReplaceDuplicates() {
        Tag tag = new Tag("key", "insertShouldNotReplaceDuplicates");
        dao.insert(tag);
        dao.insert(tag);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = SQLiteConstraintException.class)
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
        dao.insert(new Tag("key", "getShouldReturnNothingAfterTruncate"));
        dao.truncate();
        assertNull(dao.getTag(new Tag("key", "getShouldReturnNothingAfterTruncate")));
    }

    @Test
    public void getShouldReturnNothingOnMissing() {
        dao.insert(new Tag("key", "getShouldReturnNothingOnMissing"));
        assertNull(dao.getTag(new Tag("missing", "missing")));
    }

    @Test
    public void getTag() {
        long id = dao.insert(new Tag("key", "getTag"));
        Tag actual = dao.getTag(new Tag("key", "getTag"));

        assertEquals(id, actual.getTagId());
    }
}
