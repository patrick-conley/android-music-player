package io.github.patrickconley.arbutus.library;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

/**
 * Build the library graph.
 */
public class Library {

    private LibraryNodeDao libraryNodeDao;
    private LibraryEntryDao libraryEntryDao;

    Library() {
        // used by unit tests
    }

    public Library(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.libraryNodeDao = db.libraryNodeDao();
        this.libraryEntryDao = db.libraryEntryDao();
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
        if (currentNode.getContentTypeId() == LibraryContentType.Type.Track.getId()) {
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

        LibraryEntry entry = new LibraryEntry(parent, node, tags.get(node.getName()), track);
        entry.setId(libraryEntryDao.insert(entry));
        return entry;
    }

}
