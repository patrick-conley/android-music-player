package io.github.patrickconley.arbutus.scanner.strategy.impl;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;

import java.io.File;
import java.util.*;

import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

public class Mp4TagStrategy implements TagStrategy {

    private Map<String, String> keys = null;

    public Mp4TagStrategy() {
        buildKeyMap();
    }

    @Override
    public Set<Tag> readTags(File file) throws Exception {
        Set<Tag> tags = new HashSet<>();

        Iterator<TagField> tagIterator = AudioFileIO.read(file).getTag().getFields();
        while (tagIterator.hasNext()) {
            TagField tag = tagIterator.next();

            String key;
            if (keys.containsKey(tag.getId())) {
                key = keys.get(tag.getId());
            } else {
                key = tag.getId();
            }

            tags.add(new Tag(key, tag.toString()));
        }

        return tags;
    }

    @Override
    public void release() {
        // nothing to do
    }

    /*
     * Reverse JAudioTagger's mapping between field IDs and names.
     */
    private void buildKeyMap() {
        keys = new HashMap<>();

        for (Mp4FieldKey key : Mp4FieldKey.values()) {
            keys.put(key.getFieldName(), key.toString().toLowerCase(Locale.US));
        }

        // Replace some keys with my standard names
        keys.put(Mp4FieldKey.GENRE_CUSTOM.getFieldName(), "genre");
        keys.put(Mp4FieldKey.TRACK.getFieldName(), "tracknumber");
        keys.put(Mp4FieldKey.DAY.getFieldName(), "date");
    }

}
