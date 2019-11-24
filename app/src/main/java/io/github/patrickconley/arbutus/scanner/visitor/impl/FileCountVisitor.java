package io.github.patrickconley.arbutus.scanner.visitor.impl;

import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitor;

/**
 * Visit part of a filesystem, counting media files.
 *
 * @author pconley
 */
public class FileCountVisitor implements MediaVisitor {

    private int count = 0;

    @Override
    public boolean visit(MediaFolder dir) {
        // TODO does this work? Is it much faster?
        //		count += dir.getFile().listFiles(new FileFilter() {
        //			@Override
        //			public boolean accept(File pathname) {
        //				return pathname.isFile();
        //			}
        //		}).length;
        return true;
    }

    @Override
    public boolean visit(MediaFile file) {
        count++;
        return true;
    }

    /**
     * @return The number of files (but not folders) in the system.
     */
    public int getCount() {
        return count;
    }

}
