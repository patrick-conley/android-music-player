package io.github.patrickconley.arbutus.library;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.library.dao.LibraryContentTypeDao;
import io.github.patrickconley.arbutus.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.library.model.LibraryNode;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;

import static com.google.common.truth.Truth.assertThat;

/**
 * Feature: populate a library which has the default artist -> album -> track structure
 */
@RunWith(AndroidJUnit4.class)
public class LibraryManagerTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();

    private LibraryContentTypeDao contentTypeDao = db.libraryContentTypeDao();
    private LibraryNodeDao nodeDao = db.libraryNodeDao();
    private LibraryEntryDao entryDao = db.libraryEntryDao();
    private LibraryManager library = new LibraryManager(db);

    private LibraryNode artists;
    private LibraryNode albums;
    private LibraryNode titles;

    private Uri uri = Uri.parse("file://sample.ogg");

    @Before
    public void setupLibraryNodes() {
        contentTypeDao.insert(new LibraryContentType(LibraryContentType.Type.TAG));
        contentTypeDao.insert(new LibraryContentType(LibraryContentType.Type.TRACK));

        artists = nodeDao.insert(new LibraryNode(null, LibraryContentType.Type.TAG, "artist"));
        albums = nodeDao.insert(new LibraryNode(artists, LibraryContentType.Type.TAG, "album"));
        titles = nodeDao.insert(new LibraryNode(albums, LibraryContentType.Type.TRACK, "title"));
    }

    @After
    public void after() {
        db.close();
    }

    /*
     * Given the library is empty, when I insert a track with no tags, then the track is inserted
     *  at No Artist -> No Album -> No title.
     */
    @Test
    public void insertTrackWithoutTags() {
        Track track = new Track(uri);
        library.addTrack(track, Collections.<String, Tag>emptyMap());

        LibraryEntry artist = buildLibraryEntry(null, artists, null, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, null, null);
        LibraryEntry title = buildLibraryEntry(album, titles, null, track);

        assertThat(getAllEntries()).containsExactly(artist, album, title);
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

        LibraryEntry artist = buildLibraryEntry(null, artists, null, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, null, null);
        LibraryEntry title = buildLibraryEntry(album, titles, tag, track);

        assertThat(getAllEntries()).containsExactly(artist, album, title);
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

        LibraryEntry artist = buildLibraryEntry(null, artists, null, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, tag, null);
        LibraryEntry title = buildLibraryEntry(album, titles, null, track);

        assertThat(getAllEntries()).containsExactly(artist, album, title);
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

        LibraryEntry artist = buildLibraryEntry(null, artists, tag, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, null, null);
        LibraryEntry title = buildLibraryEntry(album, titles, null, track);

        assertThat(getAllEntries()).containsExactly(artist, album, title);
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

        LibraryEntry artist = buildLibraryEntry(null, artists, artistTag, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, null, null);
        LibraryEntry title = buildLibraryEntry(album, titles, titleTag, track);

        assertThat(getAllEntries()).containsExactly(artist, album, title);
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

        LibraryEntry artist = buildLibraryEntry(null, artists, artistTag, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, albumTag, null);
        LibraryEntry title = buildLibraryEntry(album, titles, titleTag, track);

        assertThat(getAllEntries()).containsExactly(artist, album, title);
    }

    /**
     * Given the library will fail to insert a title entry, when I add a track to the library,
     * then nothing is inserted.
     *
     * FIXME: without mocking I don't think I have any way to test this
     */

    /**
     * Given the library will fail to insert a album entry, when I add a track to the library,
     * then nothing is inserted.
     *
     * FIXME: without mocking I don't think I have any way to test this.
     */

    /**
     * Given the library contains a track, when I insert a track with different artist, album, and
     * title, then the track is inserted and new entries are created.
     */
    @Test
    public void insertDistinctTrack() {
        Tag artist1Tag = new Tag("artist", "foo");
        Tag album1Tag = new Tag("album", "bar");
        Tag title1Tag = new Tag("title", "baz");

        Track track1 = new Track(uri);
        library.addTrack(track1, buildTagMap(Arrays.asList(artist1Tag, album1Tag, title1Tag)));

        LibraryEntry artist1 = buildLibraryEntry(null, artists, artist1Tag, null);
        LibraryEntry album1 = buildLibraryEntry(artist1, albums, album1Tag, null);
        LibraryEntry title1 = buildLibraryEntry(album1, titles, title1Tag, track1);

        assertThat(getAllEntries()).containsExactly(artist1, album1, title1);

        Tag artist2Tag = new Tag("artist", "ham");
        Tag album2Tag = new Tag("album", "spam");
        Tag title2Tag = new Tag("title", "eggs");

        Track track2 = new Track(Uri.parse("file://sample.mp3"));
        library.addTrack(track2, buildTagMap(Arrays.asList(artist2Tag, album2Tag, title2Tag)));

        LibraryEntry artist2 = buildLibraryEntry(null, artists, artist2Tag, null);
        LibraryEntry album2 = buildLibraryEntry(artist2, albums, album2Tag, null);
        LibraryEntry title2 = buildLibraryEntry(album2, titles, title2Tag, track2);

        assertThat(getAllEntries()).containsExactly(artist1, album1, title1, artist2, album2, title2);
    }

    /**
     * Given the library contains a track, when I insert a track with the same artist, album, and
     * title, then the track is inserted and a new title entry is created.
     */
    @Test
    public void insertIdenticalTrack() {
        Tag artistTag = new Tag("artist", "foo");
        Tag albumTag = new Tag("album", "bar");
        Tag titleTag = new Tag("title", "ham");

        Track track1 = new Track(uri);
        Track track2 = new Track(Uri.parse("file://sample.mp3"));

        library.addTrack(track1, buildTagMap(Arrays.asList(artistTag, albumTag, titleTag)));
        library.addTrack(track2, buildTagMap(Arrays.asList(artistTag, albumTag, titleTag)));

        LibraryEntry artist = buildLibraryEntry(null, artists, artistTag, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, albumTag, null);
        LibraryEntry title1 = buildLibraryEntry(album, titles, titleTag, track1);
        LibraryEntry title2 = buildLibraryEntry(album, titles, titleTag, track2);

        assertThat(getAllEntries()).containsExactly(artist, album, title1, title2);
    }

    /*
     * Given the library contains a track, when I insert a track with the same artist and album,
     * then the track is inserted and a new title entry is created.
     */
    @Test
    public void insertTrackWithNewLeaf() {
        Tag artistTag = new Tag("artist", "foo");
        Tag albumTag = new Tag("album", "bar");
        Tag title1Tag = new Tag("title", "baz");
        Tag title2Tag = new Tag("title", "qux");

        Track track1 = new Track(uri);
        Track track2 = new Track(Uri.parse("file://sample.mp3"));

        library.addTrack(track1, buildTagMap(Arrays.asList(artistTag, albumTag, title1Tag)));
        library.addTrack(track2, buildTagMap(Arrays.asList(artistTag, albumTag, title2Tag)));

        LibraryEntry artist = buildLibraryEntry(null, artists, artistTag, null);
        LibraryEntry album = buildLibraryEntry(artist, albums, albumTag, null);
        LibraryEntry title1 = buildLibraryEntry(album, titles, title1Tag, track1);
        LibraryEntry title2 = buildLibraryEntry(album, titles, title2Tag, track2);

        assertThat(getAllEntries()).containsExactly(artist, album, title1, title2);
    }

    /**
     * Given the library contains a track, when I insert a track with the name artist and title,
     * then the track is inserted and a new album and title entries are created.
     */
    @Test
    public void insertTrackWithNewAlbum() {
        Tag artistTag = new Tag("artist", "foo");
        Tag album1Tag = new Tag("album", "bar");
        Tag album2Tag = new Tag("album", "ham");
        Tag titleTag = new Tag("title", "baz");

        Track track1 = new Track(uri);
        Track track2 = new Track(Uri.parse("file://sample.mp3"));

        library.addTrack(track1, buildTagMap(Arrays.asList(artistTag, album1Tag, titleTag)));
        library.addTrack(track2, buildTagMap(Arrays.asList(artistTag, album2Tag, titleTag)));

        LibraryEntry artist = buildLibraryEntry(null, artists, artistTag, null);
        LibraryEntry album1 = buildLibraryEntry(artist, albums, album1Tag, null);
        LibraryEntry title1 = buildLibraryEntry(album1, titles, titleTag, track1);

        LibraryEntry album2 = buildLibraryEntry(artist, albums, album2Tag, null);
        LibraryEntry title2 = buildLibraryEntry(album2, titles, titleTag, track2);

        assertThat(getAllEntries()).containsExactly(artist, album1, album2, title1, title2);
    }

    private long id = 0L;

    private LibraryEntry buildLibraryEntry(
            LibraryEntry parent, LibraryNode node, Tag tag, Track track
    ) {
        LibraryEntry entry = new LibraryEntry(parent, node, tag, track);
        entry.setId(++id);
        return entry;
    }

    private Map<String, Tag> buildTagMap(Collection<Tag> tags) {
        Map<String, Tag> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag);
        }

        return map;
    }

    private Set<LibraryEntry> getAllEntries() {
        return getDescendantsOf(null);
    }

    private Set<LibraryEntry> getDescendantsOf(LibraryEntry parent) {
        Set<LibraryEntry> entries = new HashSet<>(entryDao.getChildrenOf(parent));
        for (LibraryEntry entry : new HashSet<>(entries)) {
            entries.addAll(getDescendantsOf(entry));
        }

        return entries;
    }

}
