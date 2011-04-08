package jp.co.tokaneoka.youroomclient;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import jp.co.tokaneoka.youroomclient.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	Handler mHandler = new Handler();
	boolean loginCheck = false;
	String email = "";
	String password = "";
	Intent intent;
	Toast toast;
	ProgressDialog dialog;
	YouRoomUtil youRoomUtil = new YouRoomUtil(this);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);

		Button button = (Button) findViewById(R.id.login_submit);
		button.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		Class distActivity = GroupActivity.class;
		EditText emailText = (EditText) findViewById(R.id.login_email);
		email = emailText.getText().toString();

		EditText passwordText = (EditText) findViewById(R.id.login_password);
		password = passwordText.getText().toString();
		intent = new Intent(this, distActivity);
		toast = Toast.makeText(this, "ログインに失敗しました", Toast.LENGTH_SHORT);

		dialog = new ProgressDialog(this);
		dialog.setMessage("ログインしています");
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.show();

		(new Thread(new Runnable() {
			@Override
			public void run() {
				loginCheck = Login(email, password);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (loginCheck) {
							// ログイン時間を起点とするため、ログイン時間を最終アクセス時間として記録
							String currentTime = YouRoomUtil
									.getRFC3339FormattedTime();
							youRoomUtil.storeAccessTime(currentTime);
							startActivity(intent);
							dialog.dismiss();
							finish();
						} else {
							dialog.dismiss();
							toast.show();
						}
					}
				});
			}
		})).start();
	}

	private boolean Login(String username, String password) {

		boolean check = false;

		Map<String, String> xauthParameterMap = new HashMap<String, String>();
		HashMap<String, String> resultMap = new HashMap<String, String>();

		try {
			xauthParameterMap.put("x_auth_mode", SignatureEncode
					.encode("client_auth"));
			xauthParameterMap.put("x_auth_username", SignatureEncode
					.encode(username));
			xauthParameterMap.put("x_auth_password", SignatureEncode
					.encode(password));
		} catch (UnsupportedEncodingException ignore) {
		}

		Xauth xAuth = new Xauth(xauthParameterMap);
		resultMap = xAuth.getAccessToken();

		if (resultMap.size() > 0) {
			check = youRoomUtil.storeOauthTokenToLocal(resultMap);
		}
		return check;
	}
}
