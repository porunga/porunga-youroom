package jp.co.tokaneoka.youroomclient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class CheckUpdateService extends Service {

	Timer timer;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		// For Debugging
		// Toast.makeText(this, "Service Start", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onStart(Intent intent, int StartId){
				
		final Handler handler = new Handler();
		long delay = 1000;
		long period = 15000;
		
		timer = new Timer(false);		
		timer.schedule( new TimerTask(){		
			@Override
			public void run(){				
			   	Map<String, String> parameterMap = new HashMap<String, String>();

			   	String checkTime = YouRoomUtil.getYesterdayFormattedTime();		
				String encodedCheckTime = "";
			   	try {
			   		encodedCheckTime = URLEncoder.encode(checkTime, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			   	parameterMap.put("since", encodedCheckTime);
				final ArrayList<YouRoomEntry> dataList = acquireHomeEntryList(parameterMap);
				
				handler.post(new Runnable(){
					@Override
					public void run(){
						String message = "";
						if ( dataList.size() > 0) {
							message = dataList.size() + "件の更新があります。";
						} else {
							message = "更新はありません。";
						}
						Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
						String result = "";
						Iterator iterator = dataList.iterator();
						while( iterator.hasNext() ) {
							YouRoomEntry entry = ((YouRoomEntry) iterator.next());
							result += "[" + entry.getUpdatedTime() + "] " + entry.getContent() + "\n";
							result += " -------------------- \n" ;
						}
						Toast.makeText(getApplication(), result, Toast.LENGTH_LONG).show();
					}	
				});
			}
		}, delay, period);
	
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
    		    	
				dataList.add(roomEntry);
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
		   	parameterMap.put("since", times[0]);
			ArrayList<YouRoomEntry> dataList = acquireHomeEntryList(parameterMap);
			return dataList;
		}
				
		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataList){
			String message = "";
			if ( dataList.size() > 0) {
				message = dataList.size() + "件の更新があります。";
			} else {
				message = "更新はありません。";
			}
			Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
			String result = "";
			Iterator iterator = dataList.iterator();
			while( iterator.hasNext() ) {
				YouRoomEntry entry = ((YouRoomEntry) iterator.next());
				result += "[" + entry.getUpdatedTime() + "] " + entry.getContent() + "\n";
				result += " -------------------- \n" ;
			}
			Toast.makeText(getApplication(), result, Toast.LENGTH_LONG).show();

//			stopSelf();
		}
	}
	
	/*
	public class AcquireHomeEntryTask extends AsyncTask<Void, Void, ArrayList<YouRoomEntry>> {
		
		@Override
		protected ArrayList<YouRoomEntry> doInBackground(Void... ids) {
			ArrayList<YouRoomEntry> dataList = acquireHomeEntryList();
			return dataList;
		}
				
		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataList){
			Iterator iterator = dataList.iterator();
			String result = "";
			while( iterator.hasNext() ) {
				result += ((YouRoomEntry) iterator.next()).getContent() + "\n";
			}
			Toast.makeText(getApplication(), result, Toast.LENGTH_LONG).show();
			stopSelf();
		}
	}
	*/

}
