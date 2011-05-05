package com.porunga.youroomclient;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupActivity extends Activity {

	private final int DELETE_TOKEN = 1;
	private final int REACQUIRE_GROUP = 2;
	private final int SETTING = 3;

	private YouRoomUtil youRoomUtil = new YouRoomUtil(this);
	private YouRoomGroupAdapter adapter;
	private ProgressDialog progressDialog;

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
			setContentView(R.layout.group_view);

			progressDialog = new ProgressDialog(this);
			setProgressDialog(progressDialog);
			progressDialog.show();

			ListView listView = (ListView) findViewById(R.id.listView1);
			ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();
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
					SQLiteDatabase cacheDb = ((AppHolder)getApplication()).getCacheDb();
					cacheDb.execSQL("delete from rooms where roomId = ?;",new String[] {roomId});
					return true;
				}
			});
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
			progressDialog = new ProgressDialog(this);
			setProgressDialog(progressDialog);
			progressDialog.show();
			adapter.clear();
			GetGroupTask task = new GetGroupTask(this);
			task.execute();
			((AppHolder)getApplication()).clearDirty();
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
		//private ArrayList<YouRoomGroup> items;

		public YouRoomGroupAdapter(Context context, int textViewResourceId, ArrayList<YouRoomGroup> items) {
			super(context, textViewResourceId, items);
			//this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				roomImage = (ImageView)view.findViewById(R.id.room_image);
				name = (TextView) view.findViewById(R.id.textView1);
				updateTime = (TextView) view.findViewById(R.id.textView2);
			}
			if (roomImage != null){
				byte[] data = group.getRoomImage();
				roomImage.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
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

//	private ArrayList<YouRoomGroup> getMyGroupList() {
//
//		// input
//		// output [YouRoomGroup...] or []
//
//		YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
//		HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
//		YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
//		String myGroups = youRoomCommand.getMyGroup();
//		ArrayList<YouRoomGroup> dataList = new ArrayList<YouRoomGroup>();
//
//		try {
//			JSONArray jsons = new JSONArray(myGroups);
//			for (int i = 0; i < jsons.length(); i++) {
//				YouRoomGroup group = new YouRoomGroup();
//				JSONObject jObject = jsons.getJSONObject(i);
//				JSONObject groupObject = jObject.getJSONObject("group");
//
//				int id = groupObject.getInt("id");
//				String name = groupObject.getString("name");
//
//				String createdTime = groupObject.getString("created_at");
//				String updatedTime = groupObject.getString("updated_at");
//
//				group.setId(id);
//				group.setName(name);
//				group.setUpdatedTime(updatedTime);
//				group.setCreatedTime(createdTime);
//
//				String roomId = String.valueOf(id);
//				String lastAccessTime = youRoomUtil.getRoomAccessTime(roomId);
//				String time;
//				if (lastAccessTime == null) {
//					time = youRoomUtil.getAccessTime();
//					if (time == null) { // ここに入ることはないはず。
//						time = YouRoomUtil.getRFC3339FormattedTime();
//					}
//					youRoomUtil.storeRoomAccessTime(roomId, time);
//				}
//
//				UserSession session = UserSession.getInstance();
//				roomId = String.valueOf(id);
//				lastAccessTime = youRoomUtil.getRoomAccessTime(roomId);
//				session.setRoomAccessTime(roomId, lastAccessTime);
//
//				dataList.add(group);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		// 暫定的なチェック
//		// String lastAccessTime = youRoomUtil.getAccessTime();
//		// UserSession session = UserSession.getInstance();
//		// session.setLastAccessTime(lastAccessTime);
//		// String currentTime = YouRoomUtil.getYesterdayFormattedTime();
//		String currentTime = YouRoomUtil.getRFC3339FormattedTime();
//		youRoomUtil.storeAccessTime(currentTime);
//
//		return dataList;
//	}

	public class GetGroupTask extends AsyncTask<Void, Void, ArrayList<YouRoomGroup>> {
		private Activity activity;
		private boolean[] errFlg = {false};
		
		public GetGroupTask(Activity activity) {
			this.activity = activity;
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
			}
//			Iterator iterator = dataList.iterator();
//			while (iterator.hasNext()) {
//				adapter.add((YouRoomGroup) iterator.next());
//			}
			for(YouRoomGroup group : dataList) {
				adapter.add(group);
			}
			adapter.notifyDataSetChanged();
			progressDialog.dismiss();
		}
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("処理を実行しています");
		progressDialog.setCancelable(true);
	}

}
