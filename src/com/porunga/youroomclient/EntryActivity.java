package com.porunga.youroomclient;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.porunga.youroomclient.RoomActivity.YouRoomEntryAdapter.DownloadImageTask;

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

		ImageButton postButton = (ImageButton) findViewById(R.id.post_button);
		// postButton.setText(getString(R.string.post_button));
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
					contentsDialogUtil.showContentsDialog(item, roomId);
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
		private Activity activity;

		public YouRoomChildEntryAdapter(Activity activity, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(activity, textViewResourceId, items);
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.activity = activity;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.entry_list_item, null);
			}
			YouRoomEntry roomEntry = (YouRoomEntry) this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView createdTime = null;
			ImageView level = null;
			// TextView level = null;
			ImageView memberImageView = null;
			TextView attachmentType = null;

			if (roomEntry != null) {
				name = (TextView) view.findViewById(R.id.name);
				createdTime = (TextView) view.findViewById(R.id.update_time);
				content = (TextView) view.findViewById(R.id.content);
				level = (ImageView) view.findViewById(R.id.level);
				// level = (TextView) view.findViewById(R.id.level);
				memberImageView = (ImageView) view.findViewById(R.id.member_image);
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

			if (level != null) {
				switch (roomEntry.getLevel()) {
				case 1:
					level.setImageResource(R.drawable.level_image1);
					break;
				case 2:
					level.setImageResource(R.drawable.level_image2);
					break;
				case 3:
					level.setImageResource(R.drawable.level_image3);
					break;
				case 4:
					level.setImageResource(R.drawable.level_image4);
					break;
				case 5:
					level.setImageResource(R.drawable.level_image5);
					break;
				case 6:
					level.setImageResource(R.drawable.level_image6);
					break;
				default:
					level.setImageResource(R.drawable.transecate_background);
				}
				// String commentLevel = "";
				//
				// for (int i = 0; i < roomEntry.getLevel(); i++){
				// commentLevel += "> ";
				// level.setText(commentLevel);
				// }
			}

			if (memberImageView != null) {
				memberImageView.setImageResource(R.drawable.default_member_image);
				String participationId = roomEntry.getParticipationId();
				memberImageView.setTag(participationId);
				DownloadImageTask downloadImageTask = new DownloadImageTask(memberImageView, activity);
				downloadImageTask.execute(roomId, participationId);
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
					createdTime.setTextColor(Color.RED);
				}
			}

			return view;
		}
		
		public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
			private ImageView memberImage;
			private Activity activity;
			private boolean[] errFlg = { false };
			private String tag;

			public DownloadImageTask(ImageView memberImage, Activity activity) {
				this.memberImage = memberImage;
				this.activity = activity;
				this.tag = memberImage.getTag().toString();
			}

			@Override
			protected Bitmap doInBackground(String... params) {

				YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
				Bitmap image;
				synchronized (activity.getBaseContext()) {
					try {
						image = proxy.getMemberImage(params[0], params[1], errFlg);
					} catch (Exception e) {
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
