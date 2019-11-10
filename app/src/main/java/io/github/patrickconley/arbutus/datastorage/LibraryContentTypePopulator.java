package io.github.patrickconley.arbutus.datastorage;

import android.util.Log;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;

/**
 * Populate the 'contenttype' table with the the only possible types 'tag' and 'track'
 */
class LibraryContentTypePopulator implements Runnable {

    private final AppDatabase db;

    LibraryContentTypePopulator(final AppDatabase db) {
        this.db = db;
    }

    @Override
    public void run() {
        db.libraryContentTypeDao().insert(new LibraryContentType(LibraryContentType.Type.Tag),
                                          new LibraryContentType(LibraryContentType.Type.Track));
        Log.w(getClass().getName(), "Inserted library content types");
    }
}
