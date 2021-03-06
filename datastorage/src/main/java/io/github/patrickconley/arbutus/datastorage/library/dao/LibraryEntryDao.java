package io.github.patrickconley.arbutus.datastorage.library.dao;

import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public abstract class LibraryEntryDao {

    @Insert(onConflict = IGNORE)
    abstract long insertForId(LibraryEntry entry);

    public LibraryEntry insert(LibraryEntry entry) {
        if (entry.getParentId() != null && entry.getParentId() <= 0L) {
            throw new SQLiteConstraintException("invalid parent entry " + entry.getParentId());
        } else if (entry.getTagId() != null && entry.getTagId() <= 0L) {
            throw new SQLiteConstraintException("invalid tag " + entry.getTagId());
        } else if (entry.getTrackId() != null && entry.getTrackId() <= 0L) {
            throw new SQLiteConstraintException("invalid track " + entry.getTrackId());
        }
        entry.setId(insertForId(entry));
        return entry;
    }

    @Query("delete from LibraryEntry")
    public abstract void truncate();

    @Query("select * from LibraryEntry " + //
           "where parentId is null " + //
           "and tagId is null " + //
           "and trackId is null")
    abstract LibraryEntry getNullTagAtRoot();

    @Query("select * from LibraryEntry " + //
           "where parentId is null " +  //
           "and tagId = :tagId " + //
           "and trackId is null")
    abstract LibraryEntry getTagAtRoot(long tagId);

    @Query("select * from LibraryEntry " + //
           "where parentId is null " +  //
           "and tagId = :tagId " + //
           "and trackId = :trackId")
    abstract LibraryEntry getTrackAtRoot(long tagId, long trackId);

    @Query("select * from LibraryEntry " + //
           "where parentId is null " +  //
           "and tagId is null " + //
           "and trackId = :trackId")
    abstract LibraryEntry getTrackWithNullTagAtRoot(long trackId);

    @Query("select * from LibraryEntry " + //
           "where parentId = :parentId " + //
           "and tagId = :tagId " + //
           "and trackId is null")
    abstract LibraryEntry getTagBelowRoot(long parentId, long tagId);

    @Query("select * from LibraryEntry " + //
           "where parentId = :parentId " + //
           "and tagId is null " + //
           "and trackId is null")
    abstract LibraryEntry getNullTagBelowRoot(long parentId);

    @Query("select * from LibraryEntry " +  //
           "where parentId = :parentId " +  //
           "and tagId = :tagId " + //
           "and trackId = :trackId")
    abstract LibraryEntry getTrackBelowRoot(long parentId, long tagId, long trackId);

    @Query("select * from LibraryEntry " + //
           "where parentId = :parentId " + //
           "and tagId is null " + //
           "and trackId = :trackId")
    abstract LibraryEntry getTrackWithNullTagBelowRoot(long parentId, long trackId);

    public LibraryEntry getEntry(LibraryEntry parent, Tag tag, Track track) {
        Long parentId = parent == null ? null : parent.getId();
        Long trackId = track == null ? null : track.getId();
        Long tagId = tag == null ? null : tag.getId();

        if (parent == null && tag == null && track == null) {
            return getNullTagAtRoot();
        } else if (parent == null && tag == null) {
            return getTrackWithNullTagAtRoot(trackId);
        } else if (parent == null && track == null) {
            return getTagAtRoot(tagId);
        } else if (parent == null) {
            return getTrackAtRoot(tagId, trackId);
        } else if (tag != null && track == null) {
            return getTagBelowRoot(parentId, tagId);
        } else if (tagId != null) {
            return getTrackBelowRoot(parentId, tagId, trackId);
        } else if (track == null) {
            return getNullTagBelowRoot(parentId);
        } else {
            return getTrackWithNullTagBelowRoot(parentId, trackId);
        }
    }

    @Query("select * from LibraryEntry where parentId is null")
    abstract List<LibraryEntry> getRootEntries();

    @Query("select * from LibraryEntry where parentId = :parentId")
    abstract List<LibraryEntry> getByParent(long parentId);

    public List<LibraryEntry> getChildrenOf(LibraryEntry parent) {
        if (parent == null) {
            return getRootEntries();
        }

        return getByParent(parent.getId());
    }

}
