package com.porunga.youroomclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import android.app.ProgressDialog;

public class GroupActivity extends Activity {

	private final int DELETE_TOKEN = 1;
	private final int REACQUIRE_GROUP = 2;
	private final int SETTING = 3;

	private YouRoomUtil youRoomUtil = new YouRoomUtil(this);
	private YouRoomGroupAdapter adapter;
	private ListView listView;

	// private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!youRoomUtil.isLogined()) {
			setContentView(R.layout.top);
			Button login_button = (Button) findViewById(R.id.login_button);

			OnClickListener loginClickListener = new OnClickListener() {
				public void onClick(View v) {
					if (v.getId() == R.id.login_button) {
						Intent intent = new Intent(getApplication(), LoginActivity.class);
						startActivity(intent);
					}
				}
			};

			login_button.setOnClickListener(loginClickListener);

		} else {
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.group_view);

			// progressDialog = new ProgressDialog(this);
			// setProgressDialog(progressDialog);

			listView = (ListView) findViewById(R.id.listView1);
			ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(this);
			dataList = proxy.getMyGroupListFromCache();
			// if (dataList.isEmpty())
			// progressDialog.show();

			GetGroupTask task = new GetGroupTask(this);
			task.execute();

			adapter = new YouRoomGroupAdapter(this, R.layout.group_list_item, dataList);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ListView listView = (ListView) parent;
					YouRoomGroup item = (YouRoomGroup) listView.getItemAtPosition(position);
					Intent intent = new Intent(getApplication(), RoomActivity.class);
					String roomId = String.valueOf(item.getId());
					intent.putExtra("roomId", roomId);

					UserSession session = UserSession.getInstance();
					String lastAccessTime = youRoomUtil.getRoomAccessTime(roomId);
					session.setRoomAccessTime(roomId, lastAccessTime);
					String time = YouRoomUtil.getRFC3339FormattedTime();
					youRoomUtil.storeRoomAccessTime(roomId, time);

					startActivity(intent);
				}
			});
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					ListView listView = (ListView) parent;
					YouRoomGroup item = (YouRoomGroup) listView.getItemAtPosition(position);
					String roomId = String.valueOf(item.getId());
					SQLiteDatabase cacheDb = ((AppHolder) getApplication()).getCacheDb();
					cacheDb.execSQL("delete from rooms where roomId = ?;", new String[] { roomId });
					return true;
				}
			});
			
			Context context = this.getApplicationContext();
			Intent serviceIntent = new Intent(context, CacheDeleteService.class);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0);

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 1);
			
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String setTime = (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + format.format(cal.getTime());
			alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
			Log.i("CacheDeleteService", String.format("Set CacheDeleteTime [%s] ", setTime));			
			
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, DELETE_TOKEN, DELETE_TOKEN, R.string.delete_token);
		menu.add(Menu.NONE, REACQUIRE_GROUP, REACQUIRE_GROUP, R.string.reacquire_group);
		menu.add(Menu.NONE, SETTING, SETTING, R.string.setting);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case DELETE_TOKEN:
			if (youRoomUtil.removeOauthTokenFromLocal()) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			}
			ret = true;
			break;
		case REACQUIRE_GROUP:
			// progressDialog = new ProgressDialog(this);
			// setProgressDialog(progressDialog);
			// progressDialog.show();
			// adapter.clear();
			GetGroupTask task = new GetGroupTask(this);
			task.execute();
			((AppHolder) getApplication()).clearDirty();
			ret = true;
			break;
		case SETTING:
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			ret = true;
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}

	// ListViewカスタマイズ用のArrayAdapter
	public class YouRoomGroupAdapter extends ArrayAdapter<YouRoomGroup> {
		private LayoutInflater inflater;
		private Activity activity;

		// private ArrayList<YouRoomGroup> items;

		public YouRoomGroupAdapter(Activity activity, int textViewResourceId, ArrayList<YouRoomGroup> items) {
			super(activity, textViewResourceId, items);
			// this.items = items;
			this.activity = activity;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.group_list_item, null);
			}
			YouRoomGroup group = (YouRoomGroup) this.getItem(position);
			ImageView roomImage = null;
			TextView name = null;
			TextView updateTime = null;

			if (group != null) {
				roomImage = (ImageView) view.findViewById(R.id.room_image);
				name = (TextView) view.findViewById(R.id.textView1);
				updateTime = (TextView) view.findViewById(R.id.textView2);
			}
			if (roomImage != null) {
				byte[] data = group.getRoomImage();
				Bitmap roomImageBitmap = null;
				roomImage.setTag(String.valueOf(group.getId()));
				roomImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
				if (data != null) {
					roomImageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					roomImage.setImageBitmap(roomImageBitmap);
				} else {
					GetRoomImageTask getRoomImageTask = new GetRoomImageTask(roomImage, activity);
					getRoomImageTask.execute(group);
				}
			}
			if (name != null) {
				name.setText(group.getName());
			}
			if (updateTime != null) {
				updateTime.setTextColor(Color.LTGRAY);
				updateTime.setText(YouRoomUtil.convertDatetime(group.getUpdatedTime()));
			}

			UserSession session = UserSession.getInstance();
			String roomAccessTime = session.getRoomAccessTime(String.valueOf(group.getId()));
			if (roomAccessTime != null) {
				int compareResult = YouRoomUtil.calendarCompareTo(roomAccessTime, group.getUpdatedTime());
				if (compareResult < 0) {
					updateTime.setTextColor(Color.RED);
				}
			}

			return view;
		}
	}

	public class GetGroupTask extends AsyncTask<Void, Void, ArrayList<YouRoomGroup>> {
		private Activity activity;
		private boolean[] errFlg = { false };

		public GetGroupTask(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected ArrayList<YouRoomGroup> doInBackground(Void... ids) {
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
			ArrayList<YouRoomGroup> dataList = proxy.getMyGroupList(errFlg);
			return dataList;
		}

		@Override
		protected void onPostExecute(ArrayList<YouRoomGroup> dataList) {
			if (errFlg[0]) {
				Toast.makeText(getBaseContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			} else {
				// Iterator iterator = dataList.iterator();
				// while (iterator.hasNext()) {
				// adapter.add((YouRoomGroup) iterator.next());
				// }
				adapter.clear();
				for (YouRoomGroup group : dataList) {
					adapter.add(group);
				}
				adapter.notifyDataSetChanged();
			}
			setProgressBarIndeterminateVisibility(false);
			listView.setSelection(0);

		}
	}

	public class GetRoomImageTask extends AsyncTask<YouRoomGroup, Void, Bitmap> {
		private ImageView roomImage;
		private Activity activity;
		private YouRoomGroup group;
		private boolean[] errFlg = { false };
		private String tag;

		public GetRoomImageTask(ImageView roomImage, Activity activity) {
			this.roomImage = roomImage;
			this.activity = activity;
			this.tag = roomImage.getTag().toString();
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Bitmap doInBackground(YouRoomGroup... params) {
			Bitmap roomImageBitmap;
			group = params[0];
			String roomId = String.valueOf(group.getId());

			YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
			try {
				roomImageBitmap = proxy.getRoomImageFromCache(roomId);
				if (roomImageBitmap == null) {
					roomImageBitmap = proxy.getRoomImage(roomId, errFlg);
					group.setRoomImage(roomImageBitmap);
				}
			} catch (Exception e) {
				e.printStackTrace();
				errFlg[0] = true;
				roomImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
			}
			return roomImageBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap roomImageBitmap) {
			if (errFlg[0]) {
				Toast.makeText(getBaseContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
			if (tag.equals(roomImage.getTag().toString()))
				this.roomImage.setImageBitmap(roomImageBitmap);
			setProgressBarIndeterminateVisibility(false);
		}
	}
	/*
	 * public void setProgressDialog(ProgressDialog progressDialog) {
	 * progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	 * progressDialog.setMessage("処理を実行しています");
	 * progressDialog.setCancelable(true); }
	 */
}
