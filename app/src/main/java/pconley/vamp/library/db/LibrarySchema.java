package pconley.vamp.library.db;

import android.provider.BaseColumns;

public class LibrarySchema {

	/* Don't allow instantiation. */
	private LibrarySchema() {
	}

	/**
	 * Listing of tracks in the library. Only columns guaranteed to be
	 * single-valued and unique should exist here.
	 */
	public static abstract class TrackEntry implements BaseColumns {

		public static final String NAME = "Tracks";
		public static final String COLUMN_ID = _ID;
		public static final String COLUMN_URI = "uri";

		public static final String SQL_CREATE = String
				.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT " +
						        "UNIQUE NOT NULL);",
				        NAME, COLUMN_ID, COLUMN_URI);

	}

	/**
	 * Bidirectional relation between tags and tracks.
	 */
	public static abstract class TrackTagRelation {

		public static final String NAME = "TrackHasTags";
		public static final String TRACK_ID = "track_id";
		public static final String TAG_ID = "tag_id";

		public static final String SQL_CREATE = String
				.format("CREATE TABLE %s (%s INTEGER REFERENCES %s(%s) NOT " +
						        "NULL, %s INTEGER REFERENCES %s(%s) NOT " +
						        "NULL, CONSTRAINT no_dup_tracktags UNIQUE " +
						        "(%s, %s) ON CONFLICT FAIL);",
				        NAME, TRACK_ID, TrackEntry.NAME, TrackEntry.COLUMN_ID,
				        TAG_ID, TagEntry.NAME, TagEntry.COLUMN_ID, TRACK_ID,
				        TAG_ID);

	}

	/**
	 * Metadata of tracks in the library. Each tag must be related to one or
	 * more tracks.
	 */
	public static abstract class TagEntry implements BaseColumns {

		public static final String NAME = "Tags";
		public static final String COLUMN_ID = _ID;
		public static final String COLUMN_TAG = "name";
		public static final String COLUMN_VAL = "value";

		public static final String SQL_CREATE = String
				.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT " +
						        "NOT NULL, %s TEXT NOT NULL, CONSTRAINT " +
						        "no_dup_tags UNIQUE (%s, %s) ON CONFLICT " +
						        "FAIL);",
				        NAME, COLUMN_ID, COLUMN_TAG, COLUMN_VAL, COLUMN_TAG,
				        COLUMN_VAL);

	}

}
