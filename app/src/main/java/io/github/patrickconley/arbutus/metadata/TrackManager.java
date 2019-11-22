package io.github.patrickconley.arbutus.metadata;

import androidx.annotation.NonNull;

import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TagInTrackDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TagInTrack;

public class TrackManager {

    private final TrackDao trackDao;
    private final TagDao tagDao;
    private final TagInTrackDao tagInTrackDao;

    public TrackManager(AppDatabase db) {
        trackDao = db.trackDao();
        tagDao = db.tagDao();
        tagInTrackDao = db.tagInTrackDao();
    }

    public void addTrack(@NonNull Track track, @NonNull Map<String, Tag> tags) {

        trackDao.insert(track);

        for (final Tag tag : tags.values()) {
            Tag savedTag = tagDao.getTag(tag);
            if (savedTag == null) {
                savedTag = tagDao.insert(tag);
            }

            tagInTrackDao.insert(new TagInTrack(track, savedTag));
        }

    }
}
