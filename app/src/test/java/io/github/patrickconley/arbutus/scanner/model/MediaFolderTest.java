package io.github.patrickconley.arbutus.scanner.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import io.github.patrickconley.arbutus.scanner.model.impl.MediaFile;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MediaFolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private MediaVisitor visitor;

    /**
     * Given a media folder that doesn't exist, when I read the media folder, then its visitor isn't
     * visited.
     */
    @Test
    public void missingFolder() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(mediaFolder.delete());

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that's a file, when I read the media folder, then its visitor isn't
     * visited.
     */
    @Test
    public void folderIsActuallyFile() throws IOException {
        File mediaFile = folder.newFile();

        assertEquals(0L,new MediaFolder(mediaFile).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that isn't readable, when I read the media folder, then its visitor
     * isn't visited.
     */
    @Test
    public void privateFolder() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());
        assertTrue(mediaFolder.setExecutable(false));

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that's a file that isn't readable, when I read the media folder, then
     * its visitor isn't visited.
     */
    @Test
    public void privateFile() throws IOException {
        File mediaFile = folder.newFile();
        assertTrue(mediaFile.setExecutable(false));
        assertTrue(mediaFile.setReadable(false));

        assertEquals(0L,new MediaFolder(mediaFile).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given an empty media folder, when I read the folder, then its visitor is visited on the
     * media folder and children and it returns the number of files read.
     */
    @Test
    public void emptyFolder() throws IOException {
        File mediaFolder = folder.newFolder();

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that has a .nomedia file, when I read the media folder, then its
     * visitor isn't visited.
     */
    @Test
    public void folderWithNomedia() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());
        assertTrue(new File(mediaFolder, ".nomedia").createNewFile());

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that isn't readable and has a .nomedia file, when I read the
     * media folder, then its visitor isn't visited.
     */
    @Test
    public void privateFolderWithNomedia() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());
        assertTrue(new File(mediaFolder, ".nomedia").createNewFile());
        // permission has to be set after creating children
        assertTrue(mediaFolder.setExecutable(false));

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that has a .NOMEDIA file, when I read the media folder, then its
     * visitor isn't visited.
     */
    @Test
    public void folderWithAllCapsNomedia() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());
        assertTrue(new File(mediaFolder, ".NOMEDIA").createNewFile());

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that isn't readable and has a .NOMEDIA file, when I read the media
     * folder, then its visitor isn't visited.
     */
    @Test
    public void privateFolderWithAllCapsNomedia() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());
        assertTrue(new File(mediaFolder, ".NOMEDIA").createNewFile());
        // permission has to be set after creating children
        assertTrue(mediaFolder.setExecutable(false));

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a file, when I read the folder, then its visitor is
     * visited on the media folder and children and it returns the number of files read.
     */
    @Test
    public void folderWithFile() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());

        assertEquals(1L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a folder, when I read the folder, then its visitor is
     * visited on the media folder and children and it returns the number of files read.
     */
    @Test
    public void folderWithFolder() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "child").mkdir());

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains two files, when I read the folder, then its visitor is
     * visited on the media folder and children and it returns the number of files read.
     */
    @Test
    public void folderWithTwoFiles() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "sample1.ogg").createNewFile());
        assertTrue(new File(mediaFolder, "sample2.ogg").createNewFile());

        assertEquals(2L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a folder and a file, when I read the folder, then its
     * visitor is visited on the media folder and children and it returns the number of files read.
     */
    @Test
    public void folderWithFolderAndFile() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "child").mkdir());
        assertTrue(new File(mediaFolder, "sample.ogg").createNewFile());

        assertEquals(1L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains two folders, when I read the folder, then its visitor is
     * visited on the media folder and children and it returns the number of files read.
     */
    @Test
    public void folderWithTwoFolders() throws IOException {
        File mediaFolder = folder.newFolder();
        assertTrue(new File(mediaFolder, "child1").mkdir());
        assertTrue(new File(mediaFolder, "child2").mkdir());

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(0)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(3)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a folder with a file, when I read the folder, then its
     * visitor is visited on the media folder and descendants and it returns the number of files
     * read.
     */
    @Test
    public void folderWithChild() throws IOException {
        File mediaFolder = folder.newFolder();
        File child = new File(mediaFolder, "child");
        assertTrue(child.mkdir());
        assertTrue(new File(child, "sample.ogg").createNewFile());

        assertEquals(1L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a nomedia file and a folder with a file, when I read the
     * folder, then its visitor isn't visited.
     */
    @Test
    public void folderWithNomediaAndChild() throws IOException {
        File mediaFolder = folder.newFolder();
        File child = new File(mediaFolder, "child");
        assertTrue(child.mkdir());
        assertTrue(new File(child, "sample.ogg").createNewFile());
        assertTrue(new File(mediaFolder, ".nomedia").createNewFile());

        assertEquals(0L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, never()).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a file and a folder with a file, when I read the folder,
     * then its visitor is visited on the media folder and children and it returns the number of
     * files read.
     */
    @Test
    public void folderWithFileAndChild() throws IOException {
        File mediaFolder = folder.newFolder();
        File child = new File(mediaFolder, "child");
        assertTrue(child.mkdir());
        assertTrue(new File(mediaFolder, "sample1.ogg").createNewFile());
        assertTrue(new File(child, "sample2.ogg").createNewFile());

        assertEquals(2L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains a file and a folder with a file and a nomedia file,
     * when I read the folder, then its visitor is visited on the media folder but not its child
     * folder and it returns the number of files read.
     */
    @Test
    public void folderWithFileAndChildWithNomedia() throws IOException {
        File mediaFolder = folder.newFolder();
        File child = new File(mediaFolder, "child");
        assertTrue(child.mkdir());
        assertTrue(new File(mediaFolder, "sample1.ogg").createNewFile());
        assertTrue(new File(child, "sample2.ogg").createNewFile());
        assertTrue(new File(child, ".nomedia").createNewFile());

        assertEquals(1L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(1)).visit(ArgumentMatchers.<MediaFolder>any());
    }

    /**
     * Given a media folder that contains two folders with files, when I read the folder, then its
     * visitor is visited on the media folder and children and it returns the number of files read.
     */
    @Test
    public void folderWithTwoChildren() throws IOException {
        File mediaFolder = folder.newFolder();

        File child = new File(mediaFolder, "child1");
        assertTrue(child.mkdir());
        assertTrue(new File(child, "sample1.ogg").createNewFile());

        child = new File(mediaFolder, "child2");
        assertTrue(child.mkdir());
        assertTrue(new File(child, "sample2.ogg").createNewFile());

        assertEquals(2L,new MediaFolder(mediaFolder).accept(visitor));
        verify(visitor, times(2)).visit(ArgumentMatchers.<MediaFile>any());
        verify(visitor, times(3)).visit(ArgumentMatchers.<MediaFolder>any());
    }

}
