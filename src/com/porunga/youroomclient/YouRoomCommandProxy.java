package com.porunga.youroomclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import org.json.JSONException;
import org.json.JSONObject;

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
	
	public JSONObject getEntry(String roomId, String entryId, String updatedTime) {
		JSONObject json = null;
		
		Cursor c = cacheDb.rawQuery("select json from entries where entryId = ? and roomId = ? and updatedTime = ? ;", new String[]{entryId, roomId, updatedTime});
		
		if (c.getCount() == 1) {
			Log.i("CACHE", String.format("Cache Hit  [%s]", entryId));
			c.moveToFirst();
			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				json = (JSONObject)ois.readObject();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.i("CACHE", String.format("Cache Miss [%s]", entryId));
			try {
				json = (new JSONObject(youRoomCommand.getEntry(roomId, entryId))).getJSONObject("entry");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			cacheDb.beginTransaction();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				(new ObjectOutputStream(baos)).writeObject(json);
				cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[]{entryId, roomId});
				cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, json) values(?, ?, ?, ?) ;",new Object[]{entryId, roomId, updatedTime, baos.toByteArray()});
				cacheDb.setTransactionSuccessful();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cacheDb.endTransaction();
			}
		}
		c.close();
		
		return json;
	}
}
