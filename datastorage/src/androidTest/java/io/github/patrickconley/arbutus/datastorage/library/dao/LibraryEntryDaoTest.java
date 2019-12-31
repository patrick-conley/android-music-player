package io.github.patrickconley.arbutus.datastorage.library.dao;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntryText;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryNode;
import io.github.patrickconley.arbutus.datastorage.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.datastorage.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class LibraryEntryDaoTest {

    private Context context = ApplicationProvider.getApplicationContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private LibraryEntryDao dao = db.libraryEntryDao();
    private LibraryNodeDao nodeDao = db.libraryNodeDao();
    private LibraryContentTypeDao contentTypeDao = db.libraryContentTypeDao();
    private TagDao tagDao = db.tagDao();
    private TrackDao trackDao = db.trackDao();

    private LibraryNode node = new LibraryNode(null, LibraryContentType.Type.TAG, "foo");
    private final Tag tag = new Tag("key", "value");
    private final Track track = new Track(Uri.parse("file:///sample.ogg"));

    @After
    public void after() {
        db.close();
    }

    @Before
    public void before() {
        contentTypeDao.insert(new LibraryContentType(LibraryContentType.Type.TAG));
        nodeDao.insert(node);
        tagDao.insert(tag);
        trackDao.insert(track);
    }

    @Test
    public void truncate() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, tag, track));
        dao.insert(entry);
        dao.truncate();
        assertNull(dao.getEntry(null, tag, track));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertInvalidParent() {
        dao.insert(new LibraryEntry(-1L, node.getId(), tag.getId(), track.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidNode() {
        LibraryEntry root = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(root.getId(), -1L, tag.getId(), track.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidTag() {
        LibraryEntry root = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(root.getId(), node.getId(), -1L, track.getId()));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertWithInvalidTrack() {
        LibraryEntry root = dao.insert(new LibraryEntry(null, node, tag, null));
        dao.insert(new LibraryEntry(root.getId(), node.getId(), tag.getId(), -1L));
    }

    @Test
    public void trackAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, tag, track));
        assertEquals(entry, dao.getEntry(null, tag, track));
        assertThat(dao.getChildrenOf((LibraryEntry) null)).containsExactly(entry);
        assertThat(dao.getChildrenOf((LibraryEntryText) null))
                .containsExactly(new LibraryEntryText(entry.getId(), null, tag.getValue()));
    }

    @Test
    public void tagAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, tag, null));
        assertEquals(entry, dao.getEntry(null, tag, null));
        assertThat(dao.getChildrenOf((LibraryEntry) null)).containsExactly(entry);
        assertThat(dao.getChildrenOf((LibraryEntryText) null))
                .containsExactly(new LibraryEntryText(entry.getId(), null, tag.getValue()));
    }

    @Test
    public void nullTagAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, null, null));
        assertEquals(entry, dao.getEntry(null, null, null));
        assertThat(dao.getChildrenOf((LibraryEntry) null)).containsExactly(entry);
        assertThat(dao.getChildrenOf((LibraryEntryText) null))
                .containsExactly(new LibraryEntryText(entry.getId(), null, null));
    }

    @Test
    public void trackWithNullTagAtRoot() {
        LibraryEntry entry = dao.insert(new LibraryEntry(null, node, null, track));
        assertEquals(entry, dao.getEntry(null, null, track));
        assertThat(dao.getChildrenOf((LibraryEntry) null)).containsExactly(entry);
        assertThat(dao.getChildrenOf((LibraryEntryText) null))
                .containsExactly(new LibraryEntryText(entry.getId(), null, null));
    }

    @Test
    public void tagBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, tag, null));

        assertEquals(entry, dao.getEntry(parent, tag, null));
        assertThat(dao.getChildrenOf(parent)).containsExactly(entry);
        assertThat(dao.getChildrenOf(new LibraryEntryText(parent.getId(), null, tag.getValue())))
                .containsExactly(
                        new LibraryEntryText(entry.getId(), entry.getParentId(), tag.getValue()));
    }

    @Test
    public void nullTagBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, null, null));

        assertEquals(entry, dao.getEntry(parent, null, null));
        assertThat(dao.getChildrenOf(parent)).containsExactly(entry);
        assertThat(dao.getChildrenOf(new LibraryEntryText(parent.getId(), null, tag.getValue())))
                .containsExactly(new LibraryEntryText(entry.getId(), entry.getParentId(), null));
    }

    @Test
    public void trackBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, tag, track));
        assertEquals(entry, dao.getEntry(parent, tag, track));
        assertThat(dao.getChildrenOf(parent)).containsExactly(entry);
        assertThat(dao.getChildrenOf(new LibraryEntryText(parent.getId(), null, tag.getValue())))
                .containsExactly(
                        new LibraryEntryText(entry.getId(), entry.getParentId(), tag.getValue()));
    }

    @Test
    public void trackWithNullTagBelowRoot() {
        LibraryEntry parent = dao.insert(new LibraryEntry(null, node, tag, null));
        LibraryEntry entry = dao.insert(new LibraryEntry(parent, node, null, track));

        assertEquals(entry, dao.getEntry(parent, null, track));
        assertThat(dao.getChildrenOf(parent)).containsExactly(entry);
        assertThat(dao.getChildrenOf(new LibraryEntryText(parent.getId(), null, tag.getValue())))
                .containsExactly(new LibraryEntryText(entry.getId(), entry.getParentId(), null));
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

        assertThat(dao.getChildrenOf(parent)).containsExactly(child1, child2);
        assertThat(dao.getChildrenOf(new LibraryEntryText(parent.getId(), null, tag.getValue())))
                .containsExactly(
                        new LibraryEntryText(child1.getId(), child1.getParentId(), tag.getValue()),
                        new LibraryEntryText(child2.getId(), child2.getParentId(), tag.getValue()));
    }

    @Test
    public void getMissingEntry() {
        dao.insert(new LibraryEntry(null, node, tag, track));
        LibraryEntry root2 = new LibraryEntry(null, node, null, null);
        assertNull(dao.getEntry(root2, tag, track));
    }

}
