package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class GroupActivity extends Activity {

	private final int DELETE_TOKEN = 1;
	private YouRoomUtil youRoomUtil = new YouRoomUtil(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart(){
		super.onStart();
		
        if( !youRoomUtil.isLogined() ){
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

        	String myGroups = youRoomCommand.getMyGroup();
        	
    		ListView listView = (ListView)findViewById(R.id.listView1);
    		ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();
        	
    		try {
    	    	JSONArray jsons = new JSONArray(myGroups);
    	    	for(int i =0 ; i< jsons.length(); i++){
    	    		YouRoomGroup group = new YouRoomGroup();
    		    	JSONObject jObject = jsons.getJSONObject(i);
    		    	JSONObject groupObject = jObject.getJSONObject("group");

    		    	int id = groupObject.getInt("id");
    		    	String name = groupObject.getString("name");
    		    	
    		    	String formattedTime = "";
    		    	String unformattedTime = groupObject.getString("updated_at");
    		    	
    		    	formattedTime = YouRoomUtil.convertDatetime(unformattedTime);
    
    		    	group.setId(id);
    		    	group.setName(name);
    		    	group.setUpdatedTime(formattedTime);
    		    	
    	    		dataList.add(group);
    	    	}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    		
    		YouRoomGroupAdapter adapter = new YouRoomGroupAdapter(this, R.layout.list_item, dataList);
    		listView.setAdapter(adapter);
    		
    		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    	        @Override
    	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	            ListView listView = (ListView) parent;
    	            YouRoomGroup item = (YouRoomGroup) listView.getItemAtPosition(position);
    	            Intent intent = new Intent(getApplication(), RoomActivity.class);
    	            intent.putExtra("roomId", String.valueOf(item.getId()));
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
        	if ( youRoomUtil.removeOauthTokenFromLocal() ){
        		Intent intent = new Intent(this, LoginActivity.class); 
        		startActivity(intent);
        	}
        	ret = true;
	    	break;
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        }    
        return ret;
    }

    // ListViewカスタマイズ用のArrayAdapterに利用するクラス    
	public class YouRoomGroup {

		private int id;
		private int roomId;
		private String createdTime;
		private String updatedTime;
		private String name;
		private boolean opened;

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getRoomId() {
			return roomId;
		}
		public void setRoomId(int roomId) {
			this.roomId = roomId;
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
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isOpened() {
			return opened;
		}
		public void setOpened(boolean opened) {
			this.opened = opened;
		}
				
	}
    
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomGroupAdapter extends ArrayAdapter<YouRoomGroup> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomGroup> items;
		
		public YouRoomGroupAdapter( Context context, int textViewResourceId, ArrayList<YouRoomGroup> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.list_item, null);				
			}
			YouRoomGroup group = (YouRoomGroup)this.getItem(position);
			TextView name = null;
			TextView updateTime = null;
			
			if ( group != null ){
				updateTime = (TextView)view.findViewById(R.id.textView2);
				name = (TextView)view.findViewById(R.id.textView3);
			}
			if ( name != null ){
				name.setText(group.getName());
			}
			if ( updateTime != null ){
				updateTime.setText(group.getUpdatedTime());
			}
			
			return view;
		}
	}
	
}
