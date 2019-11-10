package io.github.patrickconley.arbutus.datastorage;

import android.util.Log;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDAO;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryNode;

class LibraryNodePopulator implements Runnable {

    private final AppDatabase db;

    LibraryNodePopulator(final AppDatabase db) {
        this.db = db;
    }

    @Override
    public void run() {
        LibraryNodeDAO dao = db.libraryNodeDao();
        long id = dao.insert(new LibraryNode(null, LibraryContentType.Type.Tag.getId(), "artist"));
        id = dao.insert(new LibraryNode(id, LibraryContentType.Type.Tag.getId(), "album"));
        dao.insert(new LibraryNode(id, LibraryContentType.Type.Track.getId(), "title"));
        Log.w(getClass().getName(), "Inserted default library nodes");
    }
}
