package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import jp.co.tokaneoka.youroomclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EntryActivity extends Activity {
		
	String roomId;
	YouRoomChildEntryAdapter adapter;
	ProgressDialog progressDialog;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart(){
		super.onStart();
		
        setContentView(R.layout.main);
		
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        YouRoomEntry youRoomEntry = (YouRoomEntry) intent.getSerializableExtra("youRoomEntry");
        String entryId = String.valueOf(youRoomEntry.getId());
        
    	//TODO if String decodeResult = "";
    	ListView listView = (ListView)findViewById(R.id.listView1);
		
		/*
		int level = 0;
		ArrayList<YouRoomEntry> dataList = getChild(roomId, entryId, level);
		adapter = new YouRoomChildEntryAdapter(this, R.layout.entry_list_item, dataList);
		listView.setAdapter(adapter);
		
		for(int i=0; i< dataList.size(); i++){
			try {
				GetChildEntryTask task = new GetChildEntryTask(roomId);
				task.execute(dataList.get(i));
			} catch (RejectedExecutionException e) {
				// TODO AsyncTaskでは内部的にキューを持っていますが、このキューサイズを超えるタスクをexecuteすると、ブロックされずに例外が発生します。らしいので、一旦握りつぶしている
				e.printStackTrace();
			}
		}
		*/
    	/*
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("処理を実行中しています");
		progressDialog.setCancelable(true);
		progressDialog.show();
    	*/
		int level = -1;
		youRoomEntry.setLevel(level);		
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		adapter = new YouRoomChildEntryAdapter(this, R.layout.entry_list_item, dataList);
		listView.setAdapter(adapter);

		GetChildEntryTask task = new GetChildEntryTask(roomId);
		try {
			task.execute(youRoomEntry);
		} catch  (RejectedExecutionException e) {
			// TODO AsyncTaskでは内部的にキューを持っていますが、このキューサイズを超えるタスクをexecuteすると、ブロックされずに例外が発生します。らしいので、一旦握りつぶしている
			e.printStackTrace();
		}	
	}
	
	/*
    // ListViewカスタマイズ用のArrayAdapterに利用するクラス    
	public class YouRoomChildEntry {

		private int id;
		private String content;
		private int rootId;
		private int parentId;
		private String createdTime;
		private String updatedTime;
		private int descendantsCount;
		private String participationName;
		private String participationId;
		private int level;
		
		public int getLevel() {
			return level;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public int getRootId() {
			return rootId;
		}
		public void setRootId(int rootId) {
			this.rootId = rootId;
		}
		public int getParentId() {
			return parentId;
		}
		public void setParentId(int parentId) {
			this.parentId = parentId;
		}
		public String getCreatedTime() {
			return createdTime;
		}
		public void setCreatedTime(String createdTime) {
			this.createdTime = createdTime;
		}
		public String getUpdatedTime() {
			return updatedTime;
		}
		public void setUpdatedTime(String updatedTime) {
			this.updatedTime = updatedTime;
		}
		public String getParticipationName() {
			return participationName;
		}
		public void setParticipationName(String participationName) {
			this.participationName = participationName;
		}
		public String getParticipationId() {
			return participationId;
		}
		public void setParticipationId(String participationId) {
			this.participationId = participationId;
		}
		public int getDescendantsCount() {
			return descendantsCount;
		}
		public void setDescendantsCount(int descendantsCount) {
			this.descendantsCount = descendantsCount;
		}
		
	}
	*/
    
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomChildEntryAdapter extends ArrayAdapter<YouRoomEntry> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomEntry> items;
		
		public YouRoomChildEntryAdapter( Context context, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.entry_list_item, null);				
			}
			YouRoomEntry roomEntry = (YouRoomEntry)this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView updateTime = null;
			TextView level = null;
			
			if ( roomEntry != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				updateTime = (TextView)view.findViewById(R.id.textView2);
				content = (TextView)view.findViewById(R.id.textView3);
				level = (TextView)view.findViewById(R.id.textView5);
			}
			if ( name != null ){
				name.setText(roomEntry.getParticipationName());
			}
			if ( updateTime != null ){
				updateTime.setText(roomEntry.getUpdatedTime());
			}
			if ( content != null ){
				content.setText(roomEntry.getContent());
			}
			if ( level != null ){
				String commentLevel = "";
				for(int i=0; i < roomEntry.getLevel(); i++)
					commentLevel += "-> ";
				level.setText(commentLevel);
			}
			/*
			if ( !resultDataListId.contains(roomEntry.getId()) && !requestFinishedId.contains(roomEntry.getId())){
				try {
					GetChildEntryTask task = new GetChildEntryTask(roomId);
					task.execute(roomEntry);
				} catch (RejectedExecutionException e) {
					// TODO AsyncTaskでは内部的にキューを持っていますが、このキューサイズを超えるタスクをexecuteすると、ブロックされずに例外が発生します。らしいので、一旦握りつぶしている
					e.printStackTrace();
				}
			}

			ArrayList<YouRoomEntry> dataChildList = getChild(roomId, entryId, roomEntry.getLevel() + 1 );
			
			if ( dataChildList.size() > 0){
				for (int i=0; i< dataChildList.size(); i++){
					if ( !resultDataListId.contains(dataChildList.get(i).getId()) ){
						adapter.insert(dataChildList.get(i), position + i + 1);
						resultDataListId.add(dataChildList.get(i).getId());
					} else {
					}
				}
				adapter.notifyDataSetChanged();
			}
			*/
			
			return view;
		}
	}
	
	private ArrayList<YouRoomEntry> getChild(String roomId, String entryId, int level){
        YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());        
        HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
    	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
    			    	
    	String entry = "";
    	entry = youRoomCommand.getEntry(roomId, entryId);
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		try {
			JSONObject json = new JSONObject(entry);
			if (json.getJSONObject("entry").has("children")){
	    	JSONArray children = json.getJSONObject("entry").getJSONArray("children");
	    	
	    	for(int i =0 ; i< children.length(); i++){
	    		YouRoomEntry roomChildEntry = new YouRoomEntry();
	    		
		    	JSONObject childObject = children.getJSONObject(i);

		    	int id = childObject.getInt("id");
		    	String participationName = childObject.getJSONObject("participation").getString("name");
		    	String jcontent = childObject.getString("content");
		    	String formattedTime = "";
		    	String unformattedTime = childObject.getString("created_at");
		    		
		    	formattedTime = YouRoomUtil.convertDatetime(unformattedTime);

		    	roomChildEntry.setId(id);
		    	roomChildEntry.setParticipationName(participationName);
		    	roomChildEntry.setUpdatedTime(formattedTime);
		    	roomChildEntry.setContent(jcontent);
		    	roomChildEntry.setLevel(level);
		    	dataList.add(roomChildEntry);
	    	}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataList;
	}
	
	public class GetChildEntryTask extends AsyncTask<YouRoomEntry, Void, ArrayList<YouRoomEntry>> {
		
		private String roomId;
		private YouRoomEntry roomChildEntry;
		private Object objLock = new Object();
		
		public GetChildEntryTask(String roomId){
			this.roomId = roomId;
		}

		@Override
		protected ArrayList<YouRoomEntry> doInBackground(YouRoomEntry... roomChildEntries) {
			roomChildEntry = roomChildEntries[0];
			String entryId = String.valueOf(roomChildEntry.getId());
						
			ArrayList<YouRoomEntry> dataList = getChild(roomId, entryId, roomChildEntry.getLevel() + 1 );
			ArrayList<YouRoomEntry> dataChildList;
			
			if ( dataList.size() > 0){
				for (int i=0; i< dataList.size(); i++){
					GetChildEntryTask task = new GetChildEntryTask(roomId);
					task.execute(dataList.get(i));
				}
			}

			/*
			for (int i=0; i< dataList.size(); i++){
				String childEntryId = String.valueOf(dataList.get(i).getId());
				Log.e("!!!","entryId = " + entryId);
				dataChildList = getChild(roomId, childEntryId, dataList.get(i).getLevel() + 1 );
				int listSize = dataChildList.size();
				if (listSize > 0) {
					for (int j = 0; j< listSize; j++){
						dataList.add(i+j+1, dataChildList.get(j));
					}
				}
			}
			*/
			
			return dataList;			
		}
		
		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataChildList){
			synchronized (objLock){
				if (dataChildList.size() > 0) {
					for (int i=0; i< dataChildList.size(); i++){
						adapter.insert(dataChildList.get(i), adapter.getPosition(roomChildEntry) + i + 1);
					}
				}
				adapter.notifyDataSetChanged();
//				progressDialog.dismiss();
			}

		}			
	}
	
}
