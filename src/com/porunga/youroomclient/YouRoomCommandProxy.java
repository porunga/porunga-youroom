package com.porunga.youroomclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class YouRoomCommandProxy {
	private static final String POST_OK = "201";
	
	private AppHolder appHolder = null;
	private SQLiteDatabase cacheDb = null;
	private YouRoomCommand youRoomCommand = null;
	
	public YouRoomCommandProxy(Activity activity) {
		appHolder = (AppHolder)activity.getApplication();
		cacheDb = appHolder.getCacheDb();
		youRoomCommand = new YouRoomCommand(new YouRoomUtil(appHolder).getOauthTokenFromLocal());
	}
	
	public ArrayList<YouRoomEntry> getRoomEntryList(String roomId, Map<String, String> parameterMap) {
		ArrayList<YouRoomEntry> entryList = new ArrayList<YouRoomEntry>();
		if (appHolder.isDirty(roomId)) {
			Log.i("CACHE", String.format("RoomTimeLine is Dirty [%s]", roomId));
			cacheDb.beginTransaction();
			try {
				cacheDb.execSQL("delete from timelines where roomId = ? ;", new Object[]{roomId});
				JSONArray jsonArray = new JSONArray(youRoomCommand.getRoomTimeLine(roomId, parameterMap));
				for (int i = 0; i < jsonArray.length(); i++) {
					YouRoomEntry entry = buildEntryFromJson(jsonArray.getJSONObject(i).getJSONObject("entry"));
					entry.setDescendantsCount(-1);
					entryList.add(entry);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					(new ObjectOutputStream(baos)).writeObject(entry);
					
					cacheDb.execSQL("insert into timelines(entryId, roomId, page, entry) values(?, ?, ?, ?) ;",new Object[]{entry.getId(), roomId, parameterMap.get("page"), baos.toByteArray()});
				}
				cacheDb.setTransactionSuccessful();
				appHolder.setDirty(roomId, false);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cacheDb.endTransaction();
			}
		}
		else {
			Cursor c = cacheDb.rawQuery("select entry from timelines where roomId = ? and page = ? ;", new String[]{roomId, parameterMap.get("page")});
			cacheDb.beginTransaction();
			try {
				if (c.getCount() == 0) {
					Log.i("CACHE", String.format("RoomTimeLine Cache(page:%s) Miss [%s]", parameterMap.get("page"), roomId));
					JSONArray jsonArray = new JSONArray(youRoomCommand.getRoomTimeLine(roomId, parameterMap));
					for (int i = 0; i < jsonArray.length(); i++) {
						YouRoomEntry entry = buildEntryFromJson(jsonArray.getJSONObject(i).getJSONObject("entry"));
						entry.setDescendantsCount(-1);
						entryList.add(entry);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						(new ObjectOutputStream(baos)).writeObject(entry);
						
						cacheDb.execSQL("insert into timelines(entryId, roomId, page, entry) values(?, ?, ?, ?) ;",new Object[]{entry.getId(), roomId, parameterMap.get("page"), baos.toByteArray()});
					}
					cacheDb.setTransactionSuccessful();
				}
				else {
					Log.i("CACHE", String.format("RoomTimeLine Cache(page:%s) Hit  [%s]", parameterMap.get("page"), roomId));
					if(c.moveToFirst()){
						do {
							ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
							entryList.add((YouRoomEntry)ois.readObject());
						} while(c.moveToNext());
					}
				}
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				cacheDb.endTransaction();
				c.close();
			}
		}
		return entryList;
	}
	
	public YouRoomEntry getEntry(String roomId, String entryId, String updatedTime) {
		YouRoomEntry entry = null;
		
		Cursor c = cacheDb.rawQuery("select entry from entries where entryId = ? and roomId = ? and updatedTime = ? ;", new String[]{entryId, roomId, updatedTime});
		
		if (c.getCount() == 1) {
			Log.i("CACHE", String.format("Entry Cache Hit  [%s]", entryId));
			c.moveToFirst();
			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				entry = (YouRoomEntry)ois.readObject();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				c.close();
			}
		}
		else {
			Log.i("CACHE", String.format("Entry Cache Miss [%s]", entryId));
			try {
				JSONObject json = (new JSONObject(youRoomCommand.getEntry(roomId, entryId))).getJSONObject("entry");
				entry = buildEntryFromJson(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			cacheDb.beginTransaction();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				(new ObjectOutputStream(baos)).writeObject(entry);
				cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[]{entryId, roomId});
				cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, entry) values(?, ?, ?, ?) ;",new Object[]{entryId, roomId, updatedTime, baos.toByteArray()});
				cacheDb.setTransactionSuccessful();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cacheDb.endTransaction();
				c.close();
			}
		}
		
		return entry;
	}
	
	public YouRoomEntry getEntry(String roomId, String entryId) {
		YouRoomEntry entry = null;
		
		Cursor c = cacheDb.rawQuery("select entry from entries where entryId = ? and roomId = ? ;", new String[]{entryId, roomId});
		
		if (c.getCount() == 1) {
			Log.i("CACHE", String.format("Entry Cache Hit  [%s]", entryId));
			c.moveToFirst();
			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				entry = (YouRoomEntry)ois.readObject();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				c.close();
			}
		}
		else {
			c.close();
			throw new RuntimeException("cache is not found");
		}
		return entry;
	}
	
	public String createEntry(String roomId, String parentId, String entryContent, String rootId) {
		String statusCode = youRoomCommand.createEntry(roomId, parentId, entryContent);
		if (POST_OK.equals(statusCode)) {
			appHolder.setDirty(roomId, true);
			if (rootId != null) {
				YouRoomEntry entry = null;
				String updatedTime = null;
				try {
					JSONObject json = (new JSONObject(youRoomCommand.getEntry(roomId, rootId))).getJSONObject("entry");
					entry = buildEntryFromJson(json);
					updatedTime = entry.getUpdatedTime();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				cacheDb.beginTransaction();
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					(new ObjectOutputStream(baos)).writeObject(entry);
					cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[]{rootId, roomId});
					cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, entry) values(?, ?, ?, ?) ;",new Object[]{rootId, roomId, updatedTime, baos.toByteArray()});
					cacheDb.setTransactionSuccessful();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					cacheDb.endTransaction();
				}
			}
		}
		return statusCode;
	}
	
	private YouRoomEntry buildEntryFromJson(JSONObject json) {
		YouRoomEntry entry = new YouRoomEntry();
		try {
			entry.setId(json.getInt("id"));
			entry.setParticipationName(json.getJSONObject("participation").getString("name"));
			entry.setCreatedTime(json.getString("created_at"));
			entry.setUpdatedTime(json.getString("updated_at"));
			entry.setContent(json.getString("content"));
			entry.setDescendantsCount(json.optInt("descendants_count"));
			if (json.has("children")) {
				JSONArray cArray = json.getJSONArray("children");
				ArrayList<YouRoomEntry> children = new ArrayList<YouRoomEntry>(cArray.length());
				for (int i = 0; i < cArray.length(); i++) {
					JSONObject child = cArray.getJSONObject(i);
					children.add(buildEntryFromJson(child));
				}
				entry.setChildren(children);
			}
			else {
				entry.setChildren(new ArrayList<YouRoomEntry>());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return entry;
	}
}
