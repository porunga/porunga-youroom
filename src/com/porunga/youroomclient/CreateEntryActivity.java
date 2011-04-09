package com.porunga.youroomclient;

import java.util.HashMap;

import com.porunga.youroomclient.R;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateEntryActivity extends Activity implements OnClickListener {
	private static final String POST_OK = "201";
	private static final int MAX_CHAR_NUM = 140;
	protected boolean postable = true;
	EditText entryContentText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_entry);

		Button postButton = (Button) findViewById(R.id.post_button);
		postButton.setText(getString(R.string.post_button));

		postButton.setOnClickListener(this);
		entryContentText = (EditText) findViewById(R.id.entry_content);
		entryContentText.addTextChangedListener(new UITextWatcher());

		TextView numberOfCharacters = (TextView) findViewById(R.id.number_of_characters);
		numberOfCharacters.setText(String.valueOf(MAX_CHAR_NUM));
	}

	@Override
	public void onClick(View v) {
		Intent intent = getIntent();
		String roomId = intent.getStringExtra("roomId");
		YouRoomEntry item = (YouRoomEntry) intent
				.getSerializableExtra("youRoomEntry");

		String parentId = String.valueOf(item.getId());
		if (parentId.equals("-1"))
			parentId = null;

		String entryContent = entryContentText.getText().toString();
		// TODO Auto-generated method stub

		YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
		HashMap<String, String> oAuthTokenMap = youRoomUtil
				.getOauthTokenFromLocal();
		YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
		if (postable) {
			String status = youRoomCommand.createEntry(roomId, parentId,
					entryContent);
			if (status.equals(POST_OK)) {
				entryContentText.setText("");
				Toast.makeText(getBaseContext(), getString(R.string.post_ok),
						Toast.LENGTH_SHORT).show();
				finish();
			} else
				Toast.makeText(getBaseContext(), getString(R.string.post_ng),
						Toast.LENGTH_SHORT).show();
			// Toast.makeText(this, status, Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(getBaseContext(), getString(R.string.over_char_num),
					Toast.LENGTH_SHORT).show();
		}

	}

	private class UITextWatcher implements TextWatcher {
		public void afterTextChanged(Editable arg) {
			TextView numberOfCharacters = (TextView) findViewById(R.id.number_of_characters);
			int charactersRest = MAX_CHAR_NUM - arg.length();
			if (charactersRest < 0) {
				numberOfCharacters.setTextColor(Color.RED);
				postable = false;
			} else {
				numberOfCharacters.setTextColor(Color.WHITE);
				postable = true;
			}
			numberOfCharacters.setText(String.valueOf(charactersRest));
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}
}
