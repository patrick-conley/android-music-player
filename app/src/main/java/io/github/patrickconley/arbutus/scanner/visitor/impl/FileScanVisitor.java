package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
public class FileScanVisitor implements MediaVisitor {
    private static final String TAG = FileScanVisitor.class.getName();

    private AppDatabase db;
    private TrackManager trackManager;
    private LibraryManager libraryManager;
    private StrategyFactory strategyFactory = new StrategyFactory();

    /**
     * Scan the provided directory tree, then clean up.
     */
    public static void execute(Context context, File file) {
        Log.i(TAG, "Scanning " + file);

        FileScanVisitor visitor = new FileScanVisitor(context);
        long fileCount = new MediaFolder(file).accept(visitor);
        visitor.release();

        Log.i(TAG, "Scanned " + fileCount + " files");
    }

    /**
     * Create an instance of the visitor.
     *
     * @param context
     *         The context running the visitor.
     */
    private FileScanVisitor(Context context) {
        db = AppDatabase.getInstance(context);
        trackManager = new TrackManager(db);
        libraryManager = new LibraryManager(db);
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
        Track track = new Track(file.getUri());

        Map<String, Tag> tags = readTags(file);
        if (tags == null) {
            return;
        }

        // Save track
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

    /*
     * Read tags
     *
     * Note that GenericTagStrategy will return null if the file isn't a media file (other strategies have already
     * verified the file is a media file)
     */
    @Nullable
    private Map<String, Tag> readTags(@NonNull MediaFile file) {
        try {
            return strategyFactory.getStrategy(file).readTags(file.getFile());
        } catch (ScannerException e) {
            Log.e(TAG, "Failed to read tags from " + file, e);
            return null;
        }
    }

}
