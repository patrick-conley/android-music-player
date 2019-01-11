package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.dao.LibraryItemDAO;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDAO;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryItem;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.dao.TagDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackDAO;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDAO;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.strategy.StrategyFactory;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;

import java.util.Collection;
import java.util.Set;

/**
 * Visit part of a filesystem, scanning its files for audio metadata.
 *
 * @author pconley
 */
public class FileScanVisitor implements MediaVisitorBase {
    private final String tag = getClass().getName();

    private AppDatabase db;
    private LibraryNodeDAO libraryNodeDAO;
    private LibraryItemDAO libraryItemDAO;
    private TagDAO tagDao;
    private TrackDAO trackDao;
    private TrackTagDAO trackTagDAO;

    /**
     * Create an instance of the visitor.
     *
     * @param context
     *         The context running the visitor.
     */
    public FileScanVisitor(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.libraryNodeDAO = db.libraryNodeDao();
        this.libraryItemDAO = db.libraryItemDao();
        this.tagDao = db.tagDao();
        this.trackDao = db.trackDao();
        this.trackTagDAO = db.trackTagDAO();
    }

    /**
     * Release native resources. Call this when finished.
     */
    public void close() {
        StrategyFactory.release();
    }

    @Override
    public void visit(MediaFolder dir) {
    }

    /**
     * Scan the file for audio metadata; write the file and metadata to the library. If the file isn't audio, return
     * without writing anything.
     */
    @Override
    public void visit(MediaFile file) {
        Set<Tag> tags = readTags(file);
        if (tags == null) {
            return;
        }

        // Save track
        try {
            db.beginTransaction();
            Track track = saveTrack(file, tags);
            saveLibraryItems(track, tags);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // FIXME: broadcast failures
            Log.e(tag, "Failed to save " + file, e);
        } finally {
            db.endTransaction();
        }
    }

    /*
     * Read tags
     *
     * Note that GenericTagStrategy will return null if the file isn't a media file (other strategies have already
     * verified the file is a media file)
     */
    @Nullable
    private Set<Tag> readTags(MediaFile file) {
        try {
            return StrategyFactory.getStrategy(file).readTags(file.getFile());
        } catch (Exception e) {
            Log.e(tag, "Failed to read tags from " + file, e);
            return null;
        }
    }

    /*
     * Store the track and its tags. Tags are updated with their IDs
     */
    private Track saveTrack(MediaFile file, Collection<Tag> tags) {
        Track track = new Track(Uri.fromFile(file.getFile()));
        track.setId(trackDao.insert(track));

        for (Tag tag : tags) {
            Tag savedTag = tagDao.getTag(tag);
            tag.setId(savedTag != null ? savedTag.getId() : tagDao.insert(tag));

            trackTagDAO.insert(new TrackTag(track.getId(), tag.getId()));
        }

        return track;
    }

    private void saveLibraryItems(Track track, Set<Tag> tags) {

        // FIXME this isn't safe if there is somehow no root node
        saveLibraryItems(null, libraryNodeDAO.getByParent(null).get(0), track, tags);
    }

    private void saveLibraryItems(
            LibraryItem parentItem, LibraryNode currentNode, Track track, Set<Tag> tags
    ) {
        // base case: current node is a track node
        // get or insert a LibraryItem for this track by node/parent
        if (currentNode.getContentTypeId() == LibraryContentType.Type.Track.getId()) {
            insertLibraryItem(parentItem, currentNode, track, tags);
            return;
        }

        // recursive case: current node is a tag node
        // find or insert LibraryItems for these tags
        LibraryItem item = insertLibraryItem(parentItem, currentNode, null, tags);

        // recurse for each child node/library item pair
        for (LibraryNode childNode : libraryNodeDAO.getByParent(currentNode.getId())) {
            saveLibraryItems(item, childNode, track, tags);
        }
    }

    private LibraryItem insertLibraryItem(LibraryItem parentItem, LibraryNode currentNode, Track track, Set<Tag> tags) {
        Long parentId = parentItem == null ? null : parentItem.getId();
        Long trackId = track == null ? null : track.getId();

        for (Tag tag : tags) {
            if (tag.getKey().equalsIgnoreCase(currentNode.getName())) {
                LibraryItem item = new LibraryItem(parentId, currentNode.getId(), tag.getId(), trackId);
                libraryItemDAO.insert(item);
                return item;
            }
        }

        LibraryItem item = new LibraryItem(parentId, currentNode.getId(), null, trackId);
        libraryItemDAO.insert(item);
        return item;
    }

}
