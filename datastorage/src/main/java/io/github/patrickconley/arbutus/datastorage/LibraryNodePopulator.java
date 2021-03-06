package io.github.patrickconley.arbutus.datastorage;

import android.util.Log;

import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryNode;

/**
 * Populate the 'node' table with a default structure of Artist -> Album -> Title
 */
class LibraryNodePopulator implements Runnable {

    private final AppDatabase db;

    LibraryNodePopulator(final AppDatabase db) {
        this.db = db;
    }

    @Override
    public void run() {
        LibraryNodeDao dao = db.libraryNodeDao();
        LibraryNode node = dao.insert(new LibraryNode(null, LibraryContentType.Type.TAG, "artist"));
        node = dao.insert(new LibraryNode(node, LibraryContentType.Type.TAG, "album"));
        dao.insert(new LibraryNode(node, LibraryContentType.Type.TRACK, "title"));
        Log.w(getClass().getName(), "Inserted default library nodes");
    }
}
