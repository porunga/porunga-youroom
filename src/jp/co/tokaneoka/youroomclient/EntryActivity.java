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

public class EntryActivity extends Activity {
		
	String roomId;
	
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
        String entryId = intent.getStringExtra("entryId");
        
    	//TODO if String decodeResult = "";
    	ListView listView = (ListView)findViewById(R.id.listView1);
		ArrayList<YouRoomChildEntry> dataList = new ArrayList<YouRoomChildEntry>();
		int level = 0;
        dataList = getChild(roomId, entryId, level);
        
		YouRoomChildEntryAdapter adapter = new YouRoomChildEntryAdapter(this, R.layout.entry_list_item, dataList);
		listView.setAdapter(adapter);		
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
    
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomChildEntryAdapter extends ArrayAdapter<YouRoomChildEntry> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomChildEntry> items;
		
		public YouRoomChildEntryAdapter( Context context, int textViewResourceId, ArrayList<YouRoomChildEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			int listSize;
			for (int i = 0; i< items.size(); i++ ){
				String entryId = String.valueOf(items.get(i).getId());
				ArrayList<YouRoomChildEntry> dataList = getChild(roomId, entryId, items.get(i).getLevel() + 1 );
				listSize = dataList.size();
				if (listSize > 0) {
					for (int j = 0; j< listSize; j++){
						items.add(i+j+1, dataList.get(j));
					}
				}
			}
		}
			
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.entry_list_item, null);				
			}
			YouRoomChildEntry roomEntry = (YouRoomChildEntry)this.getItem(position);
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
			
			return view;
		}
	}
	
	private ArrayList<YouRoomChildEntry> getChild(String roomId, String entryId, int level){
        YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());        
        HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
    	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
    			    	
    	String entry = "";
    	entry = youRoomCommand.getEntry(roomId, entryId);
		ArrayList<YouRoomChildEntry> dataList = new ArrayList<YouRoomChildEntry>();
		try {
			JSONObject json = new JSONObject(entry);
			if (json.getJSONObject("entry").has("children")){
	    	JSONArray children = json.getJSONObject("entry").getJSONArray("children");
	    	
	    	for(int i =0 ; i< children.length(); i++){
	    		YouRoomChildEntry roomChildEntry = new YouRoomChildEntry();
	    		
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

}
