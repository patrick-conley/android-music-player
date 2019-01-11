package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import io.github.patrickconley.arbutus.library.model.LibraryNode;

import java.util.List;

@Dao
public abstract class LibraryNodeDAO {

    @Insert
    public abstract long insert(LibraryNode node);

    @Query("select * from LibraryNode where parentId is null")
    abstract List<LibraryNode> _getRootNodes();

    @Query("select * from LibraryNode where parentId = :parentId")
    abstract List<LibraryNode> _getByParent(long parentId);

    public List<LibraryNode> getByParent(Long parentId) {
        if (parentId == null) {
            return _getRootNodes();
        }

        return _getByParent(parentId);
    }

}
