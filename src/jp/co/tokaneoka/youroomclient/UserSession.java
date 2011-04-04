package jp.co.tokaneoka.youroomclient;

import java.util.HashMap;

public class UserSession {
	
	// Begin -- Singleton Pattern --
	private static UserSession instance = new UserSession();	
	private UserSession() {}
	
	public static UserSession getInstance() {
		return instance;
	}
	// End -- Singleton Pattern --
	
	private HashMap<String, String> roomAccessTimeMap = new HashMap<String, String>();
	
	public String getRoomAccessTime(String roomId) {
		return roomAccessTimeMap.get(roomId);
	}

	public void setRoomAccessTime(String roomId, String lastAccessTime) {
		this.roomAccessTimeMap.put(roomId, lastAccessTime);
	}

	/*
	private String lastAccessTime;
	public String getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(String lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	*/
	
}
