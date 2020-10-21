package com.example.finalproject.nasa_img_activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Noah Cheesman
 * @version "%I%, %G%"
 * @see SQLiteOpenHelper
 */
public class NasaImgDBOpener extends SQLiteOpenHelper {
	final static String TABLE_NAME = "IMAGES";
	final static String COL_IMAGE = "IMAGE";
	final static String COL_DESCRIPTION = "DESCRIPTION";
	final static String COL_TITLE = "TITLE";
	final static String COL_DATE = "DATE";
	final static String COL_ID = "_id";
	private final static String DATABASE_NAME = "NasaImgDB";
	private final static int VERSION_NUM = 1;

	/**
	 * Constructor calls super constructor
	 *
	 * @param ctx context to provide to super constructor.
	 * @see SQLiteOpenHelper#SQLiteOpenHelper(Context, String, SQLiteDatabase.CursorFactory, int)
	 */
	NasaImgDBOpener(Context ctx) {
		super(ctx, DATABASE_NAME, null, VERSION_NUM);
	}

	/**
	 * On creation of Object table is added to the database
	 *
	 * @param db SQLiteDatabase that will contain table
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COL_TITLE + " TEXT,"
				+ COL_DATE + " TEXT,"
				+ COL_IMAGE + " TEXT,"
				+ COL_DESCRIPTION + " TEXT);");
	}

	/**
	 * Removes old table and creates a new one, increasing the version number in the process
	 *
	 * @param db         Database that holds the table
	 * @param oldVersion Old version number
	 * @param newVersion New version number
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	/**
	 * Removes old table and creates a new one, decreasing the version number in the process
	 *
	 * @param db         Database that holds the table
	 * @param oldVersion Old version number
	 * @param newVersion New version number
	 */
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
