package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Map;

import io.github.patrickconley.arbutus.library.Library;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
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

    private Library library;

    /**
     * Create an instance of the visitor.
     *
     * @param context
     *         The context running the visitor.
     */
    public FileScanVisitor(Context context) {
        library = new Library(context);
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
        library.addTrack(new Track(Uri.fromFile(file.getFile())), tags);
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

}
