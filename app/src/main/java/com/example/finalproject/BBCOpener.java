package com.example.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;

public class BBCOpener extends SQLiteOpenHelper {

    /**
     * The name of the database
     */
    protected final static String DATABASE_NAME = "bbcDB";
    /**
     * The version number of the database
     */
    protected final static int VERSION_NUM = 1;
    /**
     * The ID column
     */
    public final static String COL_ID = "_id";
    /**
     * The table name
     */
    public final static String TABLE_NAME = "ARTICLES";
    /**
     * The TITLE column
     */
    public final static String COL_TITLE = "TITLE";
    /**
     * The DESCRIPTION column
     */
    public final static String COL_DESCRIPTION = "DESCRIPTION";
    /**
     * The DATE column
     */
    public final static String COL_DATE =  "DATE";
    /**
     * The URL column
     */
    public final static String COL_URL =  "URL";
    /**
     * The FAVORITE column
     */
    public final static String COL_FAV = "FAVORITE";

    public BBCOpener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    /**
     * Creates a new database with the specified table and columns
     *
     * @param db the database to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text NOT NULL UNIQUE,"
                + COL_DESCRIPTION + " text,"
                + COL_DATE + " text,"
                + COL_URL + " text,"
                + COL_FAV + " integer);");
    }

    /**
     * Upgrades the database to a new version
     *
     * @param db the database to be upgraded
     * @param oldVersion the old version number
     * @param newVersion the new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Downgrades the database to an older version
     *
     * @param db the database to be upgraded
     * @param oldVersion the old version number
     * @param newVersion the new version number
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Clear the database if necessary
     *
     * @param db
     * @param tableName
     */
    public void clearDatabase(SQLiteDatabase db, String tableName) {
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        db.execSQL(clearDBQuery);
    }
}
