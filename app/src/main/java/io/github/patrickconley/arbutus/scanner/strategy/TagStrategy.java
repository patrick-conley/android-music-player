package io.github.patrickconley.arbutus.scanner.strategy;

import java.io.File;
import java.util.Map;

import io.github.patrickconley.arbutus.metadata.model.Tag;

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
    Map<String, Tag> readTags(File file) throws Exception;

    /**
     * Release any native resources
     */
    void release();
}
