package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryItem;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.dao.TagDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackDAO;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class LibraryItemDAOTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private LibraryItemDAO dao = db.libraryItemDao();
    private LibraryNodeDAO nodeDao = db.libraryNodeDao();
    private LibraryContentTypeDAO contentTypeDao = db.libraryContentTypeDao();
    private TagDAO tagDao = db.tagDao();
    private TrackDAO trackDao = db.trackDao();

    private long nodeId;
    private long tagId;
    private long trackId;

    @After
    public void after() {
        db.close();
    }

    @Before
    public void before() {
        contentTypeDao.insert(new LibraryContentType(LibraryContentType.Type.Tag));
        nodeId = nodeDao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));
        tagId = tagDao.insert(new Tag("key", "value"));
        trackId = trackDao.insert(new Track(Uri.parse("file:///sample.ogg")));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidParentId() {
        dao.insert(new LibraryItem(-1L, nodeId, tagId, trackId));
    }

    @Test
    public void insertShouldAllowNullParentId() {
        dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidNodeId() {
        long id = dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
        dao.insert(new LibraryItem(id, -1, tagId, trackId));
    }

    @Test
    public void insertShouldAllowDuplicateParentNodeId() {
        long id = dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
        long child1 = dao.insert(new LibraryItem(id, nodeId, tagId, trackId));
        long child2 = dao.insert(new LibraryItem(id, nodeId, tagId, trackId));

        assertNotEquals(child1, child2);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidTagId() {
        long id = dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
        dao.insert(new LibraryItem(id, nodeId, -1L, trackId));
    }

    @Test
    public void insertShouldAllowNullTagId() {
        long id = dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
        dao.insert(new LibraryItem(id, nodeId, null, trackId));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidTrackId() {
        long id = dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
        dao.insert(new LibraryItem(id, nodeId, tagId, -1L));
    }

    @Test
    public void insertShouldAllowNullTrackId() {
        long id = dao.insert(new LibraryItem(null, nodeId, tagId, trackId));
        dao.insert(new LibraryItem(id, nodeId, tagId, null));
    }

}
