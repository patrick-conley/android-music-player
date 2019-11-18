package io.github.patrickconley.arbutus.scanner.strategy.impl;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

/**
 * Read the tags from an MP3 file using {@link MediaMetadataRetriever}. JAudioTagger uses sun.nio.ch.DirectBuffer (which
 * is not available on Android) to work around a Windows bug.
 */
public class GenericTagStrategy implements TagStrategy {
    private final String tag = getClass().getName();

    private SparseArray<String> keys = null;

    private MediaMetadataRetriever metadataRetriever;

    public GenericTagStrategy() {
        this.metadataRetriever = new MediaMetadataRetriever();

        buildKeyMap();
    }

    public void release() {
        metadataRetriever.release();
    }

    @Nullable
    @Override
    public Map<String, Tag> readTags(File file) {

        // Scan the file. Identifying the MIME type is a bit tricky, so let the
        // retriever determine what it can read.
        if (!setRetrieverDataSource(file)) {
            return null;
        }

        // Check the file is audio
        if (checkFileIsAudio(file)) {
            return null;
        }

        // Read and store data for each key
        return readTags();
    }

    private boolean setRetrieverDataSource(File file) {
        try {
            metadataRetriever.setDataSource(file.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().endsWith("0xFFFFFFEA")) {
                Log.w(tag, "Skipping non-media file " + file);
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    private boolean checkFileIsAudio(File file) {
        if (metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == null) {
            Log.w(tag, "Skipping non-audio media file " + file);
            return false;
        }
        return true;
    }

    @NonNull
    private Map<String, Tag> readTags() {
        Map<String, Tag> tags = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String value = metadataRetriever.extractMetadata(keys.keyAt(i));
            if (value != null && !value.equals("0")) {
                tags.put(keys.valueAt(i), new Tag(keys.valueAt(i), value));
            }
        }
        return tags;
    }

    /*
     * Identify the relevant keys, which are just stored as global ints in
     * MediaMetadataRetriever.
     */
    private void buildKeyMap() {
        keys = new SparseArray<>();
        keys.put(MediaMetadataRetriever.METADATA_KEY_ALBUM, "album");
        keys.put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "albumartist");
        keys.put(MediaMetadataRetriever.METADATA_KEY_ARTIST, "artist");
        keys.put(MediaMetadataRetriever.METADATA_KEY_AUTHOR, "author");
        keys.put(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER, "tracknumber");
        keys.put(MediaMetadataRetriever.METADATA_KEY_COMPILATION, "compilation");
        keys.put(MediaMetadataRetriever.METADATA_KEY_COMPOSER, "composer");
        keys.put(MediaMetadataRetriever.METADATA_KEY_DATE, "date");
        keys.put(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER, "discnumber");
        keys.put(MediaMetadataRetriever.METADATA_KEY_GENRE, "genre");
        keys.put(MediaMetadataRetriever.METADATA_KEY_LOCATION, "location");
        keys.put(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS, "tracktotal");
        keys.put(MediaMetadataRetriever.METADATA_KEY_TITLE, "title");
        keys.put(MediaMetadataRetriever.METADATA_KEY_WRITER, "writer");
        keys.put(MediaMetadataRetriever.METADATA_KEY_YEAR, "year");
    }

}
