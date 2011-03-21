package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.co.tokaneoka.youroomclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RoomActivity extends Activity {
    /** Called when the activity is first created. */
	
	private String roomId;
	YouRoomEntryAdapter adapter;
	ProgressDialog progressDialog;
	private ListView listView;
	private int page = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.main);
		
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
		listView = (ListView)findViewById(R.id.listView1);

		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("処理を実行中しています");
		progressDialog.setCancelable(true);
		progressDialog.show();
		
	   	Map<String, String> parameterMap = new HashMap<String, String>();
    	parameterMap.put("page", String.valueOf(page));
		GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap);
		task.execute();
		page ++;
		
		final TextView textview = new TextView(this);
		textview.setText("-----読み込み-----");
		textview.setMinHeight(50);
		textview.setBackgroundColor(Color.WHITE);
				
		listView.addFooterView(textview);

		adapter = new YouRoomEntryAdapter(this, R.layout.room_list_item, dataList);
		listView.setAdapter(adapter);
    		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(view == textview){
					progressDialog.show();
				   	Map<String, String> parameterMap = new HashMap<String, String>();
			    	parameterMap.put("page", String.valueOf(page));
					GetRoomEntryTask task = new GetRoomEntryTask(roomId, parameterMap);
					task.execute();
					page ++;
				} else {
					ListView listView = (ListView) parent;
					YouRoomEntry item = (YouRoomEntry) listView.getItemAtPosition(position);
					Intent intent = new Intent(getApplication(), EntryActivity.class);
					intent.putExtra("roomId", String.valueOf(roomId) );
					intent.putExtra("youRoomEntry", item);
					startActivity(intent);
				}
			}
		});
        
    }
        
	@Override
	public void onStart(){
		super.onStart();
	}
	
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomEntryAdapter extends ArrayAdapter<YouRoomEntry> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomEntry> items;
		
		public YouRoomEntryAdapter( Context context, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.room_list_item, null);				
			}
			YouRoomEntry roomEntry = (YouRoomEntry)this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView createdTime = null;
			TextView descendantsCount = null;
			
			if ( roomEntry != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				createdTime = (TextView)view.findViewById(R.id.textView2);
				content = (TextView)view.findViewById(R.id.textView3);
			}
			if ( name != null ){
				name.setText(roomEntry.getParticipationName());
			}
			if ( createdTime != null ){
				createdTime.setText(roomEntry.getCreatedTime());
			}
			if ( content != null ){
				content.setText(roomEntry.getContent());
			}

			descendantsCount = (TextView)view.findViewById(R.id.textView4);
			int count = roomEntry.getDescendantsCount();
			if ( count == -1 ) {
				GetEntryTask task = new GetEntryTask(descendantsCount, roomId, roomEntry);
				task.execute(roomEntry.getId());
			} else {
				//TODO レイアウト修正直書き
				descendantsCount.setText("[ " + count + "comments ] > ");
			}
			return view;
		}
	}
	
	public class GetEntryTask extends AsyncTask<Integer, Void, String> {
		
		private String roomId;
		String count;
		private TextView textView;
		private YouRoomEntry roomEntry;
		
		public GetEntryTask(TextView textView, String roomId, YouRoomEntry roomEntry){
			this.roomId = roomId;
			this.roomEntry = roomEntry;
			this.textView = textView;
		}

		@Override
		protected String doInBackground(Integer... entryIds) {
			
			YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
			HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
			YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
			String entry = youRoomCommand.getEntry(roomId, String.valueOf(entryIds[0]));
			
			try {
				JSONObject json = new JSONObject(entry);    			
		    	count = json.getJSONObject("entry").getString("descendants_count");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			roomEntry.setDescendantsCount(Integer.valueOf(count));
			return count;
		}
		
		@Override
		protected void onPostExecute(String count){
			//TODO レイアウト修正直書き
			textView.setText("[ " + count + "comments ] > ");
		}
	}
	
	private ArrayList<YouRoomEntry> getRoomEntry(String roomId, Map<String, String> parameterMap){
		
		YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
		HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
		YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
		String roomTL = "";
		roomTL= youRoomCommand.getRoomTimeLine(roomId, parameterMap);
		
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		
		try {
			JSONArray jsons = new JSONArray(roomTL);
			for(int i =0 ; i< jsons.length(); i++){
				YouRoomEntry roomEntry = new YouRoomEntry();
				JSONObject jObject = jsons.getJSONObject(i);
				JSONObject entryObject = jObject.getJSONObject("entry");

				int id = entryObject.getInt("id");
				String participationName = entryObject.getJSONObject("participation").getString("name");
				String content = entryObject.getString("content");
    		    
				String formattedTime = "";
				String unformattedTime = entryObject.getString("created_at");
    		    	
				formattedTime = YouRoomUtil.convertDatetime(unformattedTime);				
				
				roomEntry.setId(id);
				roomEntry.setParticipationName(participationName);
				roomEntry.setCreatedTime(formattedTime);
				roomEntry.setContent(content);
    		    	
				dataList.add(roomEntry);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataList;
	}
	
	public class GetRoomEntryTask extends AsyncTask<Void, Void, ArrayList<YouRoomEntry>> {
		
		private String roomId;
		private Map<String, String> parameterMap;
		public GetRoomEntryTask(String roomId, Map<String, String> parameterMap){
			this.roomId = roomId;
			this.parameterMap = parameterMap;
		}
		
		@Override
		protected ArrayList<YouRoomEntry> doInBackground(Void... ids) {						
			ArrayList<YouRoomEntry> dataList = getRoomEntry(roomId, parameterMap);
			return dataList;
		}
				
		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataList){
			int count = adapter.getCount();
			Iterator iterator = dataList.iterator();
			while( iterator.hasNext() ) {
				adapter.add((YouRoomEntry) iterator.next());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(count);
			progressDialog.dismiss();
		}
	}
	
}

