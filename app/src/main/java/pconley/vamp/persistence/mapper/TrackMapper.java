package pconley.vamp.persistence.mapper;

import android.database.Cursor;
import android.net.Uri;

import pconley.vamp.persistence.LibrarySchema.TagEntry;
import pconley.vamp.persistence.LibrarySchema.TrackEntry;
import pconley.vamp.persistence.LibrarySchema.TrackTagRelation;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

public class TrackMapper {

	private Cursor cursor;

	private int trackIdColumn;
	private int uriColumn;
	private int tagIdColumn;
	private int nameColumn;
	private int valueColumn;

	/**
	 * @param cursor
	 */
	public TrackMapper(Cursor cursor) {
		this.cursor = cursor;

		// First track: move to the first row
		if (cursor.isBeforeFirst()) {
			cursor.moveToFirst();
		}

		trackIdColumn = cursor
				.getColumnIndexOrThrow(TrackTagRelation.TRACK_ID);
		uriColumn = cursor.getColumnIndexOrThrow(TrackEntry.COLUMN_URI);
		tagIdColumn = cursor.getColumnIndexOrThrow(TrackTagRelation.TAG_ID);
		nameColumn = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_TAG);
		valueColumn = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_VAL);
	}

	/**
	 * Extract a single track from the cursor. The cursor must be positioned on
	 * the first row of a track; if the cursor contains rows for multiple
	 * tracks, then each track's rows must be contiguous.
	 * <p/>
	 * When done, the cursor will point at the first row of the next track, if
	 * available.
	 *
	 * @return Track beginning at the cursor's current row, or null if
	 * past-the-end.
	 */
	public Track mapNextTrack() {
		// Last track: do nothing
		if (cursor.isAfterLast()) {
			return null;
		}

		long id = cursor.getLong(trackIdColumn);
		Track.Builder builder
				= new Track.Builder(id, Uri.parse(cursor.getString(uriColumn)));

		if (cursor.isNull(nameColumn)) {
			cursor.moveToNext();
			return builder.build();
		}

		// Add each row's tags to the track.
		while (!cursor.isAfterLast() && cursor.getLong(trackIdColumn) == id) {
			builder.add(new Tag(cursor.getLong(tagIdColumn),
			                    cursor.getString(nameColumn),
			                    cursor.getString(valueColumn)));

			cursor.moveToNext();
		}

		return builder.build();
	}

}
