package com.porunga.youroomclient;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EntryActivity extends Activity implements OnClickListener {
	String roomId;
	YouRoomChildEntryAdapter adapter;
	ProgressDialog progressDialog;
	int parentEntryCount;
	int requestCount;
	Intent intent;
	String rootId;

	private final static int MAX_LEVEL = 6;
	private ContentsDialogUtil contentsDialogUtil = new ContentsDialogUtil(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entry_list);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onStart() {
		super.onStart();
		intent = getIntent();
		roomId = intent.getStringExtra("roomId");
		YouRoomEntry pseudYouRoomEntry = (YouRoomEntry) intent.getSerializableExtra("youRoomEntry");
		rootId = String.valueOf(pseudYouRoomEntry.getId());

		YouRoomCommandProxy proxy = new YouRoomCommandProxy(this);
		YouRoomEntry youRoomEntry = proxy.getEntry(roomId, rootId);

		Button postButton = (Button) findViewById(R.id.post_button);
		postButton.setText(getString(R.string.post_button));
		postButton.setOnClickListener(this);
		parentEntryCount = youRoomEntry.getDescendantsCount();

		// TODO if String decodeResult = "";
		ListView listView = (ListView) findViewById(R.id.listView1);

		// progressDialog = new ProgressDialog(this);
		// setProgressDialog(progressDialog);
		// progressDialog.show();

		int level = -1;
		youRoomEntry.setLevel(level);
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		adapter = new YouRoomChildEntryAdapter(this, R.layout.entry_list_item, dataList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
				if (item.getLevel() == MAX_LEVEL)
					Toast.makeText(getBaseContext(), getString(R.string.deps_max), Toast.LENGTH_SHORT).show();
				else {
					Intent intentCreateEntry = new Intent(getApplication(), CreateEntryActivity.class);
					intentCreateEntry.putExtra("roomId", String.valueOf(roomId));
					intentCreateEntry.putExtra("youRoomEntry", item);
					intentCreateEntry.putExtra("rootId", rootId);

					startActivity(intentCreateEntry);
				}
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
				if (item != null) {
					contentsDialogUtil.showContentsDialog(item,roomId);
					return true;
				} else
					return false;
			}
		});

		GetChildEntryTask task = new GetChildEntryTask();
		try {
			task.execute(youRoomEntry);
		} catch (RejectedExecutionException e) {
			// TODO
			// AsyncTaskでは内部的にキューを持っていますが、このキューサイズを超えるタスクをexecuteすると、ブロックされずに例外が発生します。らしいので、一旦握りつぶしている
			e.printStackTrace();
		}
	}

	// ListViewカスタマイズ用のArrayAdapter
	public class YouRoomChildEntryAdapter extends ArrayAdapter<YouRoomEntry> {
		private LayoutInflater inflater;

		public YouRoomChildEntryAdapter(Context context, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(context, textViewResourceId, items);
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.entry_list_item, null);
			}
			YouRoomEntry roomEntry = (YouRoomEntry) this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView updateTime = null;
			TextView level = null;
			TextView attachmentType = null;

			if (roomEntry != null) {
				name = (TextView) view.findViewById(R.id.name);
				updateTime = (TextView) view.findViewById(R.id.update_time);
				content = (TextView) view.findViewById(R.id.content);
				level = (TextView) view.findViewById(R.id.level);
				attachmentType = (TextView) view.findViewById(R.id.attachment_type);
			}
			if (name != null) {
				name.setText(roomEntry.getParticipationName());
			}
			if (updateTime != null) {
				updateTime.setTextColor(Color.LTGRAY);
				updateTime.setText(YouRoomUtil.convertDatetime(roomEntry.getUpdatedTime()));
			}
			if (content != null) {
				content.setText(roomEntry.getContent());
			}
			if (level != null) {
				String commentLevel = "";
				for (int i = 0; i < roomEntry.getLevel(); i++)
					commentLevel += "> ";
				level.setText(commentLevel);
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

			UserSession session = UserSession.getInstance();
			String roomAccessTime = session.getRoomAccessTime(roomId);
			if (roomAccessTime != null) {
				int compareResult = YouRoomUtil.calendarCompareTo(roomAccessTime, roomEntry.getUpdatedTime());
				if (compareResult < 0) {
					updateTime.setTextColor(Color.RED);
				}
			}

			return view;
		}
	}

	private void addChildEntries(ArrayList<YouRoomEntry> dataList, YouRoomEntry entry, int level) {
		entry.setLevel(level);
		dataList.add(entry);
		if (entry.getChildren() != null) {
			for (YouRoomEntry child : entry.getChildren()) {
				addChildEntries(dataList, child, level + 1);
			}
		}
	}

	public class GetChildEntryTask extends AsyncTask<YouRoomEntry, Void, ArrayList<YouRoomEntry>> {

		// private String roomId;
		private YouRoomEntry roomChildEntry;
		private Object objLock = new Object();

		public GetChildEntryTask(String roomId) {
			// this.roomId = roomId;
		}

		public GetChildEntryTask() {

		}

		@Override
		protected ArrayList<YouRoomEntry> doInBackground(YouRoomEntry... roomChildEntries) {
			ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
			roomChildEntries[0].setLevel(0);
			dataList.add(roomChildEntries[0]);
			for (YouRoomEntry child : roomChildEntries[0].getChildren()) {
				addChildEntries(dataList, child, 1);
			}

			return dataList;
		}

		// @Override
		// protected void onProgressUpdate(Integer... progress) {
		// progressDialog.setProgress(progress[0]);
		// }

		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataChildList) {
			synchronized (objLock) {
				if (dataChildList.size() > 0) {
					for (int i = 0; i < dataChildList.size(); i++) {
						adapter.insert(dataChildList.get(i), adapter.getPosition(roomChildEntry) + i + 1);
					}
				}
				requestCount++;
				// publishProgress(requestCount);
				Log.e("count", "requestCount = " + requestCount);
			}
			adapter.notifyDataSetChanged();
			// // 親が一回呼ばれるので+1
			// if (parentEntryCount <= requestCount + 1)
			// progressDialog.dismiss();
		}
	}

	// public void setProgressDialog(ProgressDialog progressDialog) {
	// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	// progressDialog.setMessage("処理を実行しています");
	// progressDialog.setIndeterminate(false);
	// progressDialog.setMax(parentEntryCount);
	// progressDialog.setCancelable(true);
	// }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		YouRoomEntry youRoomEntry = (YouRoomEntry) intent.getSerializableExtra("youRoomEntry");
		Intent intentCreateEntry = new Intent(getApplication(), CreateEntryActivity.class);
		intentCreateEntry.putExtra("roomId", String.valueOf(roomId));
		intentCreateEntry.putExtra("youRoomEntry", youRoomEntry);
		intentCreateEntry.putExtra("rootId", rootId);

		startActivity(intentCreateEntry);

	}

}
