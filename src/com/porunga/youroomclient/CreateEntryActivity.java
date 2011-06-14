package com.porunga.youroomclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CreateEntryActivity extends Activity implements OnClickListener {
	private static final String POST_OK = "201";
	private static final int MAX_CHAR_NUM = 280;
	protected boolean postable = false;
	private EditText entryContentText;
	protected ProgressDialog dialog;
	private LinearLayout postLayout;
	private String unpostable_message = "";
	private String action;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_entry);

		postLayout = (LinearLayout) findViewById(R.id.post_layout);
		postLayout.setOnClickListener(this);

		entryContentText = (EditText) findViewById(R.id.entry_content);

		Intent intent = getIntent();
		action = intent.getStringExtra("action");
		YouRoomEntry item = (YouRoomEntry) intent.getSerializableExtra("youRoomEntry");

		TextView numberOfCharacters = (TextView) findViewById(R.id.number_of_characters);
		if (action.equals("edit")) {
			entryContentText.setText(item.getContent());

			int charactersRest = MAX_CHAR_NUM - entryContentText.getText().length();
			numberOfCharacters.setTextColor(Color.WHITE);
			postable = true;
			numberOfCharacters.setText(String.valueOf(charactersRest));

		} else {
			numberOfCharacters.setText(String.valueOf(MAX_CHAR_NUM));
			unpostable_message = getString(R.string.content_empty);
		}
		entryContentText.addTextChangedListener(new UITextWatcher());
	}

	@Override
	public void onClick(View v) {
		String entryContent = entryContentText.getText().toString();

		if (postable) {
			PostEntryTask post = new PostEntryTask(this);
			post.execute(entryContent);
		} else {
			Toast.makeText(getBaseContext(), unpostable_message, Toast.LENGTH_SHORT).show();
		}
	}

	public class PostEntryTask extends AsyncTask<String, Void, String> {
		private Activity activity;
		private ProgressDialog progressDialog;
		private String roomId;
		private String parentId;
		private String rootId;

		public PostEntryTask(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(activity);
			progressDialog.setMessage(getString(R.string.now_posting));
			progressDialog.show();

			Intent intent = getIntent();
			roomId = intent.getStringExtra("roomId");
			YouRoomEntry item = (YouRoomEntry) intent.getSerializableExtra("youRoomEntry");
			rootId = intent.getStringExtra("rootId");

			parentId = String.valueOf(item.getId());
			if (parentId.equals("-1"))
				parentId = null;

		}

		@Override
		protected String doInBackground(String... entryContents) {
			YouRoomCommandProxy proxy = new YouRoomCommandProxy(activity);
			String status = proxy.postEntry(roomId, parentId, entryContents[0], rootId, action);
			return status;
		}

		@Override
		protected void onPostExecute(String status) {
			progressDialog.dismiss();

			if (POST_OK.equals(status)) {
				entryContentText.setText("");
				Toast.makeText(getBaseContext(), getString(R.string.post_ok), Toast.LENGTH_SHORT).show();
				finish();
			} else
				Toast.makeText(getBaseContext(), getString(R.string.post_ng), Toast.LENGTH_SHORT).show();
			// Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
		}
	}

	private class UITextWatcher implements TextWatcher {
		public void afterTextChanged(Editable arg) {
			TextView numberOfCharacters = (TextView) findViewById(R.id.number_of_characters);
			int charactersRest = MAX_CHAR_NUM - arg.length();
			if (arg.length() == 0) {
				numberOfCharacters.setTextColor(Color.RED);
				postable = false;
				unpostable_message = getString(R.string.content_empty);
			} else if (charactersRest < 0) {
				numberOfCharacters.setTextColor(Color.RED);
				postable = false;
				unpostable_message = getString(R.string.over_char_num);
			} else {
				numberOfCharacters.setTextColor(Color.WHITE);
				postable = true;
			}
			numberOfCharacters.setText(String.valueOf(charactersRest));
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	}

}
