package io.github.patrickconley.arbutus.scanner.strategy;

import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.strategy.impl.GenericTagStrategy;
import io.github.patrickconley.arbutus.scanner.strategy.impl.Mp4TagStrategy;
import io.github.patrickconley.arbutus.scanner.strategy.impl.VorbisCommentTagStrategy;

public class StrategyFactory {

    private TagStrategy vorbisStrategy;
    private TagStrategy genericStrategy;
    private TagStrategy mp4Strategy;

    /**
     * Identify the appropriate tag-reading strategy using the media file's
     * extension.
     *
     * @param file File to read
     */
    public TagStrategy getStrategy(MediaFile file) {
        int index = file.toString().lastIndexOf('.');
        String extension = index >= 0 ? file.toString().substring(index) : "";

        switch (extension) {
            case ".ogg":
            case ".mkv":
            case ".flac":
                return getVorbisCommentTagStrategy();
            case ".mp4":
            case ".m4a":
                return getMp4TagStrategy();
            default:
                return getGenericTagStrategy();
        }
    }

    /**
     * Release native resources. Call this when finished.
     */
    public void release() {
        if (genericStrategy != null) {
            genericStrategy.release();
            genericStrategy = null;
        }
        if (vorbisStrategy != null) {
            vorbisStrategy.release();
            vorbisStrategy = null;
        }
        if (mp4Strategy != null) {
            mp4Strategy.release();
            mp4Strategy = null;
        }
    }

    private TagStrategy getVorbisCommentTagStrategy() {
        if (vorbisStrategy == null) {
            vorbisStrategy = new VorbisCommentTagStrategy();
        }

        return vorbisStrategy;
    }

    private TagStrategy getGenericTagStrategy() {
        if (genericStrategy == null) {
            genericStrategy = new GenericTagStrategy();
        }

        return genericStrategy;
    }

    private TagStrategy getMp4TagStrategy() {
        if (mp4Strategy == null) {
            mp4Strategy = new Mp4TagStrategy();
        }

        return mp4Strategy;
    }

}
