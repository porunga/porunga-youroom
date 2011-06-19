package com.porunga.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RoomActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	private String roomId;
	YouRoomEntryAdapter adapter;
	private ListView listView;
	private int page = 1;
	// private TextView footerView;

	private View footerView;
	private TextView emptyView;
	private ImageButton reloadButton;
	// The maximum number of entries from youRoom API
	private final int MAX_ROOM_COUNT = 10;
	private final int FOOTER_MIN_HEIGHT = 65;
	private final int REACQUIRE_ROOM = 1;

	private MainHandler handler = new MainHandler();
	private ContentsDialogUtil contentsDialogUtil;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.room_list);

		ImageButton postButton = (ImageButton) findViewById(R.id.post_button);
		postButton.setOnClickListener(this);

		reloadButton = (ImageButton) findViewById(R.id.reload_button);
		reloadButton.setOnClickListener(this);

		listView = (ListView) findViewById(R.id.listView1);
		footerView = LayoutInflater.from(this).inflate(R.layout.footer_layout, null);
		TextView footerText = (TextView) footerView.findViewById(R.id.footer_text_view);
		footerText.setText(getString(R.string.read_more));
		footerText.setTextColor(Color.LTGRAY);
		// footerView.setMinimumHeight(FOOTER_MIN_HEIGHT);

		ProgressBar progress = (ProgressBar) footerView.findViewById(R.id.progbar);
		progress.setIndeterminate(true);
		progress.setVisibility(View.GONE);
		// footerView = new TextView(this);
		// footerView.setText("もっと読む");
		// footerView.setGravity(Gravity.CENTER);
		// footerView.setTextColor(Color.LTGRAY);
		// footerView.setMinHeight(FOOTER_MIN_HEIGHT);
		// footerView.setVisibility(View.GONE);
		listView.addFooterView(footerView);
		emptyView = new TextView(this);
		emptyView.setMinHeight(FOOTER_MIN_HEIGHT);
		emptyView.setVisibility(View.GONE);
		listView.addFooterView(emptyView);

		contentsDialogUtil = new ContentsDialogUtil(this, new YouRoomCommandProxy(this), handler);
		((AppHolder) getApplication()).setDirty(roomId, true);

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
		Intent intent = getIntent();
		roomId = intent.getStringExtra("roomId");

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("page", String.valueOf(page));
		YouRoomCommandProxy proxy = new YouRoomCommandProxy(this);
		ArrayList<YouRoomEntry> dataList = proxy.getRoomEntryListFromCache(roomId, parameterMap);

		adapter = new YouRoomEntryAdapter(this, R.layout.room_list_item, dataList);
		listView.setAdapter(adapter);

		if (dataList.size() < MAX_ROOM_COUNT) {
			listView.removeFooterView(footerView);
			listView.removeFooterView(emptyView);
		}

		GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap, activity);
		task.execute();
		page++;

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view == footerView) {
					Map<String, String> parameterMap = new HashMap<String, String>();
					parameterMap.put("page", String.valueOf(page));
					// footerView.setMinimumHeight(FOOTER_MIN_HEIGHT);
					ProgressBar progress = (ProgressBar) footerView.findViewById(R.id.progbar);
					progress.setVisibility(View.VISIBLE);
					TextView footerText = (TextView) footerView.findViewById(R.id.footer_text_view);
					footerText.setText(getBaseContext().getString(R.string.now_loading));
					// footerView.setVisibility(View.GONE);
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
						intent.putExtra("action", "create");
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
						UserSession session = UserSession.getInstance();
						String roomAccessTime = session.getRoomAccessTime(roomId);
						Intent intent = new Intent(getApplication(), EntryActivity.class);
						if (roomAccessTime != null) {
							int compareResult = YouRoomUtil.calendarCompareTo(roomAccessTime, item.getUpdatedTime());
							if (compareResult < 0) {
								intent.putExtra("update_flag", true);
							}

						}
						intent.putExtra("roomId", String.valueOf(roomId));
						intent.putExtra("youRoomEntry", item);
						startActivity(intent);
					}
				}
			}
		});

		// listView.setOnScrollListener(new OnScrollListener() {
		// private int mark = 0;
		//
		// @Override
		// public void onScroll(AbsListView view, int firstVisibleItem, int
		// visibleItemCount, int totalItemCount) {
		//
		// if ((totalItemCount - visibleItemCount) == firstVisibleItem &&
		// totalItemCount > mark && totalItemCount > MAX_ROOM_COUNT) {
		// mark = totalItemCount;
		// Map<String, String> parameterMap = new HashMap<String, String>();
		// parameterMap.put("page", String.valueOf(page));
		// // footerView.setMinimumHeight(FOOTER_MIN_HEIGHT);
		// ProgressBar progress =
		// (ProgressBar)footerView.findViewById(R.id.progbar);
		// progress.setVisibility(View.VISIBLE);
		// TextView footerText =
		// (TextView)footerView.findViewById(R.id.footer_text_view);
		// footerText.setText(getBaseContext().getString(R.string.now_loading));
		// // footerView.setVisibility(View.GONE);
		// GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap,
		// activity);
		// task.execute();
		// page++;
		// }
		// }
		//
		// @Override
		// public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// }
		// });

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
				if (item == null)
					return false;
				// String entryId = String.valueOf(item.getId());
				// SQLiteDatabase cacheDb = ((AppHolder)
				// getApplication()).getCacheDb();
				// cacheDb.execSQL("delete from entries where entryId = ? and roomId = ? ;",
				// new String[] { entryId, roomId });
				contentsDialogUtil.showContentsDialog(item, roomId);
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
			page = 1;
			Map<String, String> parameterMap = new HashMap<String, String>();
			parameterMap.put("page", String.valueOf(page));
			ProgressBar progress = (ProgressBar) footerView.findViewById(R.id.progbar);
			progress.setVisibility(View.VISIBLE);
			TextView footerText = (TextView) footerView.findViewById(R.id.footer_text_view);
			footerText.setText(getBaseContext().getString(R.string.now_loading));
			// footerView.setMinimumHeight(FOOTER_MIN_HEIGHT);
			// footerView.setVisibility(View.GONE);
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
			ImageView memberImageView = new ImageView(getBaseContext());

			if (roomEntry != null) {
				name = (TextView) view.findViewById(R.id.name);
				createdTime = (TextView) view.findViewById(R.id.created_time);
				content = (TextView) view.findViewById(R.id.content);
				attachmentType = (TextView) view.findViewById(R.id.attachment_type);
				memberImageView = (ImageView) view.findViewById(R.id.member_image);
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

			if (memberImageView != null) {
				memberImageView.setImageResource(R.drawable.default_member_image);
				String participationId = roomEntry.getParticipationId();
				memberImageView.setTag(participationId);

				byte[] data = roomEntry.getMemberImage();
				Bitmap memberImageBitmap = null;

				if (data != null) {
					memberImageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					memberImageView.setImageBitmap(memberImageBitmap);
				} else {
					DownloadImageTask downloadImageTask = new DownloadImageTask(memberImageView, activity);
					downloadImageTask.execute(roomEntry);
				}
			}

			descendantsCount = (TextView) view.findViewById(R.id.descendants_count);
			int count = roomEntry.getDescendantsCount();
			if (count == -1) {
				descendantsCount.setText("");
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

		public class DownloadImageTask extends AsyncTask<YouRoomEntry, Void, Bitmap> {
			private ImageView memberImage;
			private Activity activity;
			private YouRoomEntry roomEntry;
			private boolean[] errFlg = { false };
			private String tag;

			public DownloadImageTask(ImageView memberImage, Activity activity) {
				this.memberImage = memberImage;
				this.activity = activity;
				this.tag = memberImage.getTag().toString();
			}

			@Override
			protected Bitmap doInBackground(YouRoomEntry... params) {

				YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
				Bitmap image = null;
				roomEntry = params[0];
				String roomId = roomEntry.getRoomId();
				String participationId = roomEntry.getParticipationId();
				synchronized (activity.getBaseContext()) {
					try {
						image = proxy.getMemberImageFromCache(roomId, participationId);
						if (image == null) {
							image = proxy.getMemberImage(roomId, participationId, errFlg);
						}
						roomEntry.setMemberImage(image);
					} catch (Exception e) {
						e.printStackTrace();
						errFlg[0] = true;
						image = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
					}
					return image;
				}
			}

			@Override
			protected void onPostExecute(Bitmap image) {
				if (errFlg[0]) {
					Toast.makeText(getBaseContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				}
				if (tag.equals(memberImage.getTag().toString()))
					this.memberImage.setImageBitmap(image);
				this.memberImage.setVisibility(View.VISIBLE);
			}
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

	public class GetRoomEntryTask extends AsyncTask<Void, Void, ArrayList<YouRoomEntry>> {
		private String roomId;
		private Map<String, String> parameterMap;
		private Activity activity;
		private boolean[] errFlg = { false };
		private ProgressDialog progressDialog;

		public GetRoomEntryTask(String roomId, Map<String, String> parameterMap, Activity activity) {
			this.roomId = roomId;
			this.parameterMap = parameterMap;
			this.activity = activity;
		}

		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			reloadButton.setImageResource(R.drawable.unclickable_reload_image);
			reloadButton.setClickable(false);
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
			} else {
				if (page == 2)
					adapter.clear();
				for (YouRoomEntry youRoomEntry : dataList) {
					adapter.add(youRoomEntry);
				}
				adapter.notifyDataSetChanged();
			}
			if (adapter.getCount() < MAX_ROOM_COUNT) {
				// footerView.setMinimumHeight(0);
				switch (listView.getFooterViewsCount()) {
				case 1:
					listView.removeFooterView(emptyView);
					break;
				case 2:
					listView.removeFooterView(footerView);
					listView.removeFooterView(emptyView);
					break;
				default:

				}
			} else if (adapter.getCount() >= MAX_ROOM_COUNT && dataList.size() < MAX_ROOM_COUNT) {
				listView.removeFooterView(footerView);
			} else {
				ProgressBar progress = (ProgressBar) footerView.findViewById(R.id.progbar);
				progress.setVisibility(View.GONE);
				TextView footerText = (TextView) footerView.findViewById(R.id.footer_text_view);
				footerText.setText(activity.getString(R.string.read_more));
				footerView.setVisibility(View.VISIBLE);

				switch (listView.getFooterViewsCount()) {
				case 0:
					listView.addFooterView(footerView);
					listView.addFooterView(emptyView);
					break;
				case 1:
					listView.removeFooterView(footerView);
					break;
				default:

				}
				listView.invalidate();

			}
			((AppHolder) getApplication()).setDirty(roomId, false);

			setProgressBarIndeterminateVisibility(false);
			reloadButton.setImageResource(R.drawable.reload_image);
			reloadButton.setClickable(true);
		}
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("処理を実行しています");
		progressDialog.setCancelable(true);
	}

	@Override
	public void onClick(View view) {

		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.post_button:
			Intent intent = new Intent(getApplication(), CreateEntryActivity.class);
			intent.putExtra("action", "create");
			intent.putExtra("roomId", String.valueOf(roomId));
			intent.putExtra("youRoomEntry", new YouRoomEntry());
			startActivity(intent);
			break;

		case R.id.reload_button:
			reloadList();
			break;
		}
	}

	private void reloadList() {
		((AppHolder) getApplication()).setDirty(roomId, true);
		page = 1;
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("page", String.valueOf(page));

		((AppHolder) getApplication()).setDirty(roomId, true);
		GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap, this);
		task.execute();
		page++;
	}

	private void destroyEntry(String[] params) {
		DestroyEntryTask task = new DestroyEntryTask(this);
		task.execute(params);
	}

	public class DestroyEntryTask extends AsyncTask<String, Void, String> {
		private Activity activity;
		private ProgressDialog progressDialog;

		public DestroyEntryTask(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(activity);
			progressDialog.setMessage(getString(R.string.now_deleting));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
			String status = "";
			try {
				status = proxy.destroyEntry(params[0], params[1], params[2]);
			} catch (YouRoomServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return status;
		}

		@Override
		protected void onPostExecute(String status) {
			progressDialog.dismiss();
			// Toast.makeText(getBaseContext(), status,
			// Toast.LENGTH_SHORT).show();
			handler.sendEmptyMessage(YouRoomUtil.RELOAD);
		}
	}

	private class MainHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case YouRoomUtil.RELOAD: {
				reloadList();
				break;
			}
			case YouRoomUtil.EDIT: {
				Intent intent = new Intent(getApplication(), CreateEntryActivity.class);
				intent.putExtra("action", "edit");
				intent.putExtra("roomId", String.valueOf(roomId));
				intent.putExtra("youRoomEntry", (YouRoomEntry) msg.obj);
				startActivity(intent);
				break;
			}
			case YouRoomUtil.DELETE: {
				String[] params = (String[]) msg.obj;
				destroyEntry(params);

				break;
			}
			}
		}
	}
}
