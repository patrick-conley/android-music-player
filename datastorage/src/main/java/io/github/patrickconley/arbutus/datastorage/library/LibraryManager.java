package io.github.patrickconley.arbutus.datastorage.library;

import androidx.annotation.NonNull;

import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryNode;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

/**
 * Build the library graph.
 */
public class LibraryManager {

    private final LibraryNodeDao libraryNodeDao;
    private final LibraryEntryDao libraryEntryDao;

    public LibraryManager(AppDatabase db) {
        libraryNodeDao = db.libraryNodeDao();
        libraryEntryDao = db.libraryEntryDao();
    }

    public void addTrack(@NonNull Track track, @NonNull Map<String, Tag> tags) {

            // FIXME this isn't safe if there is inexplicably no root node
            // FIXME this won't work as expected if there is inexplicably more than one root node
            addEntryAtNode(null, libraryNodeDao.getChildrenOf(null).get(0), track, tags);
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
