package com.porunga.youroomclient;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppHolder extends Application {
	private SQLiteDatabase cacheDb = null;
	
	public SQLiteDatabase getCacheDb() {
		return cacheDb;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("CACHE", "Open Database");
		DBHelper helper = new DBHelper(this);
		cacheDb = helper.getWritableDatabase();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i("CACHE", "Close Database");
		cacheDb.close();
	}

	private class DBHelper extends SQLiteOpenHelper {
		public DBHelper(Context context) {
			super(context, "porungadb", null, 1);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("CACHE", "Create Database");
			db.beginTransaction();
			try {
				db.execSQL("create table entries (entryId text primary key,roomId text not null, updatedTime text not null, json blob not null);");
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
