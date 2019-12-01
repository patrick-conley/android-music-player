package io.github.patrickconley.arbutus.datastorage.library.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryContentType;

@Dao
public interface LibraryContentTypeDao {

    @Insert
    void insert(LibraryContentType... libraryContentType);
}
