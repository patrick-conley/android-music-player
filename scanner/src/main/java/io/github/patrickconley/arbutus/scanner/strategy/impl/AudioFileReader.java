package io.github.patrickconley.arbutus.scanner.strategy.impl;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import io.github.patrickconley.arbutus.scanner.ScannerException;

class AudioFileReader {

    /**
     * @param file
     *         to read
     *
     * @return tag fields of the file
     *
     * @throws ScannerException on any of various org.jaudiotagger exceptions
     */
    Iterator<TagField> getTagFieldIterator(File file) throws ScannerException {
        try {
            return AudioFileIO.read(file).getTag().getFields();
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new ScannerException(e);
        }
    }

}
