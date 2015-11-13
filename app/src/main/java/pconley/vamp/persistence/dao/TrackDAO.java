package pconley.vamp.persistence.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.persistence.LibraryOpenHelper;
import pconley.vamp.persistence.LibrarySchema.TagEntry;
import pconley.vamp.persistence.LibrarySchema.TrackEntry;
import pconley.vamp.persistence.LibrarySchema.TrackTagRelation;
import pconley.vamp.persistence.mapper.TrackMapper;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.util.TrackUtil;

/**
 * Methods to read and write track data from the database.
 *
 * @author pconley
 */
public class TrackDAO {

	// SELECT * FROM (SELECT * FROM (SELECT _id AS trackId, uri FROM Tracks)
	//   LEFT OUTER JOIN TrackHasTags USING (trackId))
	// LEFT OUTER JOIN Tags ON _id = tagId ORDER BY trackId
	private static final String GET_TRACKS_QUERY = MessageFormat
			.format("SELECT * FROM (SELECT * FROM "
			        + "(SELECT {4} AS {1}, {5} FROM {3})"
			        + " LEFT OUTER JOIN {0} USING ({1}))"
			        + " LEFT OUTER JOIN {6} ON {7} = {2} ORDER BY {1}",
			        TrackTagRelation.NAME, TrackTagRelation.TRACK_ID,
			        TrackTagRelation.TAG_ID, TrackEntry.NAME,
			        TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI, TagEntry.NAME,
			        TagEntry.COLUMN_ID);

	private LibraryOpenHelper libraryOpenHelper;

	public TrackDAO(LibraryOpenHelper libraryOpenHelper) {
		this.libraryOpenHelper = libraryOpenHelper;
	}

	/**
	 * Convenience method. Get all tracks.
	 */
	public List<Track> getAllTracks() {
		List<Track> tracks = new LinkedList<Track>();
		Track track;

		Cursor results = libraryOpenHelper.getReadableDatabase().rawQuery(
				GET_TRACKS_QUERY, null);
		TrackMapper mapper = new TrackMapper(results);
		while ((track = mapper.mapNextTrack()) != null) {
			tracks.add(track);
		}

		results.close();

		return tracks;
	}

	/**
	 * Find the tracks that belong in the given collection.
	 * <p/>
	 *
	 * @param collection
	 * @return The tracks matching the set of tags in the collection.
	 */
	public List<Track> getTracksWithCollection(MusicCollection collection) {
		int nTags = collection.getHistory().size();

		// SELECT trackId, trackUri, tagId, name, value FROM
		//   buildMatchingTracksQuery
		//   INNER JOIN Tags ON tagId = Tags._id
		//   INNER JOIN Tracks ON trackId = Tracks._id;
		String query = MessageFormat
				.format("SELECT {1}, {3}, {5}, {7}, {8} FROM {9} " +
				        "INNER JOIN {4} ON {5} = {4}.{6} " +
				        "INNER JOIN {0} ON {1} = {0}.{2}",
				        TrackEntry.NAME, TrackTagRelation.TRACK_ID,
				        TrackEntry.COLUMN_ID, TrackEntry.COLUMN_URI,
				        TagEntry.NAME, TrackTagRelation.TAG_ID,
				        TagEntry.COLUMN_ID, TagEntry.COLUMN_TAG,
				        TagEntry.COLUMN_VAL,
				        TrackUtil.buildMatchingTracksQuery(nTags));

		// Use the tag IDs to make the selection
		String[] selectionArgs = new String[nTags];
		for (int i = 0; i < nTags; i++) {
			selectionArgs[i] =
					String.valueOf(collection.getHistory().get(i).getId());
		}

		List<Track> tracks = new LinkedList<Track>();
		Track track;

		// Get the tracks
		Cursor results = libraryOpenHelper.getReadableDatabase()
		                                  .rawQuery(query, selectionArgs);
		TrackMapper mapper = new TrackMapper(results);
		while ((track = mapper.mapNextTrack()) != null) {
			tracks.add(track);
		}

		results.close();

		return tracks;
	}

	/**
	 * Insert a track with its tags. This method is only to be used when
	 * populating an empty database. A transaction is used.
	 *
	 * @param track
	 * @throws SQLException
	 * 		If the track is already in the database.
	 * @throws NullPointerException
	 * 		If any input is null.
	 */
	public void insertTrack(Track track) throws SQLException,
			NullPointerException {
		SQLiteDatabase library = libraryOpenHelper.getWritableDatabase();
		TagDAO tagDAO = new TagDAO(libraryOpenHelper);

		library.beginTransaction();
		try {
			// Insert the track
			ContentValues values = new ContentValues();
			values.put(TrackEntry.COLUMN_URI, track.getUri().toString());

			long trackId = library.insertOrThrow(TrackEntry.NAME, null, values);

			// Insert the track's tags
			for (String name : track.getTagNames()) {
				List<Tag> tags = track.getTags(name);
				if (tags != null) {
					for (Tag tag : tags) {
						tagDAO.insertTag(trackId, tag);
					}
				}
			}
			library.setTransactionSuccessful();
		} finally {
			library.endTransaction();
		}
	}

	/**
	 * Remove all tracks, tags, and relations
	 */
	public void wipeDatabase() {
		SQLiteDatabase library = libraryOpenHelper.getWritableDatabase();
		library.execSQL("DELETE FROM " + TrackTagRelation.NAME);
		library.execSQL("DELETE FROM " + TrackEntry.NAME);
		library.execSQL("DELETE FROM " + TagEntry.NAME);
	}

}
