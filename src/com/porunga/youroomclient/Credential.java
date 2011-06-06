package com.porunga.youroomclient;

import java.io.Serializable;

public class Credential implements Serializable {
	private static final long serialVersionUID = 1L;

	private String roomId = "";
	private String participationId = "";
	private int admin = 0;

	Credential() {
	}

	Credential(String roomId, String participationId, int admin) {
		this.roomId = roomId;
		this.participationId = participationId;
		this.admin = admin;
	}
	
	public String getRoomId(){
		return this.roomId;
	}
	
	public void setRoomId(String roomId){
		this.roomId = roomId;
	}
	
	public String getParticipationId(){
		return this.participationId;
	}
	
	public void setParticipationId(String participationId){
		this.participationId = participationId;
	}
	
	public int getAdmin(){
		return this.admin;
	}
	
	public void setAdmin(int admin){
		this.admin = admin;
	}
}
