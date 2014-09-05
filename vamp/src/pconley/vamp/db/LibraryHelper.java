package pconley.vamp.db;

import pconley.vamp.db.LibraryContract.TagEntry;
import pconley.vamp.db.LibraryContract.TrackEntry;
import pconley.vamp.db.LibraryContract.TrackTagRelation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibraryHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "library.db";
	private static final int DATABASE_VERSION = 1;

	public LibraryHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TrackEntry.SQL_CREATE);
		db.execSQL(TagEntry.SQL_CREATE);
		db.execSQL(TrackTagRelation.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);

		db.setForeignKeyConstraintsEnabled(true);
	}

}
