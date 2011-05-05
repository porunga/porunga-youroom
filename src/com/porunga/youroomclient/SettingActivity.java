package com.porunga.youroomclient;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class SettingActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.setting);

		String[] updateTimeNames = getResources().getStringArray(R.array.update_time_name_list);
		String[] updateTimeValues = getResources().getStringArray(R.array.update_time_value_list);
		final Map<String, String> updateTimeMap = new HashMap<String, String>();

		for (int i = 0; i < updateTimeNames.length; i++) {
			updateTimeMap.put(updateTimeValues[i], updateTimeNames[i]);
		}

		CharSequence cs = getText(R.string.ID_updateTimePreference);
		ListPreference pref = (ListPreference) findPreference(cs);
		pref.setSummary(updateTimeMap.get(pref.getValue()));

		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				Intent serviceIntent = new Intent(getApplication(), CheckUpdateService.class);
				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, serviceIntent, 0);

				String updateTime = String.valueOf(newValue);
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
				preference.setSummary(updateTimeMap.get(newValue));
				return true;
			}
		});
	}
}
