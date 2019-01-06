package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;

@Dao
public interface LibraryContentTypeDAO {

    @Insert
    void insert(LibraryContentType... libraryContentType);
}
