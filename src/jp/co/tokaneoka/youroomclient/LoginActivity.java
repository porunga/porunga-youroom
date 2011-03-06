package jp.co.tokaneoka.youroomclient;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import jp.co.tokaneoka.youroomclient.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
	
	private static final String PREFERENCE_KEY = "AccessToken";
	SharedPreferences sharedpref;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);
        
        Button button = (Button) findViewById(R.id.login_submit);
        button.setOnClickListener(this);
        
    }

	@Override
	public void onClick(View v) {
		
		EditText emailText = (EditText)findViewById(R.id.login_email);
		String email = emailText.getText().toString();
		
		EditText passwordText = (EditText)findViewById(R.id.login_password);
		String password = passwordText.getText().toString();
		Login(email,password);
		
	}
	
	private void Login(String username, String password) {
		
		Map<String, String> xauthParameterMap = new HashMap<String, String>();		
    	HashMap<String, String> resultMap = new HashMap<String, String>();
    	
    	try {
    		xauthParameterMap.put("x_auth_mode", SignatureEncode.encode("client_auth"));
    		xauthParameterMap.put("x_auth_username", SignatureEncode.encode(username));
    		xauthParameterMap.put("x_auth_password", SignatureEncode.encode(password));
    	} catch (UnsupportedEncodingException ignore) {
    	}
    	    	
		Xauth xAuth = new Xauth(xauthParameterMap);
		resultMap = xAuth.getAccessToken();
		
		if ( resultMap.size() > 0 ){
			sharedpref = getSharedPreferences(PREFERENCE_KEY, Activity.MODE_APPEND );
			Editor editor = sharedpref.edit();
			editor.putString("oauthToken", resultMap.get("oauth_token"));
			editor.putString("oauthTokenSecret", resultMap.get("oauth_token_secret"));
			editor.commit();
			Intent intent = new Intent(this, YouRoomClientActivity.class);
			startActivity(intent);
		}
	}

}
