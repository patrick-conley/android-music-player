package io.github.patrickconley.arbutus.datastorage.library;

import android.content.Context;
import android.net.Uri;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.datastorage.MetadataTestUtil;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryContentTypeDao;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryNodeDao;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryContentType;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntry;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryNode;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;
import io.github.patrickconley.arbutus.datastorage.metadata.model.Track;

import static com.google.common.truth.Truth.assertThat;

/**
 * Feature: populate a library which has the default artist -> album -> track structure
 */
@RunWith(AndroidJUnit4.class)
public class LibraryManagerTest {

    private Context context = ApplicationProvider.getApplicationContext();

    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();

    private LibraryContentTypeDao contentTypeDao = db.libraryContentTypeDao();
    private LibraryNodeDao nodeDao = db.libraryNodeDao();
    private LibraryEntryDao entryDao = db.libraryEntryDao();
    private LibraryManager library = new LibraryManager(db);

    private MetadataTestUtil metadata = new MetadataTestUtil();

    private LibraryNode artists;
    private LibraryNode albums;
    private LibraryNode titles;

    private static final Uri DEFAULT_URI = Uri.parse("file://sample.ogg");

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
    public void addTrackWithoutTags() {
        Track track = buildTrack(DEFAULT_URI);
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
    public void addTrackWithLeafTag() {
        Track track = buildTrack(DEFAULT_URI);
        Tag tag = buildTag("title", "foo");
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
    public void addTrackWithInnerTag() {
        Track track = buildTrack(DEFAULT_URI);
        Tag tag = buildTag("album", "foo");
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
    public void addTrackWithRootTag() {
        Track track = buildTrack(DEFAULT_URI);
        Tag tag = buildTag("artist", "foo");
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
    public void addTrackWithTwoTags() {
        Track track = buildTrack(DEFAULT_URI);
        Tag artistTag = buildTag("artist", "foo");
        Tag titleTag = buildTag("title", "foo");
        library.addTrack(track, metadata.buildTagMap(artistTag, titleTag));

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
    public void addTrackWithExtraTag() {
        Track track = buildTrack(DEFAULT_URI);
        Tag genreTag = buildTag("genre", "foo");
        Tag artistTag = buildTag("artist", "bar");
        Tag albumTag = buildTag("album", "baz");
        Tag titleTag = buildTag("title", "ham");
        library.addTrack(track,
                         metadata.buildTagMap(genreTag, artistTag, albumTag, titleTag));

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
        Tag artist1Tag = buildTag("artist", "foo");
        Tag album1Tag = buildTag("album", "bar");
        Tag title1Tag = buildTag("title", "baz");

        Track track1 = buildTrack(DEFAULT_URI);
        library.addTrack(track1, metadata.buildTagMap(artist1Tag, album1Tag, title1Tag));

        LibraryEntry artist1 = buildLibraryEntry(null, artists, artist1Tag, null);
        LibraryEntry album1 = buildLibraryEntry(artist1, albums, album1Tag, null);
        LibraryEntry title1 = buildLibraryEntry(album1, titles, title1Tag, track1);

        assertThat(getAllEntries()).containsExactly(artist1, album1, title1);

        Tag artist2Tag = buildTag("artist", "ham");
        Tag album2Tag = buildTag("album", "spam");
        Tag title2Tag = buildTag("title", "eggs");

        Track track2 = buildTrack(Uri.parse("file://sample.mp3"));
        library.addTrack(track2, metadata.buildTagMap(artist2Tag, album2Tag, title2Tag));

        LibraryEntry artist2 = buildLibraryEntry(null, artists, artist2Tag, null);
        LibraryEntry album2 = buildLibraryEntry(artist2, albums, album2Tag, null);
        LibraryEntry title2 = buildLibraryEntry(album2, titles, title2Tag, track2);

        assertThat(getAllEntries())
             .containsExactly(artist1, album1, title1, artist2, album2, title2);
    }

    /**
     * Given the library contains a track, when I insert a track with the same artist, album, and
     * title, then the track is inserted and a new title entry is created.
     */
    @Test
    public void insertIdenticalTrack() {
        Tag artistTag = buildTag("artist", "foo");
        Tag albumTag = buildTag("album", "bar");
        Tag titleTag = buildTag("title", "ham");

        Track track1 = buildTrack(DEFAULT_URI);
        Track track2 = buildTrack(Uri.parse("file://sample.mp3"));

        library.addTrack(track1, metadata.buildTagMap(artistTag, albumTag, titleTag));
        library.addTrack(track2, metadata.buildTagMap(artistTag, albumTag, titleTag));

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
    public void addTrackWithNewLeaf() {
        Tag artistTag = buildTag("artist", "foo");
        Tag albumTag = buildTag("album", "bar");
        Tag title1Tag = buildTag("title", "baz");
        Tag title2Tag = buildTag("title", "qux");

        Track track1 = buildTrack(DEFAULT_URI);
        Track track2 = buildTrack(Uri.parse("file://sample.mp3"));

        library.addTrack(track1, metadata.buildTagMap(artistTag, albumTag, title1Tag));
        library.addTrack(track2, metadata.buildTagMap(artistTag, albumTag, title2Tag));

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
    public void addTrackWithNewAlbum() {
        Tag artistTag = buildTag("artist", "foo");
        Tag album1Tag = buildTag("album", "bar");
        Tag album2Tag = buildTag("album", "ham");
        Tag titleTag = buildTag("title", "baz");

        Track track1 = buildTrack(DEFAULT_URI);
        Track track2 = buildTrack(Uri.parse("file://sample.mp3"));

        library.addTrack(track1, metadata.buildTagMap(artistTag, album1Tag, titleTag));
        library.addTrack(track2, metadata.buildTagMap(artistTag, album2Tag, titleTag));

        LibraryEntry artist = buildLibraryEntry(null, artists, artistTag, null);
        LibraryEntry album1 = buildLibraryEntry(artist, albums, album1Tag, null);
        LibraryEntry title1 = buildLibraryEntry(album1, titles, titleTag, track1);

        LibraryEntry album2 = buildLibraryEntry(artist, albums, album2Tag, null);
        LibraryEntry title2 = buildLibraryEntry(album2, titles, titleTag, track2);

        assertThat(getAllEntries()).containsExactly(artist, album1, album2, title1, title2);
    }

    private long trackId = 0L;
    private long tagId = 0L;
    private long entryId = 0L;

    private Track buildTrack(Uri uri) {
        Track track = new Track(uri);
        track.setId(++trackId);
        return track;
    }

    private Tag buildTag(String key, String value) {
        Tag tag = new Tag(key, value);
        tag.setId(++tagId);
        return tag;
    }

    private LibraryEntry buildLibraryEntry(
            LibraryEntry parent, LibraryNode node, Tag tag, Track track
    ) {
        LibraryEntry entry = new LibraryEntry(parent, node, tag, track);
        entry.setId(++entryId);
        return entry;
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
