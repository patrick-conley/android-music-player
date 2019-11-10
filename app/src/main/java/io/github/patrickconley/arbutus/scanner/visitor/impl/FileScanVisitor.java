package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.Library;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;
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
    private Library library;
    private TagDao tagDao;
    private TrackDao trackDao;
    private TrackTagDao trackTagDAO;

    /**
     * Create an instance of the visitor.
     *
     * @param context
     *         The context running the visitor.
     */
    public FileScanVisitor(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.library = new Library(context);
        this.tagDao = db.tagDao();
        this.trackDao = db.trackDao();
        this.trackTagDAO = db.trackTagDao();
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
     * Scan the file for audio metadata; write the file and metadata to the library. If the file
     * isn't audio, return without writing anything.
     */
    @Override
    public void visit(MediaFile file) {
        Map<String, Tag> tags = readTags(file);
        if (tags == null) {
            return;
        }

        // Save track
        try {
            db.beginTransaction();
            Track track = saveTrack(file, tags);
            library.addTrack(track, tags);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // TODO: broadcast failures
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
    private Map<String, Tag> readTags(@NonNull MediaFile file) {
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
    private Track saveTrack(@NonNull MediaFile file, @NonNull Map<String, Tag> tags) {
        Track track = new Track(Uri.fromFile(file.getFile()));
        track.setId(trackDao.insert(track));

        for (Tag tag : tags.values()) {
            Tag savedTag = tagDao.getTag(tag);
            tag.setId(savedTag != null ? savedTag.getId() : tagDao.insert(tag));

            trackTagDAO.insert(new TrackTag(track, tag));
        }

        return track;
    }

}
