package io.github.patrickconley.arbutus.scanner.model.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitorBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MediaFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private MediaVisitorBase visitor;

    /**
     * Given a media file that doesn't exist, when I read the media file, then its visitor isn't
     * visited.
     */
    @Test
    public void missingFile() throws IOException {
        File file = folder.newFile();
        assertTrue(file.delete());

        assertEquals(0L, new MediaFile(file).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media file that isn't readable, when I read the media file, then its visitor isn't
     * visited.
     */
    @Test
    public void privateFile() throws IOException {
        File file = folder.newFile();
        assertTrue(file.setReadable(false));

        assertEquals(0L, new MediaFile(file).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a valid media file, when I read the media file, then its visitor is visited and it
     * returns the number of files read.
     */
    @Test
    public void validFile() throws IOException {
        File file = folder.newFile();

        assertEquals(1L, new MediaFile(file).accept(visitor));
        verify(visitor).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

}
