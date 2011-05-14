package com.porunga.youroomclient;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppHolder extends Application {
	private SQLiteDatabase cacheDb = null;
	private HashMap<String, Boolean> dirtyFlgList = new HashMap<String, Boolean>();
	
	public SQLiteDatabase getCacheDb() {
		return cacheDb;
	}
	
	public boolean isDirty(String roomId) {
		if (!dirtyFlgList.containsKey(roomId)) {
			dirtyFlgList.put(roomId, true);
		}
		return dirtyFlgList.get(roomId);
	}
	public void setDirty(String roomId, boolean dirtyFlg) {
		Log.i("CACHE", String.format("set DirtyFlg %s [%s]", dirtyFlg, roomId));
		if (dirtyFlgList.containsKey(roomId)) {
			dirtyFlgList.remove(roomId);
		}
		dirtyFlgList.put(roomId, dirtyFlg);
	}
	public void clearDirty() {
		Log.i("CACHE", "clear DirtyFlg");
		dirtyFlgList.clear();
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
				db.execSQL("create table rooms         (roomId text primary key, room blob not null); ");
				db.execSQL("create table entries       (entryId text primary key, roomId text not null, updatedTime text not null, entry blob not null); ");
				db.execSQL("create table timelines     (entryId text primary key, roomId text not null, page text not null, entry blob not null); ");
				db.execSQL("create table memberImages (participationId text primary key, image blob not null); ");
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
