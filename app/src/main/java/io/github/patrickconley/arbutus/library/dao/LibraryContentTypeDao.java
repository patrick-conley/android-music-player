package io.github.patrickconley.arbutus.library.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;

@Dao
public interface LibraryContentTypeDao {

    @Insert
    void insert(LibraryContentType... libraryContentType);
}
