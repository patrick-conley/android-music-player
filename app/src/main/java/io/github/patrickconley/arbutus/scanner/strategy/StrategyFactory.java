package io.github.patrickconley.arbutus.scanner.strategy;

import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.strategy.impl.GenericTagStrategy;
import io.github.patrickconley.arbutus.scanner.strategy.impl.Mp4TagStrategy;
import io.github.patrickconley.arbutus.scanner.strategy.impl.VorbisCommentTagStrategy;

public class StrategyFactory {

    private static TagStrategy defaultVorbisStrategy;
    private static TagStrategy defaultGenericStrategy;
    private static TagStrategy defaultMp4Strategy;

    private StrategyFactory() {
    }

    /**
     * Identify the appropriate tag-reading strategy using the media file's
     * extension.
     *
     * @param file File to read
     */
    public static TagStrategy getStrategy(MediaFile file) {
        int index = file.toString().lastIndexOf('.');
        String extension = index >= 0 ? file.toString().substring(index) : "";

        switch (extension) {
            case ".ogg":
            case ".mkv":
            case ".flac":
                return getDefaultVorbisCommentTagStrategy();
            case ".mp4":
            case ".m4a":
                return getDefaultMp4TagStrategy();
            default:
                return getDefaultGenericTagStrategy();
        }
    }

    /**
     * Release native resources. Call this when finished.
     */
    public static void release() {
        if (defaultGenericStrategy != null) {
            defaultGenericStrategy.release();
            defaultGenericStrategy = null;
        }
        if (defaultVorbisStrategy != null) {
            defaultVorbisStrategy.release();
            defaultVorbisStrategy = null;
        }
        if (defaultMp4Strategy != null) {
            defaultMp4Strategy.release();
            defaultMp4Strategy = null;
        }
    }

    private static TagStrategy getDefaultVorbisCommentTagStrategy() {
        if (defaultVorbisStrategy == null) {
            defaultVorbisStrategy = new VorbisCommentTagStrategy();
        }

        return defaultVorbisStrategy;
    }

    private static TagStrategy getDefaultGenericTagStrategy() {
        if (defaultGenericStrategy == null) {
            defaultGenericStrategy = new GenericTagStrategy();
        }

        return defaultGenericStrategy;
    }

    private static TagStrategy getDefaultMp4TagStrategy() {
        if (defaultMp4Strategy == null) {
            defaultMp4Strategy = new Mp4TagStrategy();
        }

        return defaultMp4Strategy;
    }

}
