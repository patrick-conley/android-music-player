package io.github.patrickconley.arbutus.scanner.strategy.impl;

import android.media.MediaMetadataRetriever;
import androidx.annotation.NonNull;
import android.util.SparseArray;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.ScannerException;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

/**
 * Read the tags from an MP3 file using {@link MediaMetadataRetriever}. JAudioTagger uses sun.nio.ch.DirectBuffer (which
 * is not available on Android) to work around a Windows bug.
 */
public class GenericTagStrategy implements TagStrategy {

    private static final SparseArray<String> METADATA_KEYS;

    static {
        // Linter claims a sparse array performs better than a map
        METADATA_KEYS = new SparseArray<>();
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_ALBUM, "album");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "albumartist");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_ARTIST, "artist");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_AUTHOR, "author");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER, "tracknumber");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_COMPILATION, "compilation");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_COMPOSER, "composer");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_DATE, "date");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER, "discnumber");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_GENRE, "genre");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_LOCATION, "location");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS, "tracktotal");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_TITLE, "title");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_WRITER, "writer");
        METADATA_KEYS.put(MediaMetadataRetriever.METADATA_KEY_YEAR, "year");
    }

    private MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

    public void release() {
        metadataRetriever.release();
    }

    @Override
    public Map<String, Tag> readTags(File file) throws ScannerException {

        // Scan the file. Identifying the MIME type is a bit tricky, so let the
        // retriever determine what it can read.
        setRetrieverDataSource(file);

        // Check the file is audio
        verifyFileHasAudio();

        // Read and store data for each key
        return readTags();
    }

    private void setRetrieverDataSource(File file) throws ScannerException {
        try {
            metadataRetriever.setDataSource(file.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().endsWith("0xFFFFFFEA")) {
                throw new ScannerException("Skipping non-media file", e);
            } else {
                throw new ScannerException(e);
            }
        }
    }

    private void verifyFileHasAudio() throws ScannerException {
        if (metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == null) {
            throw new ScannerException("Skipping non-audio media file");
        }
    }

    @NonNull
    private Map<String, Tag> readTags() {
        Map<String, Tag> tags = new HashMap<>();
        for (int i = 0; i < METADATA_KEYS.size(); i++) {
            String value = metadataRetriever.extractMetadata(METADATA_KEYS.keyAt(i));
            if (value != null && !value.equals("0")) {
                tags.put(METADATA_KEYS.valueAt(i), new Tag(METADATA_KEYS.valueAt(i), value));
            }
        }
        return tags;
    }

}
