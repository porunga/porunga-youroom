package com.porunga.youroomclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class YouRoomCommandProxy {
	private static final String POST_OK = "201";

	private AppHolder appHolder = null;
	private SQLiteDatabase cacheDb = null;
	private YouRoomCommand youRoomCommand = null;
	private YouRoomUtil youRoomUtil = null;

	public YouRoomCommandProxy(Activity activity) {
		appHolder = (AppHolder) activity.getApplication();
		init(activity);
	}

	public YouRoomCommandProxy(Service service) {
		appHolder = (AppHolder) service.getApplication();
		init(service);
	}

	private void init(Context context) {
		cacheDb = appHolder.getCacheDb();
		youRoomCommand = new YouRoomCommand(new YouRoomUtil(appHolder).getOauthTokenFromLocal());
		youRoomUtil = new YouRoomUtil(context);
	}

	private Bitmap getRoomImage(String roomId) {
		Bitmap roomImage = null;
		YouRoomGroup group = null;
		Cursor c = null;
		try {
			c = cacheDb.rawQuery("select room from rooms where roomId = ?;", new String[] { roomId });
			if (c.moveToFirst()) {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				group = (YouRoomGroup) ois.readObject();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} finally {
			if (c != null) {
				c.close();
			}
		}

		if (group != null) {
			byte[] data = group.getRoomImage();
			roomImage = BitmapFactory.decodeByteArray(data, 0, data.length);
		} else {
			try {
				roomImage = youRoomCommand.getImage("https://www.youroom.in/r/" + roomId + "/picture");
			} catch (YouRoomServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return roomImage;
	}

	public Bitmap getMemberImage(String roomId, String participationId, boolean[] errFlg) {
		Bitmap memberImage = null;
		byte[] image = null;
		Cursor c = null;
		try {
			c = cacheDb.rawQuery("select image from memberImages where participationId = ?;", new String[] { participationId });
			if (c.moveToFirst()) {
				image = c.getBlob(0);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} finally {
			if (c != null) {
				c.close();
			}
		}

		if (image != null) {
			memberImage = BitmapFactory.decodeByteArray(image, 0, image.length);
		} else {
			try {
				memberImage = youRoomCommand.getImage("https://www.youroom.in/r/" + roomId + "/participations/" + participationId + "/picture");
			} catch (YouRoomServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.w("NW", "Network Error occured");
				errFlg[0] = true;
				throw new RuntimeException(e);
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			memberImage.compress(Bitmap.CompressFormat.PNG, 50 ,bout);
			image = bout.toByteArray();
			cacheDb.beginTransaction();
			try {
				cacheDb.execSQL("insert into memberImages(participationId, image) values(?, ?) ;", new Object[] { participationId, image });
				cacheDb.setTransactionSuccessful();
			} finally {
				cacheDb.endTransaction();
			}
		}
		return memberImage;
	}

	public ArrayList<YouRoomGroup> getMyGroupList(boolean[] errFlg) {
		ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();
		try {
			String myGroups = youRoomCommand.getMyGroup();
			cacheDb.beginTransaction();
			try {
				// cacheDb.execSQL("delete from rooms ;");
				JSONArray jsons = new JSONArray(myGroups);
				for (int i = 0; i < jsons.length(); i++) {
					YouRoomGroup group = new YouRoomGroup();
					JSONObject jObject = jsons.getJSONObject(i);
					JSONObject groupObject = jObject.getJSONObject("group");

					int id = groupObject.getInt("id");
					String name = groupObject.getString("name");

					String createdTime = groupObject.getString("created_at");
					String updatedTime = groupObject.getString("updated_at");

					group.setId(id);
					group.setName(name);
					group.setUpdatedTime(updatedTime);
					group.setCreatedTime(createdTime);

					String roomId = String.valueOf(id);
					Bitmap roomImageBitmap = this.getRoomImage(roomId);
					group.setRoomImage(roomImageBitmap);

					String lastAccessTime = youRoomUtil.getRoomAccessTime(roomId);
					String time;
					if (lastAccessTime == null) {
						time = youRoomUtil.getAccessTime();
						if (time == null) { // ここに入ることはないはず。
							time = YouRoomUtil.getRFC3339FormattedTime();
						}
						youRoomUtil.storeRoomAccessTime(roomId, time);
					}

					UserSession session = UserSession.getInstance();
					roomId = String.valueOf(id);
					lastAccessTime = youRoomUtil.getRoomAccessTime(roomId);
					session.setRoomAccessTime(roomId, lastAccessTime);

					dataList.add(group);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					(new ObjectOutputStream(baos)).writeObject(group);
					cacheDb.execSQL("delete from rooms where roomId = ?;", new String[] { roomId });
					cacheDb.execSQL("insert into rooms(roomId, room) values(?, ?) ;", new Object[] { roomId, baos.toByteArray() });
				}
				cacheDb.setTransactionSuccessful();
			} finally {
				cacheDb.endTransaction();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("NW", "Network Error occured");
			errFlg[0] = true;

			Cursor c = null;
			try {
				c = cacheDb.rawQuery("select room from rooms ;", new String[] {});
				if (c.moveToFirst()) {
					do {
						ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
						dataList.add((YouRoomGroup) ois.readObject());
					} while (c.moveToNext());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			} finally {
				if (c != null) {
					c.close();
				}
			}
		}

		// 暫定的なチェック
		// String lastAccessTime = youRoomUtil.getAccessTime();
		// UserSession session = UserSession.getInstance();
		// session.setLastAccessTime(lastAccessTime);
		// String currentTime = YouRoomUtil.getYesterdayFormattedTime();
		String currentTime = YouRoomUtil.getRFC3339FormattedTime();
		youRoomUtil.storeAccessTime(currentTime);

		return dataList;
	}

	public ArrayList<YouRoomEntry> acquireHomeEntryList(Map<String, String> parameterMap, boolean[] errFlg) {
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		try {
			String homeTimeline = youRoomCommand.acquireHomeTimeline(parameterMap);
			JSONArray jsons = new JSONArray(homeTimeline);
			for (int i = 0; i < jsons.length(); i++) {
				YouRoomEntry roomEntry = new YouRoomEntry();
				JSONObject jObject = jsons.getJSONObject(i);
				JSONObject entryObject = jObject.getJSONObject("entry");

				int id = entryObject.getInt("id");
				String participationName = entryObject.getJSONObject("participation").getString("name");
				String content = entryObject.getString("content");

				String createdTime = entryObject.getString("created_at");
				String updatedTime = entryObject.getString("updated_at");

				roomEntry.setId(id);
				roomEntry.setUpdatedTime(updatedTime);
				roomEntry.setParticipationName(participationName);
				roomEntry.setCreatedTime(createdTime);
				roomEntry.setContent(content);

				int compareResult = YouRoomUtil.calendarCompareTo(youRoomUtil.getUpdateCheckTime(), updatedTime);
				if (compareResult < 0) {
					dataList.add(roomEntry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("NW", "Network Error occured");
			errFlg[0] = true;
		}

		if (dataList.size() > 0) {
			appHolder.clearDirty();
		}

		return dataList;
	}

	public ArrayList<YouRoomEntry> getRoomEntryList(String roomId, Map<String, String> parameterMap, boolean[] errFlg) {
		ArrayList<YouRoomEntry> entryList = new ArrayList<YouRoomEntry>();
		if (appHolder.isDirty(roomId)) {
			Log.i("CACHE", String.format("RoomTimeLine is Dirty [%s]", roomId));
			cacheDb.beginTransaction();
			try {
				cacheDb.execSQL("delete from timelines where roomId = ? ;", new Object[] { roomId });
				JSONArray jsonArray = new JSONArray(youRoomCommand.getRoomTimeLine(roomId, parameterMap));
				for (int i = 0; i < jsonArray.length(); i++) {
					YouRoomEntry entry = buildEntryFromJson(jsonArray.getJSONObject(i).getJSONObject("entry"));
					entry.setDescendantsCount(-1);
					entryList.add(entry);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					(new ObjectOutputStream(baos)).writeObject(entry);

					cacheDb.execSQL("insert into timelines(entryId, roomId, page, entry) values(?, ?, ?, ?) ;", new Object[] { entry.getId(), roomId, parameterMap.get("page"), baos.toByteArray() });
				}
				cacheDb.setTransactionSuccessful();
				appHolder.setDirty(roomId, false);
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("NW", "Network Error occured");
				errFlg[0] = true;
				Cursor c = null;
				try {
					c = cacheDb.rawQuery("select entry from timelines where roomId = ? and page = ? ;", new String[] { roomId, parameterMap.get("page") });
					if (c.moveToFirst()) {
						do {
							ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
							entryList.add((YouRoomEntry) ois.readObject());
						} while (c.moveToNext());
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				} finally {
					if (c != null) {
						c.close();
					}
				}
			} finally {
				cacheDb.endTransaction();
			}
		} else {
			Cursor c = null;
			cacheDb.beginTransaction();
			try {
				c = cacheDb.rawQuery("select entry from timelines where roomId = ? and page = ? ;", new String[] { roomId, parameterMap.get("page") });
				if (c.getCount() == 0) {
					Log.i("CACHE", String.format("RoomTimeLine Cache(page:%s) Miss [%s]", parameterMap.get("page"), roomId));
					JSONArray jsonArray = new JSONArray(youRoomCommand.getRoomTimeLine(roomId, parameterMap));
					for (int i = 0; i < jsonArray.length(); i++) {
						YouRoomEntry entry = buildEntryFromJson(jsonArray.getJSONObject(i).getJSONObject("entry"));
						entry.setDescendantsCount(-1);
						entryList.add(entry);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						(new ObjectOutputStream(baos)).writeObject(entry);

						cacheDb.execSQL("insert into timelines(entryId, roomId, page, entry) values(?, ?, ?, ?) ;",
								new Object[] { entry.getId(), roomId, parameterMap.get("page"), baos.toByteArray() });
					}
					cacheDb.setTransactionSuccessful();
				} else {
					Log.i("CACHE", String.format("RoomTimeLine Cache(page:%s) Hit  [%s]", parameterMap.get("page"), roomId));
					if (c.moveToFirst()) {
						do {
							ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
							entryList.add((YouRoomEntry) ois.readObject());
						} while (c.moveToNext());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("NW", "Network Error occured");
				errFlg[0] = true;
				Cursor c1 = null;
				try {
					c1 = cacheDb.rawQuery("select entry from timelines where roomId = ? and page = ? ;", new String[] { roomId, parameterMap.get("page") });
					if (c1.moveToFirst()) {
						do {
							ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c1.getBlob(0)));
							entryList.add((YouRoomEntry) ois.readObject());
						} while (c1.moveToNext());
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				} finally {
					if (c1 != null) {
						c1.close();
					}
				}
			} finally {
				cacheDb.endTransaction();
				if (c != null) {
					c.close();
				}
			}
		}
		return entryList;
	}

	public YouRoomEntry getEntry(String roomId, String entryId, String updatedTime, boolean[] errFlg) {
		YouRoomEntry entry = null;

		Cursor c = null;
		cacheDb.beginTransaction();
		try {
			c = cacheDb.rawQuery("select entry from entries where entryId = ? and roomId = ? and updatedTime = ? ;", new String[] { entryId, roomId, updatedTime });
			if (c.getCount() == 1) {
				Log.i("CACHE", String.format("Entry Cache Hit  [%s]", entryId));
				c.moveToFirst();
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				entry = (YouRoomEntry) ois.readObject();
			} else {
				Log.i("CACHE", String.format("Entry Cache Miss [%s]", entryId));
				JSONObject json = (new JSONObject(youRoomCommand.getEntry(roomId, entryId))).getJSONObject("entry");
				entry = buildEntryFromJson(json);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				(new ObjectOutputStream(baos)).writeObject(entry);
				cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[] { entryId, roomId });
				cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, entry) values(?, ?, ?, ?) ;", new Object[] { entryId, roomId, updatedTime, baos.toByteArray() });
				cacheDb.setTransactionSuccessful();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("NW", "Network Error occured");
			errFlg[0] = true;
			Cursor c1 = null;
			try {
				c1 = cacheDb.rawQuery("select entry from entries where entryId = ? and roomId = ? ;", new String[] { entryId, roomId, updatedTime });
				if (c.getCount() == 1) {
					c.moveToFirst();
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
					entry = (YouRoomEntry) ois.readObject();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			} finally {
				if (c1 != null) {
					c1.close();
				}
			}
		} finally {
			cacheDb.endTransaction();
			if (c != null) {
				c.close();
			}
		}
		return entry;
	}

	public YouRoomEntry getEntry(String roomId, String entryId) {
		YouRoomEntry entry = null;

		Cursor c = null;
		try {
			c = cacheDb.rawQuery("select entry from entries where entryId = ? and roomId = ? ;", new String[] { entryId, roomId });
			if (c.getCount() == 1) {
				Log.i("CACHE", String.format("Entry Cache Hit  [%s]", entryId));
				c.moveToFirst();
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(c.getBlob(0)));
				entry = (YouRoomEntry) ois.readObject();
			} else {
				throw new RuntimeException("cache disappear");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return entry;
	}

	public String createEntry(String roomId, String parentId, String entryContent, String rootId) {
		cacheDb.beginTransaction();
		String statusCode = null;
		try {
			statusCode = youRoomCommand.createEntry(roomId, parentId, entryContent);
			if (POST_OK.equals(statusCode)) {
				appHolder.setDirty(roomId, true);
				if (rootId != null) {
					JSONObject json = (new JSONObject(youRoomCommand.getEntry(roomId, rootId))).getJSONObject("entry");
					YouRoomEntry entry = buildEntryFromJson(json);
					String updatedTime = entry.getUpdatedTime();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					(new ObjectOutputStream(baos)).writeObject(entry);
					cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[] { rootId, roomId });
					cacheDb.execSQL("insert into entries(entryId, roomId, updatedTime, entry) values(?, ?, ?, ?) ;", new Object[] { rootId, roomId, updatedTime, baos.toByteArray() });
					cacheDb.setTransactionSuccessful();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("NW", "Network Error occured");
		} finally {
			cacheDb.endTransaction();
		}
		return statusCode;
	}

	private YouRoomEntry buildEntryFromJson(JSONObject json) {
		YouRoomEntry entry = new YouRoomEntry();
		try {
			entry.setId(json.getInt("id"));
			entry.setParticipationName(json.getJSONObject("participation").getString("name"));
			entry.setParticipationId(json.getJSONObject("participation").getString("id"));
			entry.setCreatedTime(json.getString("created_at"));
			entry.setUpdatedTime(json.getString("updated_at"));
			entry.setContent(json.getString("content"));
			entry.setDescendantsCount(json.optInt("descendants_count"));
			if (json.has("attachment")) {
				JSONObject attachment = json.getJSONObject("attachment");
				entry.setAttachmentType(attachment.getString("attachment_type"));
				if (attachment.getString("attachment_type").equals("Text")) {
					entry.setText(attachment.getJSONObject("data").getString("text"));
				}

				if (attachment.getString("attachment_type").equals("Link")) {
					entry.setLink(attachment.getJSONObject("data").getString("url"));
				}

				if (attachment.getString("attachment_type").equals("Image") || attachment.getString("attachment_type").equals("File")) {
					entry.setFileName(attachment.getString("filename"));
				}
			}
			if (json.has("children")) {
				JSONArray cArray = json.getJSONArray("children");
				ArrayList<YouRoomEntry> children = new ArrayList<YouRoomEntry>(cArray.length());
				for (int i = 0; i < cArray.length(); i++) {
					JSONObject child = cArray.getJSONObject(i);
					children.add(buildEntryFromJson(child));
				}
				entry.setChildren(children);
			} else {
				entry.setChildren(new ArrayList<YouRoomEntry>());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return entry;
	}
}
