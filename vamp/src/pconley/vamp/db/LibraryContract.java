package pconley.vamp.db;

import android.provider.BaseColumns;

public class LibraryContract {

	/* Don't allow instantiation. */
	public LibraryContract() {
	}

	/**
	 * Listing of tracks in the library. Only columns guaranteed to be
	 * single-valued and unique should exist here.
	 */
	public static abstract class TrackEntry implements BaseColumns {

		public static final String TABLE_NAME = "Tracks";
		public static final String COLUMN_NAME_ID = _ID;
		public static final String COLUMN_NAME_URI = "trackUri";
	}

	/**
	 * Metadata of tracks in the library. Foreign key reference to TrackEntry;
	 * no primary key. A track may have 0 or more tags; each tag has one or more
	 * values. Each row should be unique ((tag,value) pairs need not be, as two
	 * tracks may well have the same tags).
	 */
	public static abstract class TagEntry implements BaseColumns {

		public static final String TABLE_NAME = "Tags";
		public static final String COLUMN_NAME_TAG = "name";
		public static final String COLUMN_NAME_VAL = "value";
	}

}
