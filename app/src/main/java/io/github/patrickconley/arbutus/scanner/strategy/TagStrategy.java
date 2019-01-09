package io.github.patrickconley.arbutus.scanner.strategy;

import io.github.patrickconley.arbutus.metadata.model.Tag;

import java.io.File;
import java.util.Set;

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
    Set<Tag> readTags(File file) throws Exception;

    /**
     * Release any native resources
     */
    void release();
}
