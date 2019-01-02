package io.github.patrickconley.arbutus.scanner.strategy;

import java.io.File;
import java.util.List;

import io.github.patrickconley.arbutus.domain.model.Tag;

/**
 * Read the metadata from a file.
 *
 * @author pconley
 */
public interface TagStrategy {

    /**
     * Read a file's metadata and return it in a common representation.
     *
     * @param file
     *         File to read
     *
     * @return Key/value pairs.
     */
    List<Tag> getTags(File file) throws Exception;

    /**
     * Release any native resources
     */
    void release();
}
