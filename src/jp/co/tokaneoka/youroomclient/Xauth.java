package jp.co.tokaneoka.youroomclient;

/*
 * http://d.hatena.ne.jp/lynmock/20100502/の「JavaでのXauth AccessToken取得サンプル」
 * を参考に（ほぼそのまま）作成
 */

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

public class Xauth {

	private Map<String, String> xauthParameterMap;
	private YouRoomAccess youRoomAccess;

	public Xauth(Map<String, String> xauthParameterMap) {
		this.youRoomAccess = new YouRoomAccess();
		this.xauthParameterMap = xauthParameterMap;
	}

	public HashMap<String, String> getAccessToken() {

		String method = "POST";
		String api = "https://www.youroom.in/oauth/access_token";
		HashMap<String, String> oAuthTokenMap = new HashMap<String, String>();
		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		youRoomAccess.setParameter(xauthParameterMap);

		HttpResponse objResponse = youRoomAccess.authenticate();
		// TODO if (objResponse == null )

		int statusCode = objResponse.getStatusLine().getStatusCode();

		if (statusCode == HttpURLConnection.HTTP_OK) {
			try {
				String result = EntityUtils.toString(objResponse.getEntity(),
						"UTF-8");
				String[] parameters = result.split("&");
				for (String parameter : parameters) {
					String[] keyAndValue = parameter.split("=");
					if (keyAndValue.length < 2) {
						continue;
					}
					String key = keyAndValue[0];
					String value = keyAndValue[1];
					if ("oauth_token".equals(key)
							|| "oauth_token_secret".equals(key)) {
						oAuthTokenMap.put(key, value);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return oAuthTokenMap;
	}
}