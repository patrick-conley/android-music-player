package io.github.patrickconley.arbutus.scanner.strategy.impl;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

public class VorbisCommentTagStrategy implements TagStrategy {

    /**
     * Read Vorbis Comments.
     */
    @Override
    public Map<String, Tag> readTags(File file)
            throws TagException, ReadOnlyFileException, CannotReadException,
                   InvalidAudioFrameException, IOException {
        Map<String, Tag> comments = new HashMap<>();

        Iterator<TagField> tags = AudioFileIO.read(file).getTag().getFields();
        while (tags.hasNext()) {
            TagField tag = tags.next();
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
