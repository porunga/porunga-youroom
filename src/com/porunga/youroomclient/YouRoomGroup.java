package com.porunga.youroomclient;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;

//ListViewカスタマイズ用のArrayAdapterに利用するクラス    
public class YouRoomGroup implements Serializable {
	private static final long serialVersionUID = -8261690244602614995L;
	private static final int COMPRESS_QUALITY = 50;

	private int id;
	private int roomId;
	private String createdTime;
	private String updatedTime;
	private byte[] roomImage;
	private String name;
	private boolean opened;
	private String lastAccessTime = null;

	public String getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(String lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getRoomImage() {
		return roomImage;
	}

	public void setRoomImage(Bitmap roomImageBitmap) {
		
		this.roomImage = serializeBitmap(roomImageBitmap);
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	final byte[] serializeBitmap(Bitmap roomImageBitmap) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		roomImageBitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY ,bout);
		byte[] image = bout.toByteArray();
		return image;
	}

}