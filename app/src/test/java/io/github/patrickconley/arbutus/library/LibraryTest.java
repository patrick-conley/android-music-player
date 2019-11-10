package io.github.patrickconley.arbutus.library;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.github.patrickconley.arbutus.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Feature: populate a library with the default artist -> album -> track structure
 */
@RunWith(MockitoJUnitRunner.class)
public class LibraryTest {

    @Mock
    private Uri uri;

    @Mock
    private LibraryNodeDao nodeDao;

    @Mock
    private LibraryEntryDao entryDao;

    @InjectMocks
    private Library library = new Library();

    private LibraryNode artists;
    private LibraryNode albums;
    private LibraryNode titles;

    @Before
    public void setupLibraryNodes() {
        artists = new LibraryNode(null, LibraryContentType.Type.Tag, "artist");
        artists.setId(1);
        when(nodeDao.getChildrenOf(null)).thenReturn(Collections.singletonList(artists));

        albums = new LibraryNode(artists, LibraryContentType.Type.Tag, "album");
        albums.setId(2);
        when(nodeDao.getChildrenOf(artists)).thenReturn(Collections.singletonList(albums));

        titles = new LibraryNode(albums, LibraryContentType.Type.Track, "title");
        titles.setId(3);
        when(nodeDao.getChildrenOf(albums)).thenReturn(Collections.singletonList(titles));
    }

    /*
     * Given the library is empty, when I insert a track with no tags, then the track is inserted
     *  at No Artist -> No Album -> No title.
     */
    @Test
    public void insertTrackWithoutTags() {
        Track track = new Track(uri);
        library.addTrack(track, Collections.<String, Tag>emptyMap());

        LibraryEntry artist = new LibraryEntry(null, artists, null, null);
        verify(entryDao).insert(artist);

        LibraryEntry album = new LibraryEntry(artist, albums, null, null);
        verify(entryDao).insert(album);

        LibraryEntry title = new LibraryEntry(album, titles, null, track);
        verify(entryDao).insert(title);
    }

    /*
     * Given the library is empty, when I insert a track with a title, then the track is inserted
     *  at No Artist -> No Album -> Title.
     */
    @Test
    public void insertTrackWithLeafTag() {
        Track track = new Track(uri);
        Tag tag = new Tag("title", "foo");
        library.addTrack(track, Collections.singletonMap("title", tag));

        LibraryEntry artist = new LibraryEntry(null, artists, null, null);
        verify(entryDao).insert(artist);

        LibraryEntry album = new LibraryEntry(artist, albums, null, null);
        verify(entryDao).insert(album);

        LibraryEntry title = new LibraryEntry(album, titles, tag, track);
        verify(entryDao).insert(title);
    }

    /*
     * Given the library is empty, when I insert a track with an album, then the track is
     * inserted at No Artist -> Album -> No Title.
     */
    @Test
    public void insertTrackWithInnerTag() {
        Track track = new Track(uri);
        Tag tag = new Tag("album", "foo");
        library.addTrack(track, Collections.singletonMap("album", tag));

        LibraryEntry artist = new LibraryEntry(null, artists, null, null);
        verify(entryDao).insert(artist);

        LibraryEntry album = new LibraryEntry(artist, albums, tag, null);
        verify(entryDao).insert(album);

        LibraryEntry title = new LibraryEntry(album, titles, null, track);
        verify(entryDao).insert(title);
    }

    /*
     * Given the library is empty, when I insert a track with an artist, then the track is
     * inserted at Artist -> No Album -> No Title.
     */
    @Test
    public void insertTrackWithRootTag() {
        Track track = new Track(uri);
        Tag tag = new Tag("artist", "foo");
        library.addTrack(track, Collections.singletonMap("artist", tag));

        LibraryEntry artist = new LibraryEntry(null, artists, tag, null);
        verify(entryDao).insert(artist);

        LibraryEntry album = new LibraryEntry(artist, albums, null, null);
        verify(entryDao).insert(album);

        LibraryEntry title = new LibraryEntry(album, titles, null, track);
        verify(entryDao).insert(title);
    }

    /*
     * Given the library is empty, when I insert a track with an artist and title, then the track is inserted at Artist -> No Album -> Title.
     */
    @Test
    public void insertTrackWithTwoTags() {
        Track track = new Track(uri);
        Tag artistTag = new Tag("artist", "foo");
        Tag titleTag = new Tag("title", "foo");
        library.addTrack(track, buildTagMap(Arrays.asList(artistTag, titleTag)));

        LibraryEntry artist = new LibraryEntry(null, artists, artistTag, null);
        verify(entryDao).insert(artist);

        LibraryEntry album = new LibraryEntry(artist, albums, null, null);
        verify(entryDao).insert(album);

        LibraryEntry title = new LibraryEntry(album, titles, titleTag, track);
        verify(entryDao).insert(title);
    }

    private Map<String, Tag> buildTagMap(Collection<Tag> tags) {
        Map<String, Tag> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag);
        }

        return map;
    }

    /*
     * Given the library is empty, when I insert a track with an artist, album, title, and genre,
     * then the track is inserted at Artist -> Album -> Title
     */
    @Test
    public void insertTrackWithExtraTag() {
        Track track = new Track(uri);
        Tag genreTag = new Tag("genre", "foo");
        Tag artistTag = new Tag("artist", "bar");
        Tag albumTag = new Tag("album", "baz");
        Tag titleTag = new Tag("title", "ham");
        library.addTrack(track,
                         buildTagMap(Arrays.asList(genreTag, artistTag, albumTag, titleTag)));

        LibraryEntry artist = new LibraryEntry(null, artists, artistTag, null);
        verify(entryDao).insert(artist);

        LibraryEntry album = new LibraryEntry(artist, albums, albumTag, null);
        verify(entryDao).insert(album);

        LibraryEntry title = new LibraryEntry(album, titles, titleTag, track);
        verify(entryDao).insert(title);

        verifyNoMoreInteractions(entryDao);
    }


    /*
     * Given the library will fail to insert a title entry, when I add a track to the library,
     * then nothing is inserted.
     *
     * TODO this should be tested in FileScanVisitor... or FileScanVisitor should delegate
     *  track-saving to Library
     */

    /*
     * Given the library contains a track, when I insert a track with the same artist and album,
     * then the track is inserted and a new title entry is created.
     */
    @Test
    public void insertTrackWithNewLeaf() {
        Tag artist = new Tag("artist", "foo");
        Tag album = new Tag("album", "bar");
        Tag title = new Tag("title", "ham");

        Track track1 = new Track(uri);
        Track track2 = new Track(mock(Uri.class));

        library.addTrack(track1, buildTagMap(Arrays.asList(artist, album, title)));
        library.addTrack(track2, buildTagMap(Arrays.asList(artist, album, title)));

        LibraryEntry artistEntry = new LibraryEntry(null, artists, artist, null);
        verify(entryDao).insert(artistEntry);

        LibraryEntry albumEntry = new LibraryEntry(artistEntry, albums, album, null);
        verify(entryDao).insert(albumEntry);

        verify(entryDao).insert(new LibraryEntry(albumEntry, titles, title, track1));
        verify(entryDao).insert(new LibraryEntry(albumEntry, titles, title, track2));
    }

    /*
     * Given the library contains a track, when I insert a track with the name artist and title,
     * then the track is inserted and a new album and title entries are created.
     */
}
