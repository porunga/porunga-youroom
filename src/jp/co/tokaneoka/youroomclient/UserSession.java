package jp.co.tokaneoka.youroomclient;

public class UserSession {
	
	// Begin -- Singleton Pattern --
	private static UserSession instance = new UserSession();	
	private UserSession() {}
	
	public static UserSession getInstance() {
		return instance;
	}
	// End -- Singleton Pattern --
	
	private String lastAccessTime;
	
	public String getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(String lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	
}
