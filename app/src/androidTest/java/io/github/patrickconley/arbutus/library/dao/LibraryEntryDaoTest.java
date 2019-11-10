package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class LibraryEntryDaoTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private LibraryEntryDao dao = db.libraryEntryDao();
    private LibraryNodeDao nodeDao = db.libraryNodeDao();
    private LibraryContentTypeDao contentTypeDao = db.libraryContentTypeDao();
    private TagDao tagDao = db.tagDao();
    private TrackDao trackDao = db.trackDao();

    private LibraryNode node = new LibraryNode(null, LibraryContentType.Type.Tag, "foo");
    private final Tag tag = new Tag("key", "value");
    private final Track track = new Track(Uri.parse("file:///sample.ogg"));

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
        nodeId = nodeDao.insert(node);
        tagId = tagDao.insert(tag);
        trackId = trackDao.insert(track);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidParent() {
        dao.insert(new LibraryEntry(-1L, nodeId, tagId, trackId));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidNode() {
        long id = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(id, -1, tagId, trackId));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidTag() {
        long id = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(id, nodeId, -1L, trackId));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertInvalidTrack() {
        long id = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(id, nodeId, tagId, -1L));
    }

    @Test
    public void trackAtRoot() {
        LibraryEntry entry = new LibraryEntry(null, node, tag, track);
        dao.insert(entry);
        assertEquals(entry, dao.getEntry(null, tag, track));
    }

    @Test
    public void tagAtRoot() {
        LibraryEntry entry = new LibraryEntry(null, node, tag, null);
        dao.insert(entry);
        assertEquals(entry, dao.getEntry(null, tag, null));
    }

    @Test
    public void nullTagAtRoot() {
        LibraryEntry entry = new LibraryEntry(null, node, null, null);
        dao.insert(entry);
        assertEquals(entry, dao.getEntry((LibraryEntry) null, null, null));
    }

    @Test
    public void trackBelowRoot() {
        LibraryEntry parent = new LibraryEntry(null, node, tag, null);
        parent.setId(dao.insert(parent));

        LibraryEntry entry = new LibraryEntry(parent, node, tag, track);
        entry.setId(dao.insert(entry));

        assertEquals(entry, dao.getEntry(parent, tag, track));
    }

    @Test public void getTrackBelowRoot() { }
    @Test public void getTagBelowRoot() { }
    @Test public void getNullTagBelowRoot() { }
    @Test public void getEntryWithInvalidParent() { }
    @Test public void getEntryWithInvalidTag() { }
    @Test public void getEntryWithInvalidTrack() { }

    @Test
    public void insertTracksWithSameParent() {
        LibraryEntry parent = new LibraryEntry(null, nodeId, tagId, null);
        parent.setId( dao.insert(parent));

        Track track1 = new Track(Uri.parse("file://sample.ogg"));
         track1.setId(trackDao.insert(track1));
        long child1 = dao.insert(new LibraryEntry(parent, node, tag, track1));

        Track track2 = new Track(Uri.parse("file://sample.mp3"));
        track2.setId(trackDao.insert(track2));
        long child2 = dao.insert(new LibraryEntry(parent, node, tag, track2));

        assertNotEquals(child1, child2);
        assertEquals(child1, dao.getEntry())
    }

    @Test
    public void insertTrackWithNullTag() {
        long id = dao.insert(new LibraryEntry(null, nodeId, tagId, null));
        dao.insert(new LibraryEntry(id, nodeId, null, trackId));
    }

    @Test
    public void insertInnerTag() {
        long id = dao.insert(new LibraryEntry(null, nodeId, tagId, null));
        dao.insert(new LibraryEntry(id, nodeId, tagId, null));
    }

}
