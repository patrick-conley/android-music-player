package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LibraryNodeDAOTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private LibraryNodeDAO dao = db.libraryNodeDao();
    private LibraryContentTypeDAO contentTypeDao = db.libraryContentTypeDao();

    @After
    public void after() {
        db.close();
    }

    @Before
    public void before() {
        contentTypeDao.insert(new LibraryContentType(LibraryContentType.Type.Tag));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidParentId() {
        dao.insert(new LibraryNode(-1L, LibraryContentType.Type.Tag.getId(), "foo"));
    }

    @Test
    public void insertShouldAllowEmptyParentId() {
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));

    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidContentType() {
        dao.insert(new LibraryNode(null, -1, "foo"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithMissingName() {
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), null));
    }

    /*
     * Given an empty library, when I get root nodes, I should get nothing
     */
    @Test
    public void getRootNodesOnEmptyLibrary() {
        assertEquals(Collections.EMPTY_LIST, dao.getByParent(null));
    }

    /*
     * Given a library with one root node, when I get root nodes, then I should get that node
     */
    @Test
    public void getRootNodesWithOneNode() {
        long id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));

        List<LibraryNode> actual = dao.getByParent(null);

        assertEquals(1, actual.size());
        assertEquals(id, actual.get(0).getId());
    }

    /*
     * Given a library with two root nodes, when I get root nodes, then I should get both nodes
     */
    @Test
    public void getRootNodesWithMultipleNodes() {
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "bar"));

        List<LibraryNode> actual = dao.getByParent(null);
        assertEquals(2, actual.size());
    }

    /*
     * Given a library with a root node and a child node, when I get root nodes, then I should only get the root node
     */
    @Test
    public void getRootNodeWithChildNodes() {
        long id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));
        dao.insert(new LibraryNode(id, LibraryContentType.Type.Tag.getId(), "bar"));

        List<LibraryNode> actual = dao.getByParent(null);
        assertEquals(1, actual.size());
    }

    /*
     * Given an empty library, when I get child nodes, then I should get nothing
     */
    @Test
    public void getChildNodesOnEmptyLibrary() {
        assertEquals(Collections.EMPTY_LIST, dao.getByParent(-1L));
    }

    /*
     * Given a library with a node, when I get child nodes, then I should get nothing
     */
    @Test
    public void getChildNodesFromLibraryWithRoots() {
        long id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "bar"));

        assertEquals(Collections.EMPTY_LIST, dao.getByParent(id));
    }

    /*
     * Given a library with a node with parent, when I get nodes for that parent, then I should get the child node
     */
    @Test
    public void getChildNode() {
        long id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "bar"));

        long child = dao.insert(new LibraryNode(id, LibraryContentType.Type.Tag.getId(), "child"));

        assertEquals(1, dao.getByParent(id).size());
        assertEquals(child, dao.getByParent(id).get(0).getId());
    }

    /*
     * Given a library with a node with parent, when I get nodes by a different parent, then I should get nothing
     */
    @Test
    public void getChildNodeByWrongParent() {
        long id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "foo"));
        dao.insert(new LibraryNode(id, LibraryContentType.Type.Tag.getId(), "child"));

        id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "bar"));

        assertEquals(Collections.EMPTY_LIST, dao.getByParent(id));
    }

}
