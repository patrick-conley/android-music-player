package io.github.patrickconley.arbutus.metadata;

import android.support.annotation.NonNull;

import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.metadata.dao.TagDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackDao;
import io.github.patrickconley.arbutus.metadata.dao.TrackTagDao;
import io.github.patrickconley.arbutus.metadata.model.Tag;
import io.github.patrickconley.arbutus.metadata.model.Track;
import io.github.patrickconley.arbutus.metadata.model.TrackTag;

public class TrackManager {

    private final TrackDao trackDao;
    private final TagDao tagDao;
    private final TrackTagDao trackTagDao;

    public TrackManager(AppDatabase db) {
        trackDao = db.trackDao();
        tagDao = db.tagDao();
        trackTagDao = db.trackTagDao();
    }

    public void addTrack(@NonNull Track track, @NonNull Map<String, Tag> tags) {

        trackDao.insert(track);

        for (final Tag tag : tags.values()) {
            Tag savedTag = tagDao.getTag(tag);
            if (savedTag == null) {
                savedTag = tagDao.insert(tag);
            }

            trackTagDao.insert(new TrackTag(track, savedTag));
        }

    }
}
