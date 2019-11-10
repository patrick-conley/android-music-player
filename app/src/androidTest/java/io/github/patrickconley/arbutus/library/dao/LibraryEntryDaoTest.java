package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.junit.Assert.assertNull;

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

    @After
    public void after() {
        db.close();
    }

    @Before
    public void before() {
        contentTypeDao.insert(new LibraryContentType(LibraryContentType.Type.Tag));
        nodeDao.insert(node);
        tagDao.insert(tag);
        trackDao.insert(track);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void invalidParent() {
        dao.insert(new LibraryEntry(-1L, node.getId(), tag.getId(), track.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidNode() {
        LibraryEntry root = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(root.getId(), -1, tag.getId(), track.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void invalidTag() {
        LibraryEntry root = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(root.getId(), node.getId(), -1L, track.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void invalidTrack() {
        LibraryEntry root = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(root.getId(), node.getId(), tag.getId(), -1L));
    }

    @Test
    public void trackAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, tag, track));
        assertEquals(entry, dao.getEntry(null, tag, track));
    }

    @Test
    public void tagAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, tag, null));
        assertEquals(entry, dao.getEntry(null, tag, null));
    }

    @Test
    public void nullTagAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, null, null));
        assertEquals(entry, dao.getEntry((LibraryEntry) null, null, null));
    }

    @Test
    public void trackWithNullTagAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, null, track));
        assertEquals(entry, dao.getEntry(null, null, track));
    }

    @Test
    public void tagBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, tag, null));

        assertEquals(entry, dao.getEntry(parent, tag, null));
    }

    @Test
    public void nullTagBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, null, null));

        assertEquals(entry, dao.getEntry(parent, null, null));
    }

    @Test
    public void trackBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, tag, track));
        assertEquals(entry, dao.getEntry(parent, tag, track));
    }

    @Test
    public void trackWithNullTagBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, null, track));

        assertEquals(entry, dao.getEntry(parent, null, track));
    }

    @Test
    public void insertTracksWithSameParent() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));

        Track track1 = trackDao.insert(new Track(Uri.parse("file://sample.ogg")));
        LibraryEntry child1 = dao.insert(new LibraryEntry(parent, node, tag, track1));

        Track track2 = trackDao.insert(new Track(Uri.parse("file://sample.mp3")));
        LibraryEntry child2 = dao.insert(new LibraryEntry(parent, node, tag, track2));

        assertNotEquals(child1, child2);
        assertNotEquals(child1.getId(), child2.getId());
        assertEquals(child1, dao.getEntry(parent, tag, track1));
        assertEquals(child2, dao.getEntry(parent, tag, track2));
    }

    @Test
    public void getMissingEntry() {
        dao.insert(new LibraryEntry(null, node, tag, track));
        LibraryEntry root2 = new LibraryEntry(null, node, null, null);
        assertNull(dao.getEntry(root2, tag, track));
    }

}
