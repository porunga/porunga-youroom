package com.porunga.youroomclient;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class CacheDeleteService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.i("CacheDeleteService", "CacheDeleteService is created");
	}

	@Override
	public void onStart(Intent intent, int StartId) {

		CacheDeleteTask cacheDeleteTask = new CacheDeleteTask();
		cacheDeleteTask.execute(this);
		this.stopSelf();
	}

	@Override
	public void onDestroy() {
		Log.i("CacheDeleteService", "CacheDeleteService is destory");
	}

	public class CacheDeleteTask extends AsyncTask<Service, Void, Void> {

		@Override
		protected Void doInBackground(Service... service) {
			Log.i("CACHE", "CacheDelete");

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.DATE, -3); // TODO 3日前固定
			String limit = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime());
			Log.i("CACHE", String.format("limit->%s", limit));
			// String limit = "2011-04-09T00:00:00Z";
			SQLiteDatabase cacheDb = ((AppHolder) service[0].getApplication()).getCacheDb();
			Cursor c = null;
			SQLiteStatement stmt = null;
			cacheDb.beginTransaction();
			try {
				c = cacheDb.rawQuery("select entryId, updatedTime from entries where updatedTime < ? order by updatedTime asc;", new String[] { limit });
				stmt = cacheDb.compileStatement("delete from entries where entryId = ? ;");
				c.moveToFirst();
				for (int i = 0; i < c.getCount() - 10; i++) {
					Log.i("CACHE", String.format("Cache Delete [%d]", c.getInt(0)));
					stmt.bindLong(1, c.getLong(0));
					stmt.execute();
					c.moveToNext();
				}
				cacheDb.setTransactionSuccessful();
			} finally {
				stmt.close();
				c.close();
				cacheDb.endTransaction();
			}
			return null;
		}
	}

}
