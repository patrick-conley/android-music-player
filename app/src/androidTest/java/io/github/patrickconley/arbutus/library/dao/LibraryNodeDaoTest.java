package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryNode;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LibraryNodeDaoTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    private LibraryNodeDao dao = db.libraryNodeDao();
    private LibraryContentTypeDao contentTypeDao = db.libraryContentTypeDao();

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
        LibraryNode parent = new LibraryNode(null, LibraryContentType.Type.Tag, "parent");
        parent.setId(-1);
        dao.insert(new LibraryNode(parent, LibraryContentType.Type.Tag, "foo"));
    }

    @Test
    public void insertShouldAllowEmptyParentId() {
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag, "foo"));

    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithInvalidContentType() {
        dao.insert(new LibraryNode(null, -1, "foo"));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void insertShouldFailWithMissingName() {
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag, null));
    }

    /*
     * Given an empty library, when I get root nodes, I should get nothing
     */
    @Test
    public void getRootNodesOnEmptyLibrary() {
        assertEquals(Collections.emptyList(), dao.getChildrenOf(null));
    }

    /*
     * Given a library with one root node, when I get root nodes, then I should get that node
     */
    @Test
    public void getRootNodesWithOneNode() {
        LibraryNode node = new LibraryNode(null, LibraryContentType.Type.Tag, "foo");
        node.setId(dao.insert(node));

        assertEquals(Collections.singletonList(node), dao.getChildrenOf(null));
    }

    /*
     * Given a library with two root nodes, when I get root nodes, then I should get both nodes
     */
    @Test
    public void getRootNodesWithMultipleNodes() {
        List<LibraryNode> nodes =
                Arrays.asList(new LibraryNode(null, LibraryContentType.Type.Tag, "foo"),
                              new LibraryNode(null, LibraryContentType.Type.Tag, "bar"));
        for (LibraryNode node : nodes) {
            node.setId(dao.insert(node));
        }

        assertEquals(nodes, dao.getChildrenOf(null));
    }

    /*
     * Given a library with a root node and a child node, when I get root nodes, then I should only get the root node
     */
    @Test
    public void getRootNodeWithChildNodes() {
        LibraryNode parent = new LibraryNode(null, LibraryContentType.Type.Tag, "foo");
        parent.setId(dao.insert(parent));
        dao.insert(new LibraryNode(parent, LibraryContentType.Type.Tag, "bar"));

        assertEquals(Collections.singletonList(parent), dao.getChildrenOf(null));
    }

    /*
     * Given a library with a node, when I get child nodes, then I should get nothing
     */
    @Test
    public void getChildNodesFromLibraryWithRoots() {
        LibraryNode node = new LibraryNode(null, LibraryContentType.Type.Tag, "foo");
        node.setId(dao.insert(node));
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag, "bar"));

        assertEquals(Collections.emptyList(), dao.getChildrenOf(node));
    }

    /*
     * Given a library with a node with parent, when I get nodes for that parent, then I should get the child node
     */
    @Test
    public void getChildNode() {
        LibraryNode root = new LibraryNode(null, LibraryContentType.Type.Tag, "foo");
        root.setId(dao.insert(root));
        dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag, "bar"));

        LibraryNode child = new LibraryNode(root, LibraryContentType.Type.Tag, "child");
        child.setId(dao.insert(child));

        assertEquals(Collections.singletonList(child), dao.getChildrenOf(root));
    }

    /*
     * Given a library with a node with parent, when I get nodes by a different parent, then I should get nothing
     */
    @Test
    public void getChildNodeByWrongParent() {
        LibraryNode root = new LibraryNode(null, LibraryContentType.Type.Tag, "foo");
        root.setId(dao.insert(root));
        dao.insert(new LibraryNode(root, LibraryContentType.Type.Tag, "child"));

        LibraryNode node = new LibraryNode(null, LibraryContentType.Type.Tag, "bar");
        node.setId(dao.insert(node));

        assertEquals(Collections.emptyList(), dao.getChildrenOf(node));
    }

}
