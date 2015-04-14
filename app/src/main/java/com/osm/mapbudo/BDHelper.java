package com.osm.mapbudo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BDHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE_POIS = "CREATE TABLE poi (id INTEGER,changeset INTEGER,action TEXT,osmId INTEGER,lat FLOAT,lon FLOAT,uploaded INTEGER,status TEXT,version INTEGER, UNIQUE(id)ON CONFLICT REPLACE,UNIQUE (osmId) ON CONFLICT REPLACE ,UNIQUE (id,osmId) ON CONFLICT REPLACE )";
    private static final String DATABASE_CREATE_VALUES = "CREATE TABLE value (id INTEGER,osmId INTEGER,key TEXT, value TEXT)";
    private static final String DATABASE_CREATE_CHANGESETS = "CREATE TABLE changeset (changesetId INTEGER,status TEXT,UNIQUE(changesetId) ON CONFLICT REPLACE)";
    private static final String DATABASE_CREATE_INDEX_POIS_ID="CREATE INDEX IF NOT EXISTS index_id ON poi (id)";
    public BDHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_POIS);
        database.execSQL(DATABASE_CREATE_CHANGESETS);
        database.execSQL(DATABASE_CREATE_VALUES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,int newVersion) {

    }
}