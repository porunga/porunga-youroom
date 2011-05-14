package com.porunga.youroomclient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class YouRoomUtil extends ContextWrapper {
	private static final String PREFERENCE_KEY = "AccessToken";
	private static final String LAST_ACCESS_TIME_KEY = "LastAccessTime";
	private SharedPreferences sharedpref;
	private String TOKEN_MAP_KEY = "oauth_token";
	private String TOKEN_SECRET_MAP_KEY = "oauth_token_secret";


	public YouRoomUtil(Context base) {
		super(base);
	}

	public HashMap<String, String> getOauthTokenFromLocal() {

		HashMap<String, String> oAuthTokenMap = new HashMap<String, String>();

		sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND);
		String oauthToken = sharedpref.getString(TOKEN_MAP_KEY, null);
		String oauthTokenSecret = sharedpref.getString(TOKEN_SECRET_MAP_KEY, null);

		oAuthTokenMap.put(TOKEN_MAP_KEY, oauthToken);
		oAuthTokenMap.put(TOKEN_SECRET_MAP_KEY, oauthTokenSecret);

		return oAuthTokenMap;
	}

	public boolean removeOauthTokenFromLocal() {

		boolean check = false;
		sharedpref = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
		Editor editor = sharedpref.edit();
		editor.putString(TOKEN_MAP_KEY, null);
		editor.putString(TOKEN_SECRET_MAP_KEY, null);
		check = editor.commit();
		return check;
	}

	public boolean storeOauthTokenToLocal(HashMap<String, String> oAuthTokenMap) {

		boolean check = false;
		sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND);
		Editor editor = sharedpref.edit();
		editor.putString(TOKEN_MAP_KEY, oAuthTokenMap.get("oauth_token"));
		editor.putString(TOKEN_SECRET_MAP_KEY, oAuthTokenMap.get(TOKEN_SECRET_MAP_KEY));
		check = editor.commit();
		return check;
	}

	public String getRoomAccessTime(String roomId) {

		String key = "LastAccessTime_" + roomId;
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, Activity.MODE_APPEND);
		String lastAccessTime = sharedpref.getString(key, null);

		return lastAccessTime;
	}

	public boolean removeRoomAccessTime(String roomId) {

		boolean check = false;
		String key = "LastAccessTime_" + roomId;
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, MODE_PRIVATE);
		Editor editor = sharedpref.edit();
		editor.putString(key, null);
		check = editor.commit();
		return check;
	}

	public boolean storeRoomAccessTime(String roomId, String RFC3339FormattedTime) {

		boolean check = false;
		String key = "LastAccessTime_" + roomId;
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, Activity.MODE_APPEND);
		Editor editor = sharedpref.edit();
		editor.putString(key, RFC3339FormattedTime);
		check = editor.commit();
		return check;
	}

	public String getAccessTime() {

		String key = "LastAccessTime";
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, Activity.MODE_APPEND);
		String lastAccessTime = sharedpref.getString(key, null);

		return lastAccessTime;
	}

	public boolean removeAccessTime() {

		boolean check = false;
		String key = "LastAccessTime";
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, MODE_PRIVATE);
		Editor editor = sharedpref.edit();
		editor.putString(key, null);
		check = editor.commit();
		return check;
	}

	public boolean storeAccessTime(String RFC3339FormattedTime) {

		boolean check = false;
		String key = "LastAccessTime";
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, Activity.MODE_APPEND);
		Editor editor = sharedpref.edit();
		editor.putString(key, RFC3339FormattedTime);
		check = editor.commit();
		return check;
	}

	public String getUpdateCheckTime() {

		String key = "UpdateCheckTime";
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, Activity.MODE_APPEND);
		String lastAccessTime = sharedpref.getString(key, null);

		return lastAccessTime;
	}

	public boolean removeUpdateCheckTime() {

		boolean check = false;
		String key = "UpdateCheckTime";
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, MODE_PRIVATE);
		Editor editor = sharedpref.edit();
		editor.putString(key, null);
		check = editor.commit();
		return check;
	}

	public boolean storeUpdateCheckTime(String RFC3339FormattedTime) {

		boolean check = false;
		String key = "UpdateCheckTime";
		sharedpref = getSharedPreferences(LAST_ACCESS_TIME_KEY, Activity.MODE_APPEND);
		Editor editor = sharedpref.edit();
		editor.putString(key, RFC3339FormattedTime);
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
		// とりあえず、oauthTokenがあるかどうかのみチェック
		if (oauthToken != null && oauthTokenSecret != null) {
			check = true;
		}
		return check;
	}

	// "2011-03-02T12:46:06Z" -> "2011/03/02 21:46:06"
	public static String convertDatetime(String unformattedTime) {
		// 現在時刻と指定日との差を表示する

		Calendar cal = getDesignatedCalendar(unformattedTime);
		Calendar currentCal = getCurrentCalendar();
		currentCal.add(Calendar.MONTH, 1);

		Calendar displayCal = new GregorianCalendar();
		long milliseconds = currentCal.getTimeInMillis() - cal.getTimeInMillis();
		displayCal.setTimeInMillis(milliseconds);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm");

//		if ((milliseconds/1000) < 60) {
//			return String.valueOf((milliseconds/1000) + "秒前");
//		}else 
		if (milliseconds / (1000 * 60) < 60) {
			return String.valueOf(milliseconds / (1000 * 60) + "分前");
		} else if (milliseconds / (1000 * 60 * 60) < 24) {
			return String.valueOf(milliseconds / (1000 * 60 * 60) + "時間前");
		} else if (displayCal.get(Calendar.YEAR) > 1970) {
			return cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + " " + format.format(cal.getTime());
		} else {
			return cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + format.format(cal.getTime());
		}
	}

	public static Calendar getDesignatedCalendar(String unformattedTime) {

		// ( "2011-03-02T12:46:06Z" | "2011-03-02T12:46:06+09:00" ) ->
		// "2011/03/02 21:46:06"
		unformattedTime = unformattedTime.replaceAll("(\\+[0-9+:]+)|Z", "");
		String[] updateTimes = unformattedTime.substring(0, unformattedTime.length()).split("T");
		String[] date = updateTimes[0].split("-");
		String[] times = updateTimes[1].split(":");
		int year = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		int day = Integer.parseInt(date[2]);
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		int second = Integer.parseInt(times[2]);

		Calendar cal = new GregorianCalendar(year, month, day, hour, minute, second);
		cal.add(Calendar.HOUR, 9);

		return cal;
	}

	public static Calendar getCurrentCalendar() {

		Calendar currentCal = new GregorianCalendar(Locale.JAPAN);

		return currentCal;
	}

	public static String getRFC3339FormattedTime() {

		Calendar calendar = getCurrentCalendar();
		calendar.add(Calendar.HOUR_OF_DAY, -9);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		String result = format.format(calendar.getTime());
		String standardName = "";
		if (result.contains("JST")) {
			standardName = "JST";
			result = result.replaceAll(standardName, "+09:00");
		} else if (result.contains("GMT")) {
			standardName = "GMT";
			result = result.replaceAll(standardName, "");
		}
		return result;
	}

	public static int calendarCompareTo(String unformattedTime1, String unformattedTime2) {

		int result = 0;
		Calendar cal1 = getDesignatedCalendar(unformattedTime1);
		Calendar cal2 = getDesignatedCalendar(unformattedTime2);
		result = cal1.compareTo(cal2); // cal1が時間的に前なら-1
		return result;
	}

	// TODO For Debugging 昨日の時間をRFC3339形式で取得
	public static String getYesterdayFormattedTime() {

		Calendar calendar = getCurrentCalendar();
		calendar.add(Calendar.HOUR_OF_DAY, -9);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		String result = format.format(calendar.getTime());
		String standardName = "";
		if (result.contains("JST")) {
			standardName = "JST";
			result = result.replaceAll(standardName, "+09:00");
		} else if (result.contains("GMT")) {
			standardName = "GMT";
			result = result.replaceAll(standardName, "");
		}
		return result;
	}
}
