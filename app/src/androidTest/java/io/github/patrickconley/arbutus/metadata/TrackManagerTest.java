package io.github.patrickconley.arbutus.metadata;

import android.content.Context;
import android.net.Uri;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import io.github.patrickconley.arbutus.MetadataTestUtil;
import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TagInTrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.TagInTrack;
import io.github.patrickconley.arbutus.metadata.model.Track;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Feature: populate the database with new tracks.
 */
@RunWith(AndroidJUnit4.class)
@SuppressWarnings("deprecation")
public class TrackManagerTest {

    private static final Uri DEFAULT_URI = Uri.parse("file://sample.ogg");
    private static final Uri ALTERNATE_URI = Uri.parse("file://sample.mp3");

    private Context context = ApplicationProvider.getApplicationContext();
    private AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();

    private TrackDao trackDao = db.trackDao();
    private TagDao tagDao = db.tagDao();
    private TagInTrackDao tagInTrackDao = db.tagInTrackDao();
    private TrackManager trackManager = new TrackManager(db);

    private MetadataTestUtil metadata = new MetadataTestUtil();

    @After
    public void after() {
        db.close();
    }

    /**
     * Given an empty library, when I insert a track with no tags, then the database contains only
     * the track.
     */
    @Test
    public void addTrackWithoutTags() {
        Track track = new Track(DEFAULT_URI);

        trackManager.addTrack(track, Collections.<String, Tag>emptyMap());

        assertTrack(track);
        assertTag();
        assertTagInTrack();
    }

    /**
     * Given an empty library, when I insert a track with tags, then the database contains the
     * track, tags, and relations.
     */
    @Test
    public void addTrackWithTags() {
        Track track = new Track(DEFAULT_URI);
        Tag foo = new Tag("foo", "ham");
        Tag bar = new Tag("bar", "spam");

        trackManager.addTrack(track, metadata.buildTagMap(foo, bar));

        assertTrack(track);
        assertTag(foo, bar);
        assertTagInTrack(new TagInTrack(track, foo), new TagInTrack(track, bar));
    }

    /**
     * Given a library with a track with no tags, when I insert a new track with no tags, then the
     * library contains only the two tracks.
     */
    @Test
    public void addTwoTracksWithoutTags() {
        Track track1 = new Track(DEFAULT_URI);
        Track track2 = new Track(ALTERNATE_URI);

        trackManager.addTrack(track1, Collections.<String, Tag>emptyMap());
        trackManager.addTrack(track2, Collections.<String, Tag>emptyMap());

        assertTrack(track1, track2);
        assertTag();
        assertTagInTrack();
    }

    /**
     * Given a library with a track with tags, when I insert a new track with no tags, then the
     * library contains the original track, tags, and relations, and the new track.
     */
    @Test
    public void addTrackWithTagsThenTrackWithoutTags() {
        Track track1 = new Track(DEFAULT_URI);
        Track track2 = new Track(ALTERNATE_URI);

        Tag foo = new Tag("foo", "ham");
        Tag bar = new Tag("bar", "spam");

        trackManager.addTrack(track1, metadata.buildTagMap(foo, bar));
        trackManager.addTrack(track2, Collections.<String, Tag>emptyMap());

        assertTrack(track1, track2);
        assertTag(foo, bar);
        assertTagInTrack(new TagInTrack(track1, foo), new TagInTrack(track1, bar));
    }

    /**
     * Given a library with a track with no tags, when I insert a new track with tags, then the
     * library contains the original track, and the new track, tags, and relations.
     */
    @Test
    public void addTrackWithoutTagsThenTrackWithTags() {
        Track track1 = new Track(DEFAULT_URI);
        Track track2 = new Track(ALTERNATE_URI);

        Tag foo = new Tag("foo", "ham");
        Tag bar = new Tag("bar", "spam");

        trackManager.addTrack(track1, Collections.<String, Tag>emptyMap());
        trackManager.addTrack(track2, metadata.buildTagMap(foo, bar));

        assertTrack(track1, track2);
        assertTag(foo, bar);
        assertTagInTrack(new TagInTrack(track2, foo), new TagInTrack(track2, bar));
    }

    /**
     * Given a library with a track with tags, when I insert a new track with distinct tags, then
     * the library contains the original and new track, tags, and relations.
     */
    @Test
    public void addTracksWithDistinctTags() {
        Track track1 = new Track(DEFAULT_URI);
        Track track2 = new Track(ALTERNATE_URI);

        Tag foo = new Tag("foo", "foo");
        Tag bar = new Tag("bar", "bar");
        Tag baz = new Tag("foo", "baz");
        Tag qux = new Tag("bar", "qux");

        trackManager.addTrack(track1, metadata.buildTagMap(foo, bar));
        trackManager.addTrack(track2, metadata.buildTagMap(baz, qux));

        assertTrack(track1, track2);
        assertTag(foo, bar, baz, qux);
        assertTagInTrack(new TagInTrack(track1, foo), new TagInTrack(track1, bar),
                         new TagInTrack(track2, baz), new TagInTrack(track2, qux));
    }

    /**
     * Given a library with a track with tags, when I insert a new track with identical tags, then
     * the library contains the original track, tags, and relations, and the new track and
     * relations.
     */
    @Test
    public void addTracksWithIdenticalTags() {
        Track track1 = new Track(DEFAULT_URI);
        Track track2 = new Track(ALTERNATE_URI);

        Tag foo1 = new Tag("foo", "foo");
        Tag bar1 = new Tag("bar", "bar");
        Tag foo2 = new Tag("foo", "foo");
        Tag bar2 = new Tag("bar", "bar");

        trackManager.addTrack(track1, metadata.buildTagMap(foo1, bar1));
        trackManager.addTrack(track2, metadata.buildTagMap(foo2, bar2));

        assertTrack(track1, track2);
        assertTag(foo1, bar1);
        assertTagInTrack(new TagInTrack(track1, foo1), new TagInTrack(track1, bar1),
                         new TagInTrack(track2, foo1), new TagInTrack(track2, bar1));

        // Verify that tags that aren't inserted are nevertheless updated with their IDs
        assertEquals(foo1.getId(), foo2.getId());
        assertEquals(bar1.getId(), bar2.getId());
    }

    /**
     * Given a library with a track with tags, when I insert a new track with overlapping tags, then
     * the library contains the union of the sets of tags, and the original and new track and
     * relations.
     */
    @Test
    public void addTracksWithOverlappingTags() {
        Track track1 = new Track(DEFAULT_URI);
        Track track2 = new Track(ALTERNATE_URI);

        Tag foo = new Tag("foo", "foo");
        Tag bar1 = new Tag("bar", "bar");
        Tag baz = new Tag("foo", "baz");
        Tag bar2 = new Tag("bar", "bar");

        trackManager.addTrack(track1, metadata.buildTagMap(foo, bar1));
        trackManager.addTrack(track2, metadata.buildTagMap(baz, bar2));

        assertTrack(track1, track2);
        assertTag(foo, bar1, baz);
        assertTagInTrack(new TagInTrack(track1, foo), new TagInTrack(track1, bar1),
                         new TagInTrack(track2, baz), new TagInTrack(track2, bar1));

        // Verify that tags that aren't inserted are nevertheless updated with their IDs
        assertEquals(bar1.getId(), bar2.getId());
    }

    private void assertTrack(Track... expected) {
        for (Track track : expected) {
            assertThat(track.getId()).isGreaterThan(0);
        }

        assertThat(trackDao.getAll()).containsExactly((Object[]) expected);
    }

    private void assertTag(Tag... expected) {
        for (Tag tag : expected) {
            assertThat(tag.getId()).isGreaterThan(0);
        }

        assertThat(tagDao.getAll()).containsExactly((Object[]) expected);
    }

    private void assertTagInTrack(TagInTrack... expected) {
        assertThat(tagInTrackDao.getAll()).containsExactly((Object[]) expected);
    }

}
