package io.github.patrickconley.arbutus.scanner.strategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.strategy.impl.GenericTagStrategy;
import io.github.patrickconley.arbutus.scanner.strategy.impl.Mp4TagStrategy;
import io.github.patrickconley.arbutus.scanner.strategy.impl.VorbisCommentTagStrategy;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class StrategyFactoryTest {

    @Parameterized.Parameter
    public File file;

    @Parameterized.Parameter(1)
    public Class<?> expectedStrategy;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { new File("track.ogg"), VorbisCommentTagStrategy.class },
                { new File("track.mkv"), VorbisCommentTagStrategy.class },
                { new File("track.flac"), VorbisCommentTagStrategy.class },
                { new File("track.mp4"), Mp4TagStrategy.class },
                { new File("track.m4a"), Mp4TagStrategy.class },
                { new File("track.mp3"), GenericTagStrategy.class },
                { new File("track.aac"), GenericTagStrategy.class },
                });
    }

    @Test
    public void getStrategy() {
        assertThat(new StrategyFactory().getStrategy(new MediaFile(file)))
                .isInstanceOf(expectedStrategy);
    }

}
