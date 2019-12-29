package io.github.patrickconley.arbutus.scanner.visitor;

import io.github.patrickconley.arbutus.scanner.model.MediaFileBase;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;

/**
 * Traverse a hierarchical filesystem. Methods in subclasses of
 * {@link MediaVisitor} should only be called from {@link MediaFolder} and
 * {@link MediaFile}.
 *
 * @author pconley
 */
public interface MediaVisitor {

    /**
     * Do some work on a directory. The directory is guaranteed to exist and to
     * not contain a .nomedia file.
     * <p/>
     * This method should *not* call
     * {@link MediaFileBase#accept(MediaVisitor)} on the directory's
     * children - {@link MediaFolder#accept(MediaVisitor)} does that itself
     * after this method returns.
     *
     * @param dir to visit
     * @return whether the visit completed without errors
     */
    boolean visit(MediaFolder dir);

    /**
     * Do some work on a file. The file is guaranteed to be readable.
     *
     * @param file to visit
     * @return whether the visit completed without errors
     */
    boolean visit(MediaFile file);

}
