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
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;

/**
 * Visit part of a filesystem, scanning its files for audio metadata.
 *
 * @author pconley
 */
public class FileScanVisitor implements MediaVisitorBase {
    private final String tag = getClass().getName();

    private AppDatabase db;
    private TrackManager trackManager;
    private LibraryManager libraryManager;
    private StrategyFactory strategyFactory = new StrategyFactory();

    /**
     * Create an instance of the visitor.
     *
     * @param context
     *         The context running the visitor.
     */
    public FileScanVisitor(Context context) {
        db = AppDatabase.getInstance(context);
        trackManager = new TrackManager(db);
        libraryManager = new LibraryManager(db);
    }

    public long execute(File file, Method method) {
        long result = method.execute(file, this);
        strategyFactory.release();
        return result;
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
            Log.e(tag, "Failed to save " + track, e);
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
            Log.e(tag, "Failed to read tags from " + file, e);
            return null;
        }
    }

    public interface Method {
        long execute(File file, FileScanVisitor visitor);
    }
}
