package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.tokaneoka.youroomclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RoomActivity extends Activity {
    /** Called when the activity is first created. */
	
	private String roomId;
	private YouRoomUtil youRoomUtil = new YouRoomUtil(this);

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
        
		HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
		YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
		String roomTL = "";
		roomTL= youRoomCommand.getRoomTimeLine(roomId);
		
		ListView listView = (ListView)findViewById(R.id.listView1);
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
        	
		try {
			JSONArray jsons = new JSONArray(roomTL);
			for(int i =0 ; i< jsons.length(); i++){
				YouRoomEntry roomEntry = new YouRoomEntry();
				JSONObject jObject = jsons.getJSONObject(i);
				JSONObject entryObject = jObject.getJSONObject("entry");

				int id = entryObject.getInt("id");
				String participationName = entryObject.getJSONObject("participation").getString("name");
				String content = entryObject.getString("content");
    		    
				String formattedTime = "";
				String unformattedTime = entryObject.getString("created_at");
    		    	
				formattedTime = YouRoomUtil.convertDatetime(unformattedTime);
    
				roomEntry.setId(id);
				roomEntry.setParticipationName(participationName);
				roomEntry.setCreatedTime(formattedTime);
				roomEntry.setContent(content);
    		    	
				dataList.add(roomEntry);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    		
		YouRoomEntryAdapter adapter = new YouRoomEntryAdapter(this, R.layout.list_item, dataList);
		listView.setAdapter(adapter);
    		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
				Intent intent = new Intent(getApplication(), EntryActivity.class);
				intent.putExtra("roomId", String.valueOf(roomId) );
				intent.putExtra("entryId", String.valueOf(item.getId()));
				startActivity(intent);
			}
		});
	}
	    
    // ListViewカスタマイズ用のArrayAdapterに利用するクラス    
	public class YouRoomEntry {

		private int id;
		private String content;
		private int rootId;
		private int parentId;
		private String createdTime;
		private String updatedTime;
		
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
		
	}
    
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomEntryAdapter extends ArrayAdapter<YouRoomEntry> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomEntry> items;
		
		public YouRoomEntryAdapter( Context context, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.list_item, null);				
			}
			YouRoomEntry roomEntry = (YouRoomEntry)this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView createdTime = null;
			
			if ( roomEntry != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				createdTime = (TextView)view.findViewById(R.id.textView2);
				content = (TextView)view.findViewById(R.id.textView3);
			}
			if ( name != null ){
				name.setText(roomEntry.getParticipationName());
			}
			if ( createdTime != null ){
				createdTime.setText(roomEntry.getCreatedTime());
			}
			if ( content != null ){
				content.setText(roomEntry.getContent());
			}
			return view;
		}
	}
	
}

