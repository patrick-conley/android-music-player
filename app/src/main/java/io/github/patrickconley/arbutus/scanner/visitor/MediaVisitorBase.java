package io.github.patrickconley.arbutus.scanner.visitor;

import io.github.patrickconley.arbutus.scanner.model.MediaFileBase;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;

/**
 * Traverse a hierarchical filesystem. Methods in subclasses of
 * {@link MediaVisitorBase} should only be called from {@link MediaFolder} and
 * {@link MediaFile}.
 *
 * @author pconley
 */
public interface MediaVisitorBase {

    /**
     * Do some work on a directory. The directory is guaranteed to exist and to
     * not contain a .nomedia file.
     * <p/>
     * This method should *not* call
     * {@link MediaFileBase#accept(MediaVisitorBase)} on the directory's
     * children - {@link MediaFolder#accept(MediaVisitorBase)} does that itself
     * after this method returns.
     *
     * @param dir to visit
     */
    void visit(MediaFolder dir);

    /**
     * Do some work on a file. The file is guaranteed to be readable.
     *
     * @param file to visit
     */
    void visit(MediaFile file);

}
