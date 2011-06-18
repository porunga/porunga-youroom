package com.porunga.youroomclient;

import java.io.Serializable;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;

//ListViewカスタマイズ用のArrayAdapterに利用するクラス    
public class YouRoomEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int COMPRESS_QUALITY = 50;

	private int id = -1;
	private String content;
	private int rootId;
	private int parentId = 0;
	private String createdTime;
	private String updatedTime;
	private int descendantsCount = -1;
	private String participationName;
	private String participationId;
	private int level;
	private ArrayList<YouRoomEntry> children;
	private String attachmentType = "";
	private String text = "";
	private String link = "";
	private String fileName = "";
	private boolean canUpdate=false;
	private String roomId;
	private byte[] memberImage;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getDescendantsCount() {
		return descendantsCount;
	}

	public void setDescendantsCount(int descendantsCount) {
		this.descendantsCount = descendantsCount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getRootId() {
		return rootId;
	}

	public void setRootId(int rootId) {
		this.rootId = rootId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
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

	public String getParticipationName() {
		return participationName;
	}

	public void setParticipationName(String participationName) {
		this.participationName = participationName;
	}

	public String getParticipationId() {
		return participationId;
	}

	public void setParticipationId(String participationId) {
		this.participationId = participationId;
	}

	public ArrayList<YouRoomEntry> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<YouRoomEntry> children) {
		this.children = children;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public boolean getCanUpdate() {
		return canUpdate;
	}

	public void setCanUpdate(boolean canUpdate) {
		this.canUpdate = canUpdate;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public byte[] getMemberImage() {
		return memberImage;
	}

	public void setMemberImage(Bitmap memberImageBitmap) {
		this.memberImage = serializeBitmap(memberImageBitmap);
	}

	final byte[] serializeBitmap(Bitmap memberImageBitmap) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		memberImageBitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, bout);
		byte[] image = bout.toByteArray();
		return image;
	}

}