package io.github.patrickconley.arbutus.scanner.strategy.impl;

import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.ScannerException;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

public class Mp4TagStrategy implements TagStrategy {

    private static final Map<String, String> METADATA_KEYS;

    /*
     * Reverse JAudioTagger's mapping between field IDs and names.
     */
    static {
        METADATA_KEYS = new HashMap<>();

        for (Mp4FieldKey key : Mp4FieldKey.values()) {
            METADATA_KEYS.put(key.getFieldName(), key.toString().toLowerCase(Locale.US));
        }

        // Replace some keys with my standard names
        METADATA_KEYS.put(Mp4FieldKey.GENRE_CUSTOM.getFieldName(), "genre");
        METADATA_KEYS.put(Mp4FieldKey.TRACK.getFieldName(), "tracknumber");
        METADATA_KEYS.put(Mp4FieldKey.DAY.getFieldName(), "date");
    }

    @Override
    public Map<String, Tag> readTags(File file) throws ScannerException {
        Map<String, Tag> tags = new HashMap<>();

        Iterator<TagField> tagIterator = new AudioFileReader().getTagFieldIterator(file);
        while (tagIterator.hasNext()) {
            TagField tag = tagIterator.next();
            String key = getKey(tag);
            tags.put(key, new Tag(key, tag.toString()));
        }

        return tags;
    }

    private String getKey(TagField tag) {
        String tagId = tag.getId();
        return METADATA_KEYS.containsKey(tagId) ? METADATA_KEYS.get(tagId) : tagId;
    }

    @Override
    public void release() {
        // nothing to do
    }

}
