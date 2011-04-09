package com.porunga.youroomclient;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class YouRoomCommandProxy {
	private SQLiteDatabase cacheDb = null;
	private YouRoomCommand youRoomCommand = null;
	
	public YouRoomCommandProxy(Activity activity) {
		cacheDb = ((AppHolder)activity.getApplication()).getCacheDb();
		youRoomCommand = new YouRoomCommand(new YouRoomUtil(activity.getApplication()).getOauthTokenFromLocal());
	}
	
	public String getEntry(String roomId, String entryId, String updatedTime) {
		String result = "";
		
		Cursor c = cacheDb.rawQuery("select result from entries where entryId = ? and roomId = ? and updatedTime = ? ;", new String[]{entryId, roomId, updatedTime});
		
		if (c.getCount() == 1) {
			Log.i("CACHE", String.format("Cache Hit  [%s]", entryId));
			c.moveToFirst();
			result =  c.getString(0);
		}
		else {
			Log.i("CACHE", String.format("Cache Miss [%s]", entryId));
			String res = youRoomCommand.getEntry(roomId, entryId);
			cacheDb.beginTransaction();
			try {
				cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[]{entryId, roomId});
				cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, result) values(?, ?, ?, ?) ;",new String[]{entryId, roomId, updatedTime, res});
				result =  res;
				cacheDb.setTransactionSuccessful();
			} finally {
				cacheDb.endTransaction();
			}
		}
		c.close();
		
		return result;
	}
}
