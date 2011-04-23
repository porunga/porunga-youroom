package com.porunga.youroomclient;

public class YouRoomServerException extends Exception {
	private static final long serialVersionUID = -6387733599565512519L;

	public YouRoomServerException() {
	}

	public YouRoomServerException(String detailMessage) {
		super(detailMessage);
	}

	public YouRoomServerException(Throwable throwable) {
		super(throwable);
	}

	public YouRoomServerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
