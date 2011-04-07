package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
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
	public void onCreate(){
		// For Debugging
		Toast.makeText(this, "更新確認を開始しました。", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onStart(Intent intent, int StartId){
		
    	String lastAccessTime = youRoomUtil.getAccessTime();
    	youRoomUtil.storeUpdateCheckTime(lastAccessTime);
		
    	CheckUpdateEntryTask task = new CheckUpdateEntryTask();
		task.execute();
				
		/*
		// require 2005-08-09T10:57:00-08:00
		// actual  2011-03-24T04:28:39+09:00
		String checkTime = YouRoomUtil.getYesterdayFormattedTime();		
		String encodedCheckTime = "";
	   	try {
	   		encodedCheckTime = URLEncoder.encode(checkTime, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CheckUpdateEntryTask task = new CheckUpdateEntryTask();
		Toast.makeText(this, "24時間前から現在までの間に更新のあったエントリをチェックします。", Toast.LENGTH_LONG).show();
		task.execute(encodedCheckTime);
		*/
		
	}
	
	@Override
	public void onDestroy(){
		// For Debugging
		timer.cancel();
		Toast.makeText(this, "更新確認を終了しました。", Toast.LENGTH_LONG).show();
	}

	private ArrayList<YouRoomEntry> acquireHomeEntryList(Map<String, String> parameterMap){
		
        YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
        HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
    	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
    	String homeTimeline = youRoomCommand.acquireHomeTimeline(parameterMap);

		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
    	
		try {
			JSONArray jsons = new JSONArray(homeTimeline);
			for(int i =0 ; i< jsons.length(); i++){
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
				if ( compareResult < 0 ){
					dataList.add(roomEntry);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return dataList;
	}

	public class CheckUpdateEntryTask extends AsyncTask<String, Void, ArrayList<YouRoomEntry>> {
				
		@Override
		protected ArrayList<YouRoomEntry> doInBackground(String... times) {
		   	Map<String, String> parameterMap = new HashMap<String, String>();
			ArrayList<YouRoomEntry> dataList = acquireHomeEntryList(parameterMap);
			return dataList;
		}
				
		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataList){
			
			String message = "";
			
			int updateItemCount = dataList.size();						
			if ( updateItemCount > 0) {
				if ( updateItemCount == 10 ) {
					message = updateItemCount + "件以上の更新があります。";
				} else {
					message = updateItemCount + "件の更新があります。";
				}
				
				Class distActivity = GroupActivity.class;
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

}
