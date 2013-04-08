package com.jerry.tagdealer;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.jerry.lily.R;
import com.jerry.utils.Constants;

public class TagDealer implements TagHandler{
	private static TagDealer THIS;
	
	private boolean isCloseTag = false;
	private int startIndex = 0;
	private Context mContext;

	public static final TagDealer getInstance(Context context) {
		if(THIS == null) {
			THIS = new TagDealer();
		} 
		THIS.reset(context);
		return THIS;
	}

	private TagDealer() {
		
	}

	private void reset(Context context) {
		isCloseTag = false;
		startIndex = 0;
		mContext = context;
	}

	@Override
	public void handleTag(boolean opening, String tag, Editable output,
			XMLReader xmlReader) {
		if(!opening) {
			return;
		}
		if(tag.equals("uid")) {
			processUid(opening, output);
		} else if(tag.equals("emotion")) {
			processEmotion(opening, output);
		} else if(tag.equals("pic")) {
			processPicture(opening, output);
		} else if(tag.equals("url")) {
			processUrl(opening, output);
		} else if(tag.equals("wma")) {
			processMusic(opening, output);
		} else if(tag.equals("brd")) {
			processBoard(opening, output);
		}
	}
	
	private void processBoard(boolean opening, Editable output) {

		if(!isCloseTag) {
			startIndex = output.length();
			isCloseTag = true;
		} else {
			String boardName = output.subSequence(startIndex, output.length()).toString();
			output.setSpan(new UrlSpan("http://bbs.nju.edu.cn/board?board=" + boardName, mContext), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			isCloseTag = false;
		}
	}

	private void processMusic(boolean opening, Editable output) {
		if(!isCloseTag) {
			startIndex = output.length();
			isCloseTag = true;
		} else {
			String musicUrl = output.subSequence(startIndex, output.length()).toString();
			MusicSpan musicSpan = new MusicSpan(musicUrl, mContext);
			output.setSpan(musicSpan, startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			output.setSpan(musicSpan.getClickAction(), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			isCloseTag = false;
		}
	}

	private void processUrl(boolean opening, Editable output) {

		if(!isCloseTag) {
			startIndex = output.length();
			isCloseTag = true;
		} else {
			String url = output.subSequence(startIndex, output.length()).toString();
			output.setSpan(new UrlSpan(url, mContext), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			isCloseTag = false;
		}
	}

	private void processPicture(boolean opening, Editable output) {
		if(!isCloseTag) {
			startIndex = output.length();
			isCloseTag = true;
		} else {
			String picSource = output.subSequence(startIndex, output.length()).toString();
			if(picSource.equals(Constants.SIGN)) {
				Drawable drawable = mContext.getResources().getDrawable(R.drawable.sign);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				output.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {
				PictureSpan picSpan = new PictureSpan(picSource, mContext);
				output.setSpan(picSpan, startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				output.setSpan(picSpan.getClickAction(), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			isCloseTag = false;
		}
	}

	private void processEmotion(boolean opening, Editable output) {
		if(!isCloseTag) {
			startIndex = output.length();
			isCloseTag = true;
		} else {
			String emotionSource = output.subSequence(startIndex, output.length()).toString();
			output.setSpan(new ImageSpan(getEmotionDrawable(emotionSource)), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			isCloseTag = false;
		}
	}

	private void processUid(boolean opening, Editable output) {
		if(!isCloseTag) {
			startIndex = output.length();
			isCloseTag = true;
		} else {
			String username = output.subSequence(startIndex, output.length()).toString();
			output.setSpan(new UserSpan(username, mContext), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			isCloseTag = false;
		}
	}

	private final Drawable getEmotionDrawable(String source) {
		Drawable drawable = null;
		for(int i = 0; i < Constants.EMOTION_REG_ARRAY.length; i++) {
			if(Constants.EMOTION_REG_ARRAY[i].equals(source)) {
				drawable = mContext.getResources().getDrawable(Constants.EMOTION_DRAWABLE_ID_ARRAY[i]);
				break;
			}
		}
		if(drawable == null) {
			drawable = mContext.getResources().getDrawable(Constants.EMOTION_DRAWABLE_ID_ARRAY[0]);
		}
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		return drawable;
	}

}
