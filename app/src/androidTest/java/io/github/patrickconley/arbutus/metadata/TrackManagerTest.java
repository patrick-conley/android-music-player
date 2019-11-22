package io.github.patrickconley.arbutus.metadata;

import androidx.room.Room;
import android.content.Context;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import io.github.patrickconley.arbutus.MetadataTestUtil;
import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TagInTrackDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TagInTrack;

import static com.google.common.truth.Truth.assertThat;

/**
 * Feature: populate the database with new tracks.
 */
@RunWith(AndroidJUnit4.class)
@SuppressWarnings("deprecation")
public class TrackManagerTest {

    private static final Uri DEFAULT_URI = Uri.parse("file://sample.ogg");
    private static final Uri ALTERNATE_URI = Uri.parse("file://sample.mp3");

    private Context context = InstrumentationRegistry.getTargetContext();
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

        assertThat(trackDao.getAll()).containsExactly(track);
        assertThat(tagDao.getAll()).isEmpty();
        assertThat(tagInTrackDao.getAll()).isEmpty();
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

        assertThat(trackDao.getAll()).containsExactly(track);
        assertThat(tagDao.getAll()).containsExactly(foo, bar);
        assertThat(tagInTrackDao.getAll())
                .containsExactly(new TagInTrack(track, foo), new TagInTrack(track, bar));
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

        assertThat(trackDao.getAll()).containsExactly(track1, track2);
        assertThat(tagDao.getAll()).isEmpty();
        assertThat(tagInTrackDao.getAll()).isEmpty();
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

        assertThat(trackDao.getAll()).containsExactly(track1, track2);
        assertThat(tagDao.getAll()).containsExactly(foo, bar);
        assertThat(tagInTrackDao.getAll())
                .containsExactly(new TagInTrack(track1, foo), new TagInTrack(track1, bar));
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

        assertThat(trackDao.getAll()).containsExactly(track1, track2);
        assertThat(tagDao.getAll()).containsExactly(foo, bar);
        assertThat(tagInTrackDao.getAll())
                .containsExactly(new TagInTrack(track2, foo), new TagInTrack(track2, bar));
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

        assertThat(trackDao.getAll()).containsExactly(track1, track2);
        assertThat(tagDao.getAll()).containsExactly(foo, bar, baz, qux);
        assertThat(tagInTrackDao.getAll())
                .containsExactly(new TagInTrack(track1, foo), new TagInTrack(track1, bar),
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

        Tag foo = new Tag("foo", "foo");
        Tag bar = new Tag("bar", "bar");

        trackManager.addTrack(track1, metadata.buildTagMap(foo, bar));
        trackManager.addTrack(track2,
                              metadata.buildTagMap(new Tag("foo", "foo"), new Tag("bar", "bar")));

        assertThat(trackDao.getAll()).containsExactly(track1, track2);
        assertThat(tagDao.getAll()).containsExactly(foo, bar);
        assertThat(tagInTrackDao.getAll())
                .containsExactly(new TagInTrack(track1, foo), new TagInTrack(track1, bar),
                                 new TagInTrack(track2, foo), new TagInTrack(track2, bar));
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
        Tag bar = new Tag("bar", "bar");
        Tag baz = new Tag("foo", "baz");

        trackManager.addTrack(track1, metadata.buildTagMap(foo, bar));
        trackManager.addTrack(track2, metadata.buildTagMap(baz, new Tag("bar", "bar")));

        assertThat(trackDao.getAll()).containsExactly(track1, track2);
        assertThat(tagDao.getAll()).containsExactly(foo, bar, baz);
        assertThat(tagInTrackDao.getAll())
                .containsExactly(new TagInTrack(track1, foo), new TagInTrack(track1, bar),
                                 new TagInTrack(track2, baz), new TagInTrack(track2, bar));
    }

}
