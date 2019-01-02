package io.github.patrickconley.arbutus.scanner.model;

import java.io.File;

import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;

/**
 * Representation of a file to be used as part of a hierarchical visitor pattern
 * with {@link MediaVisitorBase}.
 *
 * @author pconley
 */
public abstract class MediaFileBase {

    private File file;

    public MediaFileBase(File file) {
        this.file = file;
    }

    /**
     * Validate, then visit the file.
     *
     * @param visitor
     *
     * @return Number of files (not folders) visited
     */
    public abstract long accept(MediaVisitorBase visitor);

    /**
     * @return The File underlying this object.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return The string representation of the underlying file.
     */
    public String toString() {
        return file.toString();
    }

}
