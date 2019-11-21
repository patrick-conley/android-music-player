package io.github.patrickconley.arbutus.scanner.model.impl;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

import io.github.patrickconley.arbutus.scanner.model.MediaFileBase;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;

/**
 * A visitable directory.
 *
 * @author pconley
 */
public class MediaFolder extends MediaFileBase {
    private final String tag = getClass().getName();

    public MediaFolder(File file) {
        super(file);
    }

    /**
     * Validate the directory (it must be a readable directory that
     * does not contain a .nomedia file), then visit it and call its children's
     * appropriate accept methods.
     */
    @Override
    public long accept(MediaVisitorBase visitor) {

        // Check the directory is readable
        if (!getFile().exists() || !getFile().isDirectory() || !getFile().canExecute()) {
            Log.w(tag, "Directory is invalid");
            return 0L;
        }

        Log.d(tag, "Scanning directory " + getFile().toString());

        // Check the directory allows media scanning
        if (getFile().listFiles(new NoMediaFilter()).length > 0) {
            Log.d(tag, "Skipping directory (.nomedia)");
            return 0L;
        }

        visitor.visit(this);

        File[] contents = getFile().listFiles();
        long count = 0L;
        for (File file : contents) {
            if (file.isDirectory()) {
                count += new MediaFolder(file).accept(visitor);
            } else {
                count += new MediaFile(file).accept(visitor);
            }
        }

        return count;
    }

    private static class NoMediaFilter implements FilenameFilter {
        @Override
        public boolean accept(File file, String name) {
            return name.equalsIgnoreCase(".nomedia");
        }
    }
}
