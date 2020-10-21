package com.example.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GuardianDatabaseOpener extends SQLiteOpenHelper {
    protected final static String DATABASE_NAME = "Guardian_DB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "Articles";
    public final static String COL_TITLE = "TITLE";
    public final static String COL_URL = "URL";
    public final static String COL_SECTION_NAME = "SECTION_NAME";
    public final static String COL_ID = "_id";

    public GuardianDatabaseOpener(Context ctx){
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    public void onCreate(SQLiteDatabase db){

        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_URL + " text,"
                + COL_SECTION_NAME + " text);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL ("DROP TABLE IF EXISTS " + TABLE_NAME);

        this.onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL ("DROP TABLE IF EXISTS " + TABLE_NAME);

        this.onCreate(db);
    }
}
