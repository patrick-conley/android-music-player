package io.github.patrickconley.arbutus.datastorage.library.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryNode;

import java.util.List;

@Dao
public abstract class LibraryNodeDao {

    @Insert
    abstract long insertForId(LibraryNode node);

    public LibraryNode insert(LibraryNode node) {
        node.setId(insertForId(node));
        return node;
    }

    @Query("select * from LibraryNode where parentId is null")
    abstract List<LibraryNode> getRootNodes();

    @Query("select * from LibraryNode where parentId = :parentId")
    abstract List<LibraryNode> getByParent(long parentId);

    public List<LibraryNode> getChildrenOf(LibraryNode parent) {
        if (parent == null) {
            return getRootNodes();
        }

        return getByParent(parent.getId());
    }

}
