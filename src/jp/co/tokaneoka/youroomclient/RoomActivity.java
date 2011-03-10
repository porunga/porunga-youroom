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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class RoomActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String PREFERENCE_KEY = "AccessToken";
	SharedPreferences sharedpref;
	private final int DELETE_TOKEN = 1;
	private String roomId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
        
	@Override
	public void onStart(){
		super.onStart();

        if( !isLogined() ){
            setContentView(R.layout.top);
        	Button login_button = (Button)findViewById(R.id.login_button);
        	
            OnClickListener loginClickListener = new OnClickListener(){
            	public void onClick(View v) {
                	if ( v.getId() == R.id.login_button){
                		Intent intent = new Intent(getApplication(), LoginActivity.class); 
                		startActivity(intent);
                	}
            	}		    	
            };
        	
        	login_button.setOnClickListener(loginClickListener);

        } else {
            setContentView(R.layout.main);
            YouRoomUtil youRoomUtil = new YouRoomUtil(this);
            HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
        	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
        	String roomTL = "";
        	roomId = "726";
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
    		    	roomEntry.setUpdatedTime(formattedTime);
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
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(Menu.NONE, DELETE_TOKEN, DELETE_TOKEN, R.string.delete_token);
    return super.onCreateOptionsMenu(menu);
    }

	public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        case DELETE_TOKEN:
        	SharedPreferences pref = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
        	SharedPreferences.Editor editor = pref.edit();
        	editor.putString("oauthToken", null);
        	editor.putString("oauthTokenSecret", null);
        	editor.commit();
    		Intent intent = new Intent(this, LoginActivity.class); 
    		startActivity(intent);
        	ret = true;
	    	break;
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        }    
        return ret;
    }
    
	private boolean isLogined() {
		
		boolean check = false;
    	sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND );
		String oauthToken = sharedpref.getString("oauthToken", null);
		String oauthTokenSecret = sharedpref.getString("oauthTokenSecret", null);
		//とりあえず、oauthTokenがあるかどうかのみチェック
		if (oauthToken != null && oauthTokenSecret != null ){
			check = true;
		}
		return check;
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

