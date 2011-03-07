package jp.co.tokaneoka.youroomclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import jp.co.tokaneoka.youroomclient.R;
import jp.co.tokaneoka.youroomclient.R.id;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class YouRoomClientActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private static final String PREFERENCE_KEY = "AccessToken";
	SharedPreferences sharedpref;
	private final int DELETE_TOKEN = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
	public void onClick(View v) {
    	if ( v.getId() == R.id.login_button){
    		Intent intent = new Intent(this, LoginActivity.class); 
    		startActivity(intent);
    	}
	}		
	
	@Override
	public void onStart(){
		super.onStart();

        if( !isLogined() ){
            setContentView(R.layout.top);
        	Button login_button = (Button)findViewById(R.id.login_button);
        	login_button.setOnClickListener(this);

        } else {
            setContentView(R.layout.main);

            HashMap<String, String> oAuthTokenMap = getOauthTokenFromLocal();
        	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
        	String roomTL = "";
        	roomTL= youRoomCommand.getRoomTimeLine();

    		ListView listView = (ListView)findViewById(R.id.listView1);
    		ArrayList<RoomTimeLine> datalist = new ArrayList<RoomTimeLine>();
        	
    		try {
    	    	JSONArray jsons = new JSONArray(roomTL);
    	    	for(int i =0 ; i< jsons.length(); i++){
    	    		RoomTimeLine roomTimeLine = new RoomTimeLine();
    		    	JSONObject jobject = jsons.getJSONObject(i);
    		    	JSONObject entryobject = jobject.getJSONObject("entry");
    		    	String name = entryobject.getJSONObject("participation").getString("name");
    		    	String content = entryobject.getString("content");
    		    	String formattedTime = "";
    		    	String unformattedTime = entryobject.getString("created_at");
    		    	
    		    	formattedTime = convertDatetime(unformattedTime);
    
    		    	roomTimeLine.setName(name);
    		    	roomTimeLine.setUpdateTime(formattedTime);
    		    	roomTimeLine.setContent(content);
    	    		datalist.add(roomTimeLine);
    	    	}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    		
    		RoomTimeLineAdapter adapter = new RoomTimeLineAdapter(this, R.layout.list_item, datalist);
    		listView.setAdapter(adapter);
    		
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
	
	private HashMap<String, String> getOauthTokenFromLocal(){
		HashMap<String, String> oAuthTokenMap = new HashMap<String, String>();
		
    	sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND );
		String oauthToken = sharedpref.getString("oauthToken", null);
		String oauthTokenSecret = sharedpref.getString("oauthTokenSecret", null);
		
		oAuthTokenMap.put("oauth_token", oauthToken);
		oAuthTokenMap.put("oauth_token_secret", oauthTokenSecret);
		
		return oAuthTokenMap;
	}
	
	// "2011-03-02T12:46:06Z" -> "2011/03/02 21:46:06"
    private String convertDatetime(String unformattedTime) {
    	
    	String[] updateTimes = unformattedTime.substring(0, unformattedTime.length() -1).split("T");
    	String[] date = updateTimes[0].split("-");
    	String[] times = updateTimes[1].split(":");
    	int year = Integer.parseInt(date[0]);
    	int month = Integer.parseInt(date[1]);
    	int day = Integer.parseInt(date[2]);
    	int hour = Integer.parseInt(times[0]);
    	int minute = Integer.parseInt(times[1]);
    	int second = Integer.parseInt(times[2]);
    	
    	Calendar cal = new GregorianCalendar(year, month ,day, hour, minute, second);
    	cal.add(Calendar.HOUR, 9);
    	
    	return cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) +"/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
    	
	}

    // ListViewカスタマイズ用のArrayAdapterに利用するクラス    
	public class RoomTimeLine {

		private String content;
		private String updateTime;
		private String name;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getContent(){
			return content;
		}
		
		public void setContent(String content) {
			this.content = content;
		}

		public String getUpdateTime() {
			return updateTime;
		}
		
		public void setUpdateTime(String updateTime) {
			this.updateTime = updateTime;
		}		
	}
    
    // ListViewカスタマイズ用のArrayAdapter
	public class RoomTimeLineAdapter extends ArrayAdapter<RoomTimeLine> {
		private LayoutInflater inflater;
		private ArrayList<RoomTimeLine> items;
		
		public RoomTimeLineAdapter( Context context, int textViewResourceId, ArrayList<RoomTimeLine> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.list_item, null);				
			}
			RoomTimeLine roomTL = (RoomTimeLine)this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView updateTime = null;
			
			if ( roomTL != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				updateTime = (TextView)view.findViewById(R.id.textView2);
				content = (TextView)view.findViewById(R.id.textView3);
			}
			if ( name != null ){
				name.setText(roomTL.getName());
			}
			if ( updateTime != null ){
				updateTime.setText(roomTL.getUpdateTime());
			}
			if ( content != null ){
				content.setText(roomTL.getContent());
			}
			return view;
		}
	}
	
}

