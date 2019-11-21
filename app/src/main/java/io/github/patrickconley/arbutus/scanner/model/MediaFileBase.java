package io.github.patrickconley.arbutus.scanner.model;

import android.net.Uri;

import java.io.File;

import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitor;

/**
 * Representation of a file to be used as part of a hierarchical visitor pattern
 * with {@link MediaVisitor}.
 *
 * @author pconley
 */
public abstract class MediaFileBase {

    private File file;

    protected MediaFileBase(File file) {
        this.file = file;
    }

    /**
     * Validate, then visit the file.
     *
     * @return Number of files (not folders) visited
     */
    public abstract long accept(MediaVisitor visitor);

    /**
     * @return The File underlying this object.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return The Uri of the file underlying this object.
     */
    public Uri getUri() {
        return Uri.fromFile(file);
    }

    /**
     * @return The string representation of the underlying file.
     */
    public String toString() {
        return file.toString();
    }

}
