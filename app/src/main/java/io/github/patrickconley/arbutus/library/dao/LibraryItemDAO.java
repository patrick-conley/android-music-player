package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import io.github.patrickconley.arbutus.library.model.LibraryItem;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface LibraryItemDAO {

    @Insert(onConflict = IGNORE)
    long insert(LibraryItem item);

}
