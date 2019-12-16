package io.github.patrickconley.arbutus.scanner.strategy;

import java.io.File;
import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.ScannerException;

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
     * @throws ScannerException If the file doesn't have audio or can't be read
     */
    Map<String, Tag> readTags(File file) throws ScannerException;

    /**
     * Release any native resources
     */
    void release();
}
