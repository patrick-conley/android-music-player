package io.github.patrickconley.arbutus.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;

/**
 * Build the library graph.
 */
public class LibraryManager {
    private final String log = getClass().getName();

    private final AppDatabase db;
    private final TrackDao trackDao;
    private final TagDao tagDao;
    private final TrackTagDao trackTagDao;
    private final LibraryNodeDao libraryNodeDao;
    private final LibraryEntryDao libraryEntryDao;

    public LibraryManager(Context context) {
        this(AppDatabase.getInstance(context));
    }

    LibraryManager(AppDatabase db) {
        this.db = db;
        trackDao = db.trackDao();
        tagDao = db.tagDao();
        trackTagDao = db.trackTagDao();
        libraryNodeDao = db.libraryNodeDao();
        libraryEntryDao = db.libraryEntryDao();
    }

    public void addTrack(@NonNull Track track, @NonNull Map<String, Tag> tags) {

        // FIXME consider moving the code that saves Tracks & Tags into a separate class called
        //  by FileScanVisitor (TrackManager vs LibraryManager)
        try {
            db.beginTransaction();
            trackDao.insert(track);

            for (final Tag tag : tags.values()) {
                Tag savedTag = tagDao.getTag(tag);
                if (savedTag == null) {
                    savedTag = tagDao.insert(tag);
                }

                trackTagDao.insert(new TrackTag(track, savedTag));
            }

            // FIXME this isn't safe if there is inexplicably no root node
            // FIXME this won't work as expected if there is inexplicably more than one root node
            addEntryAtNode(null, libraryNodeDao.getChildrenOf(null).get(0), track, tags);

            db.setTransactionSuccessful();
        } catch (RuntimeException e) {
            // TODO: broadcast failures
            Log.e(log, "Failed to save " + track, e);
        } finally {
            db.endTransaction();
        }
    }

    private void addEntryAtNode(
            LibraryEntry parentEntry, @NonNull LibraryNode currentNode, @NonNull Track track,
            @NonNull Map<String, Tag> tags
    ) {
        // base case: current node is a track node
        // get or insert a LibraryEntries for this track by node/parent
        if (currentNode.getContentTypeId() == LibraryContentType.Type.TRACK.getId()) {
            insertEntry(parentEntry, currentNode, tags, track);
            return;
        }

        // recursive case: current node is a tag node
        // find or insert LibraryEntries for these tags
        LibraryEntry entry = insertEntry(parentEntry, currentNode, tags, null);

        // recurse for each child node/library entry pair
        for (LibraryNode childNode : libraryNodeDao.getChildrenOf(currentNode)) {
            addEntryAtNode(entry, childNode, track, tags);
        }
    }

    private LibraryEntry insertEntry(
            LibraryEntry parent, @NonNull LibraryNode node, @NonNull Map<String, Tag> tags,
            Track track
    ) {

        LibraryEntry savedEntry = libraryEntryDao.getEntry(parent, tags.get(node.getName()), track);
        if (savedEntry != null) {
            return savedEntry;
        }

        return libraryEntryDao
                .insert(new LibraryEntry(parent, node, tags.get(node.getName()), track));
    }

}
