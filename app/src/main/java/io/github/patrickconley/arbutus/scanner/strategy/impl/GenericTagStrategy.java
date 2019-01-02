package io.github.patrickconley.arbutus.scanner.strategy.impl;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import io.github.patrickconley.arbutus.domain.model.Tag;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

/**
 * Read the tags from an MP3 file using {@link MediaMetadataRetriever}.
 * JAudioTagger uses sun.nio.ch.DirectBuffer (which is not available on Android)
 * to work around a Windows bug.
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

    @Override
    public List<Tag> getTags(File file) throws Exception {
        List<Tag> tags = new LinkedList<>();

        // Scan the file. Identifying the MIME type is a bit tricky, so let the
        // retriever determine what it can read.
        try {
            metadataRetriever.setDataSource(file.getAbsolutePath());
        } catch (RuntimeException e) {
            if (e.getMessage().endsWith("0xFFFFFFEA")) {
                Log.w(tag, "Skipping non-media file " + file);
                return null;
            } else {
                throw e;
            }
        }

        // Check the file is audio
        if (metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) ==
            null) {
            Log.w(tag, "Skipping non-audio media file " + file);
            return null;
        }

        // Read and store data for each key
        for (int i = 0; i < keys.size(); i++) {
            String value = metadataRetriever.extractMetadata(keys.keyAt(i));
            if (value != null && !value.equals("0")) {
                tags.add(new Tag(keys.valueAt(i), value));
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
