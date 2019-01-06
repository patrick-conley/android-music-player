package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import io.github.patrickconley.arbutus.library.model.LibraryItem;

@Dao
public interface LibraryItemDAO {

    @Insert
    long insert(LibraryItem item);
}
