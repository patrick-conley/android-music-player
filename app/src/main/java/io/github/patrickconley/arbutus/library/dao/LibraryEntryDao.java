package io.github.patrickconley.arbutus.library.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public abstract class LibraryEntryDao {

    @Insert(onConflict = IGNORE)
    public abstract long insert(LibraryEntry entry);

    @Query("select * from LibraryEntry where parentId = :parentId and tagId = :tagId and trackId " +
           "= :trackId")
    abstract LibraryEntry getEntry(Long parentId, Long tagId, Long trackId);

    public LibraryEntry getEntry(LibraryEntry parent, Tag tag, Track track) {
        Long parentId = parent == null ? null : parent.getId();
        Long trackId = track == null ? null : track.getId();
        Long tagId = tag == null ? null : tag.getId();

        return getEntry(parentId, tagId, trackId);
    }

}
