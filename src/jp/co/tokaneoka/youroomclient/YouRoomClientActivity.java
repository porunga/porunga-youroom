package jp.co.tokaneoka.youroomclient;

import java.util.HashMap;

import jp.co.tokaneoka.youroomclient.R;
import jp.co.tokaneoka.youroomclient.R.id;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class YouRoomClientActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String PREFERENCE_KEY = "AccessToken";
	SharedPreferences sharedpref;
	private final int DELETE_TOKEN = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

	@Override
	public void onStart(){
		super.onStart();

        if( !isLogined() ){
        	Intent intent = new Intent(this, LoginActivity.class); 
        	startActivity(intent);
        } else {
        	       	
            HashMap<String, String> oAuthTokenMap = getOauthTokenFromLocal();
        	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
        	String roomTL = "";
        	roomTL= youRoomCommand.getRoomTimeLine();
        	String result = ""; 

    		try {
    	    	JSONArray jsons = new JSONArray(roomTL);
    	    	for(int i =0 ; i< jsons.length(); i++){
    		    	JSONObject jobject = jsons.getJSONObject(i);
    		    	JSONObject entryobject = jobject.getJSONObject("entry");
    		    	result += "[" + entryobject.getJSONObject("participation").getString("name") + "]\n";
    		    	result += entryobject.getString("content") + "\n";
    		    	result += " ------------------------------ " + "\n";
    	    	}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}

        	TextView textview = (TextView)findViewById(id.main_view);
        	textview.setText(result);
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
}