package com.porunga.youroomclient;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {

		Log.i("StartReceiver", "StartReceiver Start");

		Intent serviceIntent = new Intent(arg0, CheckUpdateService.class);
		AlarmManager alarmManager = (AlarmManager) arg0.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getService(arg0, 0, serviceIntent, 0);

		SharedPreferences sharedpref = PreferenceManager.getDefaultSharedPreferences(arg0);
		CharSequence cs = arg0.getText(R.string.ID_updateTimePreference);

		String key = cs.toString();
		String updateTime = String.valueOf(sharedpref.getString(key, "not_set"));

		if (updateTime.equals("not_set")) {
			pendingIntent.cancel();
			alarmManager.cancel(pendingIntent);
			Log.i("CheckUpdateService", "Not set CheckUpdateTime");
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			long updateTimeInMillis = Long.parseLong(updateTime) * (60 * 1000);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), updateTimeInMillis, pendingIntent);
			Log.i("CheckUpdateService", String.format("Set CheckUpdateTime every [%s] minutes", updateTime));
		}

	}

}
