package pconley.prototype.database.library;

import pconley.prototype.database.library.LibraryContract.TagEntry;
import pconley.prototype.database.library.LibraryContract.TrackEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibraryDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "library.db";
	private static final int DATABASE_VERSION = 1;

	private static final String SQL_CREATE_TRACKS = String
			.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT UNIQUE NOT NULL);",
					TrackEntry.TABLE_NAME, TrackEntry.COLUMN_NAME_ID,
					TrackEntry.COLUMN_NAME_URI);

	private static final String SQL_CREATE_TAGS = String
			.format("CREATE TABLE %s (%s INTEGER REFERENCES %s(%s), %s TEXT NOT NULL, %s TEXT NOT NULL);",
					TagEntry.TABLE_NAME, TrackEntry.COLUMN_NAME_ID,
					TrackEntry.TABLE_NAME, TrackEntry.COLUMN_NAME_ID,
					TagEntry.COLUMN_NAME_TAG, TagEntry.COLUMN_NAME_VAL);

	public LibraryDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_TRACKS);
		db.execSQL(SQL_CREATE_TAGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
