package com.porunga.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RoomActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	private String roomId;
	YouRoomEntryAdapter adapter;
	ProgressDialog progressDialog;
	private ListView listView;
	private int page = 1;
	private TextView footerView;
	// The maximum number of entries from youRoom API
	private final int MAX_ROOM_COUNT = 10;
	private final int FOOTER_MIN_HEIGHT = 65;
	private final int REACQUIRE_ROOM = 1;

	private ContentsDialogUtil contentsDialogUtil = new ContentsDialogUtil(this);

	// private YouRoomUtil youRoomUtil = new YouRoomUtil(this);

	// private YouRoomGroup group;
	// private EditText entryContentText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.room_list);

		Button postButton = (Button) findViewById(R.id.post_button);
		postButton.setText(getString(R.string.post_button));
		postButton.setOnClickListener(this);

		Intent intent = getIntent();
		roomId = intent.getStringExtra("roomId");
		listView = (ListView) findViewById(R.id.listView1);

		footerView = new TextView(this);
		footerView.setText("もっと読む");
		footerView.setGravity(Gravity.CENTER);
		footerView.setTextColor(Color.LTGRAY);
		listView.addFooterView(footerView);

		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		adapter = new YouRoomEntryAdapter(this, R.layout.room_list_item, dataList);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		page = 1;
		adapter.clear();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onStart() {
		super.onStart();
		final Activity activity = this;

		progressDialog = new ProgressDialog(this);
		setProgressDialog(progressDialog);
		progressDialog.show();

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("page", String.valueOf(page));
		footerView.setMinHeight(FOOTER_MIN_HEIGHT);
		footerView.setVisibility(View.GONE);
		GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap, activity);
		task.execute();
		page++;

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view == footerView) {
					progressDialog.show();
					Map<String, String> parameterMap = new HashMap<String, String>();
					parameterMap.put("page", String.valueOf(page));
					footerView.setMinHeight(FOOTER_MIN_HEIGHT);
					footerView.setVisibility(View.GONE);
					GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap, activity);
					task.execute();
					page++;
				} else {
					ListView listView = (ListView) parent;
					YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
					if (item.getDescendantsCount() == -1) {
						Toast.makeText(getApplication(), "読み込み中です。もう少しおまちください。", Toast.LENGTH_SHORT).show();

					} else if (item.getDescendantsCount() == 0) {
						Intent intent = new Intent(getApplication(), CreateEntryActivity.class);
						intent.putExtra("roomId", String.valueOf(roomId));
						intent.putExtra("youRoomEntry", item);

						startActivity(intent);
					} else {

						/*
						 * UserSession session = UserSession.getInstance();
						 * String lastAccessTime =
						 * youRoomUtil.getRoomAccessTime(roomId);
						 * session.setRoomAccessTime(roomId, lastAccessTime);
						 * String time = YouRoomUtil.getRFC3339FormattedTime();
						 * youRoomUtil.storeRoomAccessTime(roomId, time);
						 */

						Intent intent = new Intent(getApplication(), EntryActivity.class);
						intent.putExtra("roomId", String.valueOf(roomId));
						intent.putExtra("youRoomEntry", item);
						startActivity(intent);
					}
				}
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
				if (item == null)
					return false;
//				String entryId = String.valueOf(item.getId());
//				SQLiteDatabase cacheDb = ((AppHolder) getApplication()).getCacheDb();
//				cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[] { entryId, roomId });
				contentsDialogUtil.showContentsDialog(item,roomId);
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, REACQUIRE_ROOM, REACQUIRE_ROOM, R.string.reacquire_room);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case REACQUIRE_ROOM:
			progressDialog = new ProgressDialog(this);
			setProgressDialog(progressDialog);
			progressDialog.show();
			adapter.clear();
			page = 1;
			Map<String, String> parameterMap = new HashMap<String, String>();
			parameterMap.put("page", String.valueOf(page));
			footerView.setMinHeight(FOOTER_MIN_HEIGHT);
			footerView.setVisibility(View.GONE);
			((AppHolder) getApplication()).setDirty(roomId, true);
			GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap, this);
			task.execute();
			page++;
			ret = true;
			break;
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		}
		return ret;
	}

	// ListViewカスタマイズ用のArrayAdapter
	public class YouRoomEntryAdapter extends ArrayAdapter<YouRoomEntry> {
		private LayoutInflater inflater;
		private Activity activity;

		public YouRoomEntryAdapter(Activity activity, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(activity, textViewResourceId, items);
			this.activity = activity;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.room_list_item, null);
			}

			YouRoomEntry roomEntry = (YouRoomEntry) this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView createdTime = null;
			TextView descendantsCount = null;
			TextView attachmentType = null;

			if (roomEntry != null) {
				name = (TextView) view.findViewById(R.id.name);
				createdTime = (TextView) view.findViewById(R.id.created_time);
				content = (TextView) view.findViewById(R.id.content);
				attachmentType = (TextView) view.findViewById(R.id.attachment_type);
			}
			if (name != null) {
				name.setText(roomEntry.getParticipationName());
			}
			if (createdTime != null) {
				createdTime.setTextColor(Color.LTGRAY);
				createdTime.setText(YouRoomUtil.convertDatetime(roomEntry.getCreatedTime()));
			}
			if (content != null) {
				content.setText(roomEntry.getContent());
			}

			String type = roomEntry.getAttachmentType();

			if (attachmentType != null) {
				if (type.equals("")) {
					attachmentType.setText("");
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append(getString(R.string.display_attachment));
					if (type.equals("Text"))
						sb.append(getString(R.string.attachment_type_text));
					if (type.equals("Image"))
						sb.append(getString(R.string.attachment_type_image));
					if (type.equals("File"))
						sb.append(getString(R.string.attachment_type_file));
					if (type.equals("Link"))
						sb.append(getString(R.string.attachment_type_link));

					attachmentType.setText(sb.toString());
				}
			}

			descendantsCount = (TextView) view.findViewById(R.id.descendants_count);
			int count = roomEntry.getDescendantsCount();
			if (count == -1) {
				GetEntryTask task = new GetEntryTask(descendantsCount, roomId, activity);
				task.execute(roomEntry);
			} else {
				// TODO レイアウト修正直書き
				descendantsCount.setText("[ " + count + "comments ] > ");
			}

			UserSession session = UserSession.getInstance();
			String roomAccessTime = session.getRoomAccessTime(roomId);
			if (roomAccessTime != null) {
				int compareResult = YouRoomUtil.calendarCompareTo(roomAccessTime, roomEntry.getUpdatedTime());
				if (compareResult < 0) {
					createdTime.setTextColor(Color.RED);
				}
			}

			return view;
		}
	}

	public class GetEntryTask extends AsyncTask<YouRoomEntry, Void, Integer> {
		private String roomId;
		private TextView textView;
		private Activity activity;
		private boolean[] errFlg = { false };

		public GetEntryTask(TextView textView, String roomId, Activity activity) {
			this.roomId = roomId;
			this.textView = textView;
			this.activity = activity;
		}

		@Override
		protected Integer doInBackground(YouRoomEntry... entries) {
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
			YouRoomEntry entry = proxy.getEntry(roomId, String.valueOf(entries[0].getId()), entries[0].getUpdatedTime(), errFlg);
			entries[0].setDescendantsCount(entry.getDescendantsCount());
			return entry.getDescendantsCount();
		}

		@Override
		protected void onPostExecute(Integer count) {
			if (errFlg[0]) {
				Toast.makeText(getBaseContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
			// TODO レイアウト修正直書き
			textView.setText("[ " + count.toString() + "comments ] > ");
		}
	}

	// private ArrayList<YouRoomEntry> getRoomEntryList(String roomId,
	// Map<String, String> parameterMap) {
	//
	// YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
	// HashMap<String, String> oAuthTokenMap =
	// youRoomUtil.getOauthTokenFromLocal();
	// YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
	// String roomTL = "";
	// roomTL = youRoomCommand.getRoomTimeLine(roomId, parameterMap);
	//
	// ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
	//
	// try {
	// JSONArray jsons = new JSONArray(roomTL);
	// for (int i = 0; i < jsons.length(); i++) {
	// YouRoomEntry roomEntry = new YouRoomEntry();
	// JSONObject jObject = jsons.getJSONObject(i);
	// JSONObject entryObject = jObject.getJSONObject("entry");
	//
	// int id = entryObject.getInt("id");
	// String participationName =
	// entryObject.getJSONObject("participation").getString("name");
	// String content = entryObject.getString("content");
	//
	// String createdTime = entryObject.getString("created_at");
	// String updatedTime = entryObject.getString("updated_at");
	//
	// roomEntry.setId(id);
	// roomEntry.setUpdatedTime(updatedTime);
	// roomEntry.setParticipationName(participationName);
	// roomEntry.setCreatedTime(createdTime);
	// roomEntry.setContent(content);
	//
	// dataList.add(roomEntry);
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return dataList;
	// }

	public class GetRoomEntryTask extends AsyncTask<Void, Void, ArrayList<YouRoomEntry>> {
		private String roomId;
		private Map<String, String> parameterMap;
		private Activity activity;
		private boolean[] errFlg = { false };

		public GetRoomEntryTask(String roomId, Map<String, String> parameterMap, Activity activity) {
			this.roomId = roomId;
			this.parameterMap = parameterMap;
			this.activity = activity;
		}

		@Override
		protected ArrayList<YouRoomEntry> doInBackground(Void... ids) {
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
			ArrayList<YouRoomEntry> dataList = proxy.getRoomEntryList(roomId, parameterMap, errFlg);
			return dataList;
		}

		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataList) {
			if (errFlg[0]) {
				Toast.makeText(getBaseContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
			int count = adapter.getCount();
			// Iterator iterator = dataList.iterator();
			// while (iterator.hasNext()) {
			// adapter.add((YouRoomEntry) iterator.next());
			// }
			for (YouRoomEntry youRoomEntry : dataList) {
				adapter.add(youRoomEntry);
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(count);
			if (dataList.size() < MAX_ROOM_COUNT) {
				footerView.setMinHeight(0);
			} else {
				footerView.setVisibility(View.VISIBLE);
			}
			progressDialog.dismiss();
		}
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("処理を実行しています");
		progressDialog.setCancelable(true);
	}

	@Override
	public void onClick(View arg0) {

		// TODO Auto-generated method stub

		Intent intent = new Intent(getApplication(), CreateEntryActivity.class);
		intent.putExtra("roomId", String.valueOf(roomId));
		intent.putExtra("youRoomEntry", new YouRoomEntry());
		startActivity(intent);
	}
}
