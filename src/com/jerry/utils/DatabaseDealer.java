package com.jerry.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.jerry.model.Article;
import com.jerry.model.Mail;
import com.jerry.model.MailGroup;

public class DatabaseDealer {
	private static final String DEFAULT_DATABASE_NAME = "LILY";

	private static final SQLiteDatabase createDataBase(Context context) {
		DatabaseHelper dbHelper = new DatabaseHelper(context, DEFAULT_DATABASE_NAME);
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		return database;
	}

	public static final String getLastMailTargetUrl(Context context) {
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select value from settings where key = 'mailurl'",null);
		if(cursor.moveToNext()) {
			return cursor.getString(0);
		}
		return "";
	}

	public static final void syncLastMailTargetUrl(Context context, String url) {
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select value from settings where key = 'mailurl'",null);
		if(!cursor.moveToNext()) {
			db.execSQL("INSERT INTO settings(key,value) VALUES ('mailurl','abc')");
		} else if(url.length() > 0){
			String old = cursor.getString(0);
			if(old.length() == 0) {
				cursor.close();
				db.close();
				return;
			}
			String reg = "?start=";
			if(old.contains(reg)) {
				int oldValue = Integer.valueOf(old.substring(old.indexOf(reg) + reg.length()));
				if(url.contains(reg)) {
					int newValue = Integer.valueOf(url.substring(url.indexOf(reg) + reg.length()));
					if(oldValue <= newValue) {
						cursor.close();
						db.close();
						return;
					}
				}
			}
		}
		cursor.close();
		db.execSQL("update settings set value='" + url + "' where key = 'mailurl'");
		db.close();
	}

	public static final List<Mail> getMailListByPoster(Context context,String poster) {
		SQLiteDatabase db = createDataBase(context);
		List<Mail> mailList = new ArrayList<Mail>();
		Cursor cursor = db.rawQuery("select content, time, type from mails where poster = '" + poster + "' order by time asc",null);
		while(cursor.moveToNext()){
			Mail mail = new Mail();
			mail.setContent(cursor.getString(0));
			mail.setTime(cursor.getLong(1));
			mail.setType(cursor.getInt(2));
			mailList.add(mail);
		}
		db.execSQL("update mails set isread = 1 where poster='" + poster + "' and isread = 0");
		cursor.close();
		db.close();
		return mailList;
	}

	public static final int getStoredLatestMailCode(Context context, boolean isLatest) {
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select contenturl from mails where type <> " + Constants.MAIL_TYPE_SENT + " order by time " + (isLatest ? "desc" : "asc") + " limit 0,1",null);
		while(cursor.moveToNext()){
			String contentUrl = cursor.getString(0);
			Mail mail = new Mail();
			mail.setContentUrl(contentUrl);
			return mail.hashCode();
		}
		cursor.close();
		db.close();
		return 0;
	}

	public static final List<MailGroup> getMailByGroup(Context context) {
		List<MailGroup> group = new ArrayList<MailGroup>();
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select poster, content, time, isread ,type from mails group by poster order by time desc",null);
		while(cursor.moveToNext()){
			String poster = cursor.getString(0);
			String content = cursor.getString(1);
			long time = cursor.getLong(2);
			boolean isRead = (cursor.getInt(3) == 0 ? false : true);
			int type = cursor.getInt(4);
			Mail mail = new Mail(poster, content, time, isRead, type);
			group.add(new MailGroup(poster, mail));
		}
		cursor.close();
		db.close();
		return group;
	}

	public static final void insertMails(Context context, List<Mail> mailList) {
		SQLiteDatabase db = createDataBase(context);
		for(Mail mail : mailList) {
			mail.insert(context, db);
		}
		db.close();
	}

	public static final void deleteFromBlock(Context context, String name) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("delete from block where name = '" + name + "'");
		db.close();
	}

	public static final void add2Block(Context context,String name) {
		SQLiteDatabase db = createDataBase(context);
		ContentValues values = new ContentValues();
		values.put("name", name);
		db.insert("block", null, values);
		db.close();
	}

	public static final List<String> getBlockList(Context context) {
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select name from block",null);
		List<String> blockList = new ArrayList<String>();
		while(cursor.moveToNext()){
			blockList.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return blockList;
	}

	public static final void insert(Context context,String username, String password) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("delete from userinfo");
		ContentValues values = new ContentValues();
		values.put("username", username);
		values.put("password", password);
		db.insert("userinfo", null, values);
		db.close();
	}

	public static final void updateSettings(Context context, String key, String value) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("update settings set value = '" + value + "' where key = '" + key + "'");
		db.close();
	}

	public static final List<String> getFriendsList(Context context) {
		List<String> list = new ArrayList<String>();
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select distinct name from friends order by _id desc",null);
		while(cursor.moveToNext()) {
			list.add(cursor.getString(0));
		}
		return list;
	}

	public static final List<Article> getArticleColliection(Context context) {
		List<Article> list = new ArrayList<Article>();
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select author, title,board,contenturl from collection order by _id desc",null);
		while(cursor.moveToNext()){
			Article article = new Article();
			article.setAuthorName(cursor.getString(0));
			article.setTitle(cursor.getString(1));
			article.setGroup(cursor.getString(2));
			article.setContentUrl(cursor.getString(3));
			list.add(article);
		}
		cursor.close();
		db.close();
		return list;
	}

	public static final void insertArticleCollection(Context context, Article article) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("INSERT INTO collection(author, title,board,contenturl) VALUES ('" + article.getAuthorName() + "', '" + article.getTitle() + "','" + article.getGroup() + "','" + article.getContentUrl() + "')");
		db.close();
	}

	public static final void insertFriends(Context context, String name) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("INSERT INTO friends(name) VALUES ('" + name + "')");
		db.close();
	}

	private static final void insertSettings(Context context, String key, String value) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("INSERT INTO settings(key, value) VALUES ('" + key + "', '" + value + "')");
		db.close();
	}

	public static final com.jerry.model.Settings getSettings(Context context) {
		SQLiteDatabase db = createDataBase(context);
		Bundle bundle = new Bundle();
		Cursor cursor = db.rawQuery("select key,value from settings",null);
		while(cursor.moveToNext()){
			bundle.putString(cursor.getString(0), cursor.getString(1));
		}
		cursor.close();
		db.close();
		String isLogin = bundle.getString("isLogin");
		String isSavePic = bundle.getString("isSavePic");
		String isShowPic = bundle.getString("isShowPic");
		String isSendMail = bundle.getString("isSendMail");
		String isNight = bundle.getString("isNight");
		String sign = bundle.getString("sign");
		String fontSize = bundle.getString("fontSize");

		if(isLogin == null) {
			insertSettings(context, "isLogin", "0");
			isLogin = "0";
		}
		if(isSavePic == null) {
			insertSettings(context, "isSavePic", "1");
			isSavePic = "1";
		}
		if(isShowPic == null) {
			insertSettings(context, "isShowPic", "1");
			isShowPic = "1";
		}
		if(isSendMail == null) {
			insertSettings(context, "isSendMail", "0");
			isSendMail = "0";
		}
		if(isNight == null) {
			insertSettings(context, "isNight", "0");
			isNight = "0";
		}

		if(sign == null) {
			insertSettings(context, "sign", Constants.SIGN);
			sign = Constants.SIGN;
		}

		if(fontSize == null) {
			insertSettings(context, "fontSize", "15");
			fontSize = "19";
		}

		com.jerry.model.Settings settings = new com.jerry.model.Settings();
		settings.setLogin(isLogin.equals("1"));
		settings.setShowPic(isShowPic.equals("1"));
		settings.setSendMail(isSendMail.equals("1"));
		settings.setNight(isNight.equals("1"));
		settings.setSign(sign);
		settings.setFontSize(Integer.valueOf(fontSize));
		return settings;
	}

	public static final boolean isFav(Context context, String boardName) {
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select english from fav where english ='" + boardName + "'",null);
		if(cursor.getCount() > 0) {
			db.close();
			cursor.close();
			return true;
		}
		db.close();
		cursor.close();
		return false;
	}

	public static final Bundle query(Context context) {
		Bundle bundle = new Bundle();
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select username,password from userinfo order by _id desc",null);
		while(cursor.moveToNext()){
			bundle.putString("username", cursor.getString(0));
			bundle.putString("password", cursor.getString(1));
		}
		cursor.close();
		db.close();
		return bundle;
	}

	public static final void addBoard2LocalFav(Context context,String boardName) {
		ContentValues values = new ContentValues();
		values.put("english", boardName);
		values.put("islocal", 1);
		SQLiteDatabase db = createDataBase(context);
		db.insert("fav", null, values);
		db.close();
	}

	public static final void addFav(Context context,List<ContentValues> valueList) {
		if (valueList == null || valueList.size() == 0) {
			return;
		}
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("delete from fav where islocal = 0");
		for(int i = 0; i < valueList.size(); i++) {
			db.insert("fav", null, valueList.get(i));
		}
		db.close();
	}

	public static List<String> getFavList(Context context) {
		SQLiteDatabase db = createDataBase(context);
		Cursor cursor = db.rawQuery("select distinct english from fav order by english",null);
		List<String> fav = new ArrayList<String>();
		while(cursor.moveToNext()){
			fav.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return fav;
	}

	public static final void deleteSpecialFav(Context context,String boardName) {
		SQLiteDatabase db = createDataBase(context);
		db.execSQL("delete from fav where english = '" + boardName + "'");
	}


	public static List<String> getAllBoardList(Context context, String query) {
		SQLiteDatabase db = createDataBase(context);
		String queryBlock = query.length() > 0 ? " where english like '%" 
				+ query + "%' or chinese like '%" + query + "%'" : "";
		Cursor cursor = db.rawQuery("select distinct english,chinese from board" + queryBlock,null);
		List<String> fav = new ArrayList<String>();
		while(cursor.moveToNext()) {
			fav.add(cursor.getString(0) + "(" + cursor.getString(1) + ")");
		}
		Collections.sort(fav,new MyComparator());
		cursor.close();
		db.close();
		return fav;
	}

	public static final Cursor getAllBoardCursor(Context context) {
		SQLiteDatabase db = createDataBase(context);
		return db.rawQuery("select english,chinese from board order by english",null);
	}
}
