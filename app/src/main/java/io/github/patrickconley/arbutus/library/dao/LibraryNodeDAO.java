package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import io.github.patrickconley.arbutus.library.model.LibraryNode;

@Dao
public interface LibraryNodeDAO {

    @Insert
    long insert(LibraryNode node);
}
