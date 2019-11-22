package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.LibraryManager;
import io.github.patrickconley.arbutus.metadata.TrackManager;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.scanner.ScannerException;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.strategy.StrategyFactory;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitor;

/**
 * Visit part of a filesystem, scanning its files for audio metadata.
 *
 * @author pconley
 */
public final class FileScanVisitor implements MediaVisitor {
    private static final String TAG = FileScanVisitor.class.getName();

    private AppDatabase db;
    private TrackManager trackManager;
    private LibraryManager libraryManager;
    private StrategyFactory strategyFactory;

    /**
     * Scan the provided directory tree, then clean up.
     */
    public static void execute(Context context, File file) {
        Log.i(TAG, "Scanning " + file);

        FileScanVisitor visitor = new FileScanVisitor(AppDatabase.getInstance(context));
        long fileCount = new MediaFolder(file).accept(visitor);
        visitor.release();

        Log.i(TAG, "Scanned " + fileCount + " files");
    }

    @Deprecated //Used by unit tests
    FileScanVisitor() {
    }

    private FileScanVisitor(AppDatabase db) {
        this.db = db;
        this.trackManager = new TrackManager(db);
        this.libraryManager = new LibraryManager(db);
        this.strategyFactory = new StrategyFactory();
    }

    private void release() {
        strategyFactory.release();
    }

    @Override
    public void visit(MediaFolder dir) {
        // nothing to do
    }

    /**
     * Scan the file for audio metadata; write the file and metadata to the library. If the file
     * isn't audio, return without writing anything.
     */
    @Override
    public void visit(MediaFile file) {

        Map<String, Tag> tags;
        try {
            tags = strategyFactory.getStrategy(file).readTags(file.getFile());
        } catch (ScannerException e) {
            Log.e(TAG, "Failed to read tags from " + file, e);
            return;
        }

        saveTrack(new Track(file.getUri()), tags);
    }

    private void saveTrack(Track track, Map<String, Tag> tags) {
        try {
            db.beginTransaction();

            trackManager.addTrack(track, tags);
            libraryManager.addTrack(track, tags);

            db.setTransactionSuccessful();
        } catch (RuntimeException e) {
            // TODO: broadcast failures
            Log.e(TAG, "Failed to save " + track, e);
        } finally {
            db.endTransaction();
        }
    }

}
