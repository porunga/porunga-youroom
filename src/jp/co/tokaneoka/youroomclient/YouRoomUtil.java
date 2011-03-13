package jp.co.tokaneoka.youroomclient;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class YouRoomUtil extends ContextWrapper {

	private static final String PREFERENCE_KEY = "AccessToken";
	SharedPreferences sharedpref;
	String TOKEN_MAP_KEY = "oauth_token";
	String TOKEN_SECRET_MAP_KEY = "oauth_token_secret";

	public YouRoomUtil(Context base) {
		super(base);
	}
	    
	public HashMap<String, String> getOauthTokenFromLocal(){
		
		HashMap<String, String> oAuthTokenMap = new HashMap<String, String>();
		
    	sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND );
		String oauthToken = sharedpref.getString(TOKEN_MAP_KEY, null);
		String oauthTokenSecret = sharedpref.getString(TOKEN_SECRET_MAP_KEY, null);
		
		oAuthTokenMap.put(TOKEN_MAP_KEY, oauthToken);
		oAuthTokenMap.put(TOKEN_SECRET_MAP_KEY, oauthTokenSecret);
		
		return oAuthTokenMap;
	}
	
	public boolean removeOauthTokenFromLocal(){
		
		boolean check = false;
    	sharedpref = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
    	Editor editor = sharedpref.edit();
    	editor.putString(TOKEN_MAP_KEY, null);
    	editor.putString(TOKEN_SECRET_MAP_KEY, null);
    	check = editor.commit();
    	return check;
	}
	
	public boolean storeOauthTokenToLocal(HashMap<String, String> oAuthTokenMap){
		
		boolean check = false;
		sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND );
		Editor editor = sharedpref.edit();
		editor.putString(TOKEN_MAP_KEY, oAuthTokenMap.get("oauth_token"));
		editor.putString(TOKEN_SECRET_MAP_KEY, oAuthTokenMap.get(TOKEN_SECRET_MAP_KEY));
		check = editor.commit();
		return check;
	}

	public boolean isLogined() {
		
		boolean check = false;
        String oauthToken = null;
        String oauthTokenSecret = null;
        HashMap<String, String> oAuthTokenMap = getOauthTokenFromLocal();
        
        oauthToken = oAuthTokenMap.get(TOKEN_MAP_KEY);
        oauthTokenSecret = oAuthTokenMap.get(TOKEN_SECRET_MAP_KEY);        
		//とりあえず、oauthTokenがあるかどうかのみチェック
		if (oauthToken != null && oauthTokenSecret != null ){
			check = true;
		}
		return check;
	}

	
	// "2011-03-02T12:46:06Z" -> "2011/03/02 21:46:06"
    public static String convertDatetime(String unformattedTime) {
    	
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


}
