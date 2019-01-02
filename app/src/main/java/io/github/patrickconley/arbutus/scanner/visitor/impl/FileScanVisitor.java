package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import io.github.patrickconley.arbutus.domain.AppDatabase;
import io.github.patrickconley.arbutus.domain.dao.TagDAO;
import io.github.patrickconley.arbutus.domain.dao.TrackDAO;
import io.github.patrickconley.arbutus.domain.dao.TrackTagDAO;
import io.github.patrickconley.arbutus.domain.model.Tag;
import io.github.patrickconley.arbutus.domain.model.Track;
import io.github.patrickconley.arbutus.domain.model.TrackTag;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;
import io.github.patrickconley.arbutus.scanner.strategy.StrategyFactory;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

/**
 * Visit part of a filesystem, scanning its files for audio metadata.
 *
 * @author pconley
 */
public class FileScanVisitor implements MediaVisitorBase {
    private final String tag = getClass().getName();

    private AppDatabase db;
    private TrackDAO trackDao;
    private TagDAO tagDao;
    private TrackTagDAO trackTagDAO;

    /**
     * Create an instance of the visitor.
     *
     * @param context
     *         The context running the visitor.
     */
    public FileScanVisitor(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.trackDao = db.trackDao();
        this.tagDao = db.tagDao();
        this.trackTagDAO = db.trackTagDAO();
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
     * Scan the file for audio metadata; write the file and metadata to the
     * library. If the file isn't audio, return without writing anything.
     */
    @Override
    public void visit(MediaFile file) {
        List<Tag> tags;

        // Read tags - note GenericTagStrategy will return null if the file isn't a media file
        try {
            TagStrategy strategy = StrategyFactory.getStrategy(file);
            tags = strategy.getTags(file.getFile());
            if (tags == null) {
                return;
            }
        } catch (Exception e) {
            Log.e(tag, "Failed to read tags from " + file, e);
            return;
        }

        // Save track
        try {
            db.beginTransaction();
            long trackId = trackDao.insert(new Track(Uri.fromFile(file.getFile())));

            for (Tag tag : tags) {
                Tag savedTag = tagDao.getTag(tag);
                long tagId = savedTag != null ? savedTag.getTagId() : tagDao.insert(tag);

                trackTagDAO.insert(new TrackTag(trackId, tagId));
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            // FIXME: broadcast failures
            Log.e(tag, "Failed to save " + file, e);
        } finally {
            db.endTransaction();
        }

    }

}
