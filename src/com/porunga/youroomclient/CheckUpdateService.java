package com.porunga.youroomclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class CheckUpdateService extends Service {

	Timer timer;
	private YouRoomUtil youRoomUtil = new YouRoomUtil(this);

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// For Debugging
		Toast.makeText(this, "更新確認を開始しました。", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStart(Intent intent, int StartId) {

		String lastAccessTime = youRoomUtil.getAccessTime();
		youRoomUtil.storeUpdateCheckTime(lastAccessTime);

		CheckUpdateEntryTask task = new CheckUpdateEntryTask(this);
		task.execute();
		
		CacheDeleteTask cacheDeleteTask = new CacheDeleteTask();
		cacheDeleteTask.execute(this);

		/*
		 * // require 2005-08-09T10:57:00-08:00 // actual
		 * 2011-03-24T04:28:39+09:00 String checkTime =
		 * YouRoomUtil.getYesterdayFormattedTime(); String encodedCheckTime =
		 * ""; try { encodedCheckTime = URLEncoder.encode(checkTime, "UTF-8"); }
		 * catch (UnsupportedEncodingException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 * 
		 * CheckUpdateEntryTask task = new CheckUpdateEntryTask();
		 * Toast.makeText(this, "24時間前から現在までの間に更新のあったエントリをチェックします。",
		 * Toast.LENGTH_LONG).show(); task.execute(encodedCheckTime);
		 */

	}

	@Override
	public void onDestroy() {
		// For Debugging
		timer.cancel();
		Toast.makeText(this, "更新確認を終了しました。", Toast.LENGTH_LONG).show();
	}

//	private ArrayList<YouRoomEntry> acquireHomeEntryList(Map<String, String> parameterMap) {
//
//		YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
//		HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
//		YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
//		String homeTimeline = youRoomCommand.acquireHomeTimeline(parameterMap);
//
//		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
//
//		try {
//			JSONArray jsons = new JSONArray(homeTimeline);
//			for (int i = 0; i < jsons.length(); i++) {
//				YouRoomEntry roomEntry = new YouRoomEntry();
//				JSONObject jObject = jsons.getJSONObject(i);
//				JSONObject entryObject = jObject.getJSONObject("entry");
//
//				int id = entryObject.getInt("id");
//				String participationName = entryObject.getJSONObject("participation").getString("name");
//				String content = entryObject.getString("content");
//
//				String createdTime = entryObject.getString("created_at");
//				String updatedTime = entryObject.getString("updated_at");
//
//				roomEntry.setId(id);
//				roomEntry.setUpdatedTime(updatedTime);
//				roomEntry.setParticipationName(participationName);
//				roomEntry.setCreatedTime(createdTime);
//				roomEntry.setContent(content);
//
//				int compareResult = YouRoomUtil.calendarCompareTo(youRoomUtil.getUpdateCheckTime(), updatedTime);
//				if (compareResult < 0) {
//					dataList.add(roomEntry);
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		return dataList;
//	}

	public class CheckUpdateEntryTask extends AsyncTask<String, Void, ArrayList<YouRoomEntry>> {
		private Service service;

		public CheckUpdateEntryTask(Service service) {
			this.service = service;
		}

		@Override
		protected ArrayList<YouRoomEntry> doInBackground(String... times) {
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(service);
			Map<String, String> parameterMap = new HashMap<String, String>();
			ArrayList<YouRoomEntry> dataList = proxy.acquireHomeEntryList(parameterMap);
			return dataList;
		}

		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataList) {

			String message = "";

			int updateItemCount = dataList.size();
			if (updateItemCount > 0) {
				if (updateItemCount == 10) {
					message = updateItemCount + "件以上の更新があります。";
				} else {
					message = updateItemCount + "件の更新があります。";
				}

				Class<GroupActivity> distActivity = GroupActivity.class;
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				Notification notification = new Notification(R.drawable.myrooms, message, System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.number = updateItemCount;
				Intent intent = new Intent(getApplication(), distActivity);
				PendingIntent contentIntent = PendingIntent.getActivity(getApplication(), 0, intent, 0);
				notification.setLatestEventInfo(getApplicationContext(), "youRoomClient", message, contentIntent);
				notificationManager.notify(R.string.app_name, notification);
			}
		}
	}
	
	public class CacheDeleteTask extends AsyncTask<Service, Void, Void> {
		
		@Override
		protected Void doInBackground(Service... service) {
			Log.i("CACHE", "CacheDelete");
			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.DATE, -3); //TODO 3日前固定
			String limit = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime());
			Log.i("CACHE", String.format("limit->%s", limit));
			//String limit = "2011-04-09T00:00:00Z";
			SQLiteDatabase cacheDb = ((AppHolder)service[0].getApplication()).getCacheDb();
			Cursor c = null;
			SQLiteStatement stmt = null;
			cacheDb.beginTransaction();
			try {
				c = cacheDb.rawQuery("select entryId, updatedTime from entries where updatedTime < ? ;", new String[]{limit});
				stmt = cacheDb.compileStatement("delete from entries where entryId = ? ;");
				c.moveToFirst();
				for (int i = 0; i < c.getCount(); i++) {
					Log.i("CACHE", String.format("Cache Delete [%d]", c.getInt(0)));
					stmt.bindLong(1, c.getLong(0));
					stmt.execute();
					c.moveToNext();
				}
				cacheDb.setTransactionSuccessful();
			} finally {
				stmt.close();
				c.close();
				cacheDb.endTransaction();
			}
			return null;
		}
	}
}
