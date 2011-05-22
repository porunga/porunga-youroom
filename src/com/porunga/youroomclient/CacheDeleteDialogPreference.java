package com.porunga.youroomclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class CacheDeleteDialogPreference extends DialogPreference {

	private Context context;
	public CacheDeleteDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// TODO Auto-generated constructor stub
		setDialogTitle(R.string.deleteCacheDialogTitle);
		setDialogMessage(R.string.deleteCacheDialogMessage);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult){
			SQLiteDatabase cacheDb = ((AppHolder)context.getApplicationContext()).getCacheDb();
			cacheDb.beginTransaction();
			try {
				cacheDb.execSQL("delete from rooms");
				cacheDb.execSQL("delete from entries");
				cacheDb.execSQL("delete from timelines");
				cacheDb.execSQL("delete from memberImages");
				cacheDb.setTransactionSuccessful();
			} finally {
				cacheDb.endTransaction();
			}
		}
		super.onDialogClosed(positiveResult);
	}

}
