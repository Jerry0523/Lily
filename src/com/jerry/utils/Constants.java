package com.jerry.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.jerry.lily.R;

public class Constants {
	public static final int CANCEL_REPLY = 0;
	public static final int SEND_REPLY = 1;
	
	public static final int ADD_2_FAV = 5;
	public static final int POST_ARTICLE = 6;
	
	public static final int GALLERY = 7;
	public static final int CAMERA = 8;
	public static final int SHARE = 9; 
	
	public static final int BROWSER = 10;
	
	public static final String SIGN = "http://bbs.nju.edu.cn/file/W/WStaotao/sign.png";
	public static final String REG_URL = "http[\\S]*\\s";
	public static final String REG_PIC = "http[\\S]*((jpg)|(png)|(jpeg))";
	
	public static final String REG_AT = "@\\[uid\\][\\S]*\\[/uid\\]";
	public static final String REG_AUTHOR = "http://bbs.nju.edu.cn/[\\S]*bbsqry\\?userid=[\\S]*";
	public static final String REG_BOARD = "http://bbs.nju.edu.cn/[\\S]*board\\?board=[\\S]*";
	public static final String REG_ARTICLE = "http://bbs.nju.edu.cn/[\\S]*bbstcon\\?board=[\\S]*&file=M.[\\S]*.A";
	
	public static final SimpleDateFormat DATE_FORMAT =  new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",Locale.ENGLISH);
	
	public static final String[] EMOTION_RESOURCE_ARRAY = {"[:-b]","[:-8]","[;PT]","[:hx]","[;K]","[:E]","[:-(]","[;hx]","[:-v]","[;xx]",
		"[:@]","[:)]","[:(]","[:$]","[:D]","[:Q]","[:T]","[:-|]","[;P]","[;-D]","[:!]","[:L]",
		"[:?]","[:U]","[:O]","[:P]","[:'(]","[:K]","[:s]","[:C-]","[;X]","[:|]","[:H]","[:X]",
		"[;bye]","[;cool]"};
	
	public static final String[] EMOTION_REG_ARRAY = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
		"t", "u", "v", "w", "x", "y", "z"};
	
	public static final int[] EMOTION_DRAWABLE_ID_ARRAY = {R.drawable.emotion_0,R.drawable.emotion_1, R.drawable.emotion_2, R.drawable.emotion_3, R.drawable.emotion_4,
		R.drawable.emotion_5, R.drawable.emotion_6, R.drawable.emotion_7, R.drawable.emotion_8, R.drawable.emotion_9,
		R.drawable.emotion_a,R.drawable.emotion_b, R.drawable.emotion_c, R.drawable.emotion_d, R.drawable.emotion_e,
		R.drawable.emotion_f, R.drawable.emotion_g, R.drawable.emotion_h, R.drawable.emotion_i, R.drawable.emotion_j,
		R.drawable.emotion_k, R.drawable.emotion_l, R.drawable.emotion_m, R.drawable.emotion_n, R.drawable.emotion_o,
		R.drawable.emotion_p, R.drawable.emotion_q, R.drawable.emotion_r, R.drawable.emotion_s, R.drawable.emotion_t,
		R.drawable.emotion_u, R.drawable.emotion_v, R.drawable.emotion_w, R.drawable.emotion_x, R.drawable.emotion_y,
		R.drawable.emotion_z};
	
}
