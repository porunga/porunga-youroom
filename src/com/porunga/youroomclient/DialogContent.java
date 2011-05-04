package com.porunga.youroomclient;

public class DialogContent {
	private String category;
	private String content;

	public DialogContent() {
		this.category = "";
		this.content = "";
	}
	public DialogContent(String category, String content) {
		this.category = category;
		this.content = content;
	}
	
	
	public String getCategory(){
		return category;
	}
	
	public void setCategory(String category){
		this.category=category;
	}
	
	public String getContent(){
		return content;
	}
	
	public void setContent(String content){
		this.content = content;
	}
}
