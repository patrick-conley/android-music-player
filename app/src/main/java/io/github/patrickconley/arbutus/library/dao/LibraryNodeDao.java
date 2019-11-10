package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import io.github.patrickconley.arbutus.library.model.LibraryNode;

import java.util.List;

@Dao
public abstract class LibraryNodeDao {

    @Insert
    public abstract long insert(LibraryNode node);

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
