package io.github.patrickconley.arbutus.scanner.strategy.impl;

import org.jaudiotagger.tag.TagField;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.ScannerException;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

public class VorbisCommentTagStrategy implements TagStrategy {

    /**
     * Read Vorbis Comments.
     */
    @Override
    public Map<String, Tag> readTags(File file)
            throws ScannerException {
        Map<String, Tag> comments = new HashMap<>();

        Iterator<TagField> tagIterator = new AudioFileReader().getTagFieldIterator(file);
        while (tagIterator.hasNext()) {
            TagField tag = tagIterator.next();
            String key = tag.getId().toLowerCase(Locale.getDefault());
            comments.put(key, new Tag(key, tag.toString()));
        }

        return comments;
    }

    @Override
    public void release() {
        // nothing to do
    }
}
