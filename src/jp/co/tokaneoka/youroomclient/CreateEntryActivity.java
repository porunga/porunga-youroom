package jp.co.tokaneoka.youroomclient;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateEntryActivity extends Activity {
	private static final String POST_OK = "201";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_entry);

		Button postButton = (Button) findViewById(R.id.post_button);
		postButton.setText(getString(R.string.post_button));

		postButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getIntent();
				String roomId = intent.getStringExtra("roomId");
				YouRoomEntry item = (YouRoomEntry)intent.getSerializableExtra("youRoomEntry");
				
				String parentId = String.valueOf(item.getId());
				if(parentId.equals("-1"))
					parentId=null;
				
				EditText entryContentText = (EditText) findViewById(R.id.entry_content);
				String entryContent = entryContentText.getText().toString();
				// TODO Auto-generated method stub
			
				YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());
				HashMap<String, String> oAuthTokenMap = youRoomUtil
						.getOauthTokenFromLocal();
				YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);

				String status = youRoomCommand.createEntry(roomId, parentId, entryContent);
				if (status.equals(POST_OK)) {
					entryContentText.setText("");
					Toast.makeText(getBaseContext(), getString(R.string.post_ok), Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getBaseContext(), getString(R.string.post_ng), Toast.LENGTH_SHORT).show();
//					Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
				finish();
			}
		});

	}
	

}
