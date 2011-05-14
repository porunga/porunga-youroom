package com.porunga.youroomclient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class YouRoomCommand {

	private YouRoomAccess youRoomAccess;

	public YouRoomCommand(Map<String, String> oauthTokenMap) {
		this.youRoomAccess = new YouRoomAccess();
		// TODO oauthTokenの設定はYouRoomAccessでやりたい
		youRoomAccess.setOauthToken(oauthTokenMap.get("oauth_token"));
		youRoomAccess.setOauthTokenSecret(oauthTokenMap.get("oauth_token_secret"));
	}

	/*
	 * public HashMap<String,String> getAccessToken(){
	 * 
	 * String method = "POST"; String api =
	 * "https://www.youroom.in/oauth/access_token"; HashMap<String,String>
	 * oAuthTokenMap = new HashMap<String,String>();
	 * 
	 * youRoomAccess.setMethod(method); youRoomAccess.setApi(api); HttpResponse
	 * objResponse = youRoomAccess.postRequest(); //TODO if (objResponse == null
	 * )
	 * 
	 * int statusCode = objResponse.getStatusLine().getStatusCode();
	 * 
	 * if (statusCode == HttpURLConnection.HTTP_OK) { try { String result =
	 * EntityUtils.toString(objResponse.getEntity(), "UTF-8"); String[]
	 * parameters = result.split("&"); for (String parameter : parameters) {
	 * String[] keyAndValue = parameter.split("="); if (keyAndValue.length < 2)
	 * { continue; } String key = keyAndValue[0]; String value = keyAndValue[1];
	 * if("oauth_token".equals(key) || "oauth_token_secret".equals(key)){
	 * oAuthTokenMap.put(key,value); } } } catch (ParseException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); } }
	 * return oAuthTokenMap; }
	 */

	public String getMyGroup() throws YouRoomServerException {
		Log.i("ACCESS", "getMyGroup");

		String method = "GET";

		String api = "https://www.youroom.in/groups/my";
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("format", "json");

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		youRoomAccess.setParameter(parameterMap);
		HttpResponse objResponse = youRoomAccess.requestGet();

		int statusCode = objResponse.getStatusLine().getStatusCode();
		String decodeResult = "";
		if (statusCode == HttpURLConnection.HTTP_OK) {
			String result;
			try {
				result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				decodeResult = UnicodeEscape.decode(result);
			} catch (Exception e) {
				throw new YouRoomServerException(e);
			}
		}
		return decodeResult;
	}

	public String getEntry(String roomId, String entryId) throws YouRoomServerException {
		Log.i("ACCESS", "getEntry");

		String method = "GET";
		String api = "https://www.youroom.in/r/" + roomId + "/entries/" + entryId;
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("format", "json");

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		youRoomAccess.setParameter(parameterMap);
		HttpResponse objResponse = youRoomAccess.requestGet();

		int statusCode = objResponse.getStatusLine().getStatusCode();
		String decodeResult = "";
		if (statusCode == HttpURLConnection.HTTP_OK) {
			String result;
			try {
				result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				decodeResult = UnicodeEscape.decode(result);
			} catch (Exception e) {
				throw new YouRoomServerException(e);
			}
		}
		return decodeResult;
	}

	public Bitmap getImage(String api) throws YouRoomServerException {
		Log.i("ACCESS", "getRoomImage");

		String method = "GET";
//		String api = "https://www.youroom.in/r/" + roomId + "/picture";

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		HttpResponse objResponse = youRoomAccess.requestGet();

		int statusCode = objResponse.getStatusLine().getStatusCode();
		Bitmap roomImageBitmap = null;
		if (statusCode == HttpURLConnection.HTTP_OK) {
			try {
				InputStream is = objResponse.getEntity().getContent();
				roomImageBitmap = BitmapFactory.decodeStream(is);
				is.close();
			} catch (Exception e) {
				throw new YouRoomServerException(e);
			}
		}
		return roomImageBitmap;
	}

	public Bitmap getAttachmentImage(String roomId, String entryId) throws YouRoomServerException {
		Log.i("ACCESS", "getAttachmentImage");

		String method = "GET";
		String api = "https://www.youroom.in/r/" + roomId + "/entries/" + entryId + "/attachment";

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		HttpResponse objResponse = youRoomAccess.requestGet();

		int statusCode = objResponse.getStatusLine().getStatusCode();
		Bitmap attachmentImageBitmap = null;
		if (statusCode == HttpURLConnection.HTTP_OK) {
			try {
				InputStream is = objResponse.getEntity().getContent();
				attachmentImageBitmap = BitmapFactory.decodeStream(is);
				is.close();
			} catch (Exception e) {
				throw new YouRoomServerException(e);
			}
		}
		return attachmentImageBitmap;
	}

	public String getRoomTimeLine(String roomId, Map<String, String> parameterMap) throws YouRoomServerException {
		Log.i("ACCESS", "getRoomTimeLine");

		String method = "GET";
		String api = "https://www.youroom.in/r/" + roomId;
		parameterMap.put("format", "json");

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		youRoomAccess.setParameter(parameterMap);
		HttpResponse objResponse = youRoomAccess.requestGet();
		// TODO if (objResponse == null )

		int statusCode = objResponse.getStatusLine().getStatusCode();
		String decodeResult = "";
		if (statusCode == HttpURLConnection.HTTP_OK) {
			String result;
			try {
				result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				decodeResult = UnicodeEscape.decode(result);
			} catch (Exception e) {
				throw new YouRoomServerException(e);
			}
		}
		return decodeResult;
	}

	public String acquireHomeTimeline(Map<String, String> parameterMap) throws YouRoomServerException {
		Log.i("ACCESS", "acquireHomeTimeline");

		String method = "GET";

		String api = "https://www.youroom.in/";
		parameterMap.put("format", "json");

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		youRoomAccess.setParameter(parameterMap);
		HttpResponse objResponse = youRoomAccess.requestGet();

		int statusCode = objResponse.getStatusLine().getStatusCode();
		String decodeResult = "";
		if (statusCode == HttpURLConnection.HTTP_OK) {
			String result;
			try {
				result = EntityUtils.toString(objResponse.getEntity(), "UTF-8");
				decodeResult = UnicodeEscape.decode(result);
			} catch (Exception e) {
				throw new YouRoomServerException(e);
			}
		}
		return decodeResult;
	}

	public String createEntry(String roomId, String parentId, String entryContent) throws YouRoomServerException {
		Log.i("ACCESS", "createEntry");

		String method = "POST";
		String api = "https://www.youroom.in/r/" + roomId + "/entries";
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("format", "json");
		parameterMap.put("entry[content]", entryContent);
		if (parentId != null)
			parameterMap.put("entry[parent_id]", parentId);

		youRoomAccess.setMethod(method);
		youRoomAccess.setApi(api);
		youRoomAccess.setParameter(parameterMap);
		HttpResponse objResponse = youRoomAccess.requestPost();

		int statusCode = objResponse.getStatusLine().getStatusCode();

		return String.valueOf(statusCode);
	}

}
