package com.jerry.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Mail {
	private String poster;
	private String contentUrl;
	private String content;
	private long time;
	private boolean isRead;
	private int type;
	
	public Mail() {
		
	}
	
	public Mail(String poster, String content, long time, boolean isRead, int type) {
		this.poster = poster;
		this.content = content;
		this.time = time;
		this.isRead = isRead;
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return contentUrl.hashCode();
	}
	
	public String getPoster() {
		return poster;
	}
	public void setPoster(String poster) {
		this.poster = poster;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getContentUrl() {
		return contentUrl;
	}
	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public void insert(Context context, SQLiteDatabase db) {
		db.execSQL("INSERT INTO mails(poster,content,contenturl,time,isread,type) VALUES ('" + poster.toLowerCase() + "', '" + content + "','" 
	+ contentUrl + "','" + String.valueOf(time) + "','" + (isRead ? "1" : "0") + "','" +String.valueOf(type) + "')");
	}

}
