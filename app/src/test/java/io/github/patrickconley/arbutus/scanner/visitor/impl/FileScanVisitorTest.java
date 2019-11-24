package io.github.patrickconley.arbutus.scanner.visitor.impl;

import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.LibraryManager;
import io.github.patrickconley.arbutus.metadata.TrackManager;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.scanner.ScannerException;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.strategy.StrategyFactory;
import io.github.patrickconley.arbutus.scanner.strategy.TagStrategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FileScanVisitorTest {

    @Mock
    private AppDatabase db;

    @Mock
    private TrackManager trackManager;

    @Mock
    private LibraryManager libraryManager;

    @Mock
    private StrategyFactory strategyFactory;

    @Mock
    private TagStrategy tagStrategy;

    @InjectMocks
    @SuppressWarnings("deprecation")
    private FileScanVisitor visitor = new FileScanVisitor();

    @Before
    public void setupStrategyFactory() throws ScannerException {
        when(strategyFactory.getStrategy(ArgumentMatchers.<MediaFile>any()))
               .thenReturn(tagStrategy);
        when(tagStrategy.readTags(ArgumentMatchers.<File>any()))
               .thenReturn(new HashMap<String, Tag>());
    }

    /**
     * Given a folder, when I visit the folder, then nothing happens.
     */
    @Test
    public void visitingAFolderPassesWithNoEffect() {
        assertTrue(visitor.visit(new MediaFolder(new File("foo"))));
    }

    /**
     * Given a file that can't be read, when I visit the file, then nothing is saved.
     */
    @Test
    public void visitingAnUnreadableFileFails() throws ScannerException {
        when(tagStrategy.readTags(ArgumentMatchers.<File>any())).thenThrow(ScannerException.class);

        assertFalse(visitor.visit(new MediaFile(new File("foo"))));
    }

    /**
     * Given I can't write to the database, when I visit the file, then nothing is saved.
     */
    @Test
    public void visitingAFileWithoutDatabaseFails() {
        // I don't allow the Uri to be null, but it's generated somewhere in the code by
        // Android's obtuse Uri.parse
        MediaFile mediaFile = mock(MediaFile.class);
        when(mediaFile.getFile()).thenReturn(new File("foo"));
        when(mediaFile.getUri()).thenReturn(mock(Uri.class));

        doThrow(SQLiteConstraintException.class).when(db).runInTransaction(
                ArgumentMatchers.<Runnable>any());

        assertFalse(visitor.visit(mediaFile));
    }

    /**
     * Given a valid file, when I visit the file, then everything is saved.
     */
    @Test
    public void visitingAValidFileSavesIt() {
        assertTrue(visitor.visit(new MediaFile(new File("foo"))));
    }

}
