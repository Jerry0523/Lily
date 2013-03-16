package com.jerry.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
	public static final int CANCEL_REPLY = 0;
	public static final int SEND_REPLY = 1;
	
	public static final int ADD_2_FAV = 5;
	public static final int POST_ARTICLE = 6;
	
	public static final int GALLERY = 7;
	public static final int CAMERA = 8;
	public static final int SHARE = 9; 
	
	public static final int BROWSER = 10;
	
	public static final String sign = "http://bbs.nju.edu.cn/file/W/WStaotao/sign.png";
	public static final String reg = "http[\\S]*\\s";
	
	public static final SimpleDateFormat DATE_FORMAT =  new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",Locale.ENGLISH);
	
}
