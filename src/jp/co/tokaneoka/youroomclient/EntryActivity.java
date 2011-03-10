package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.tokaneoka.youroomclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EntryActivity extends Activity {
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart(){
		super.onStart();
		
        setContentView(R.layout.main);
		
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");        
        String entryId = intent.getStringExtra("entryId");
        
        YouRoomUtil youRoomUtil = new YouRoomUtil(this);        
        HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
    	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
    	
    	String entry = "";
    	entry = youRoomCommand.getEntry(roomId, entryId);
//    	if String decodeResult = "";
		ListView listView = (ListView)findViewById(R.id.listView1);
		ArrayList<YouRoomChildEntry> dataList = new ArrayList<YouRoomChildEntry>();
    	
		try {
			JSONObject json = new JSONObject(entry);    			
	    	JSONArray children = json.getJSONObject("entry").getJSONArray("children");
	    	
	    	for(int i =0 ; i< children.length(); i++){
	    		YouRoomChildEntry roomChildEntry = new YouRoomChildEntry();
		    	
		    	JSONObject childObject = children.getJSONObject(i);
		    			    	
		    	String participationName = childObject.getJSONObject("participation").getString("name");
		    	String content = childObject.getString("content");
		    	String formattedTime = "";
		    	String unformattedTime = childObject.getString("created_at");
		    	
		    	formattedTime = YouRoomUtil.convertDatetime(unformattedTime);

		    	roomChildEntry.setParticipationName(participationName);
		    	roomChildEntry.setUpdatedTime(formattedTime);
		    	roomChildEntry.setContent(content);
	    		dataList.add(roomChildEntry);
	    		YouRoomChildEntryAdapter adapter = new YouRoomChildEntryAdapter(this, R.layout.list_item, dataList);
	    		listView.setAdapter(adapter);
	    	}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, "コメントはありません。", Toast.LENGTH_SHORT).show();
		}
		
	}
	
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
    
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomChildEntryAdapter extends ArrayAdapter<YouRoomChildEntry> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomChildEntry> items;
		
		public YouRoomChildEntryAdapter( Context context, int textViewResourceId, ArrayList<YouRoomChildEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.list_item, null);				
			}
			YouRoomChildEntry roomEntry = (YouRoomChildEntry)this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView updateTime = null;
			
			if ( roomEntry != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				updateTime = (TextView)view.findViewById(R.id.textView2);
				content = (TextView)view.findViewById(R.id.textView3);
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
			return view;
		}
	}


}
