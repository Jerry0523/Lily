package com.jerry.tagdealer;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;

import com.jerry.lily.ArticleActivity;
import com.jerry.lily.Author;
import com.jerry.lily.BoardList;
import com.jerry.lily.Url;
import com.jerry.utils.Constants;

public class UrlSpan extends ClickableSpan{
	private String url;
	private Context context;

	public UrlSpan(String url, Context context) {
		this.url = url;
		this.context = context;
	}
	@Override
	public void onClick(View widget) {
		Intent intent = null;
		if(Pattern.compile(Constants.REG_AUTHOR, Pattern.CASE_INSENSITIVE).matcher(url).find()){
			intent = new Intent(context, Author.class);
			intent.putExtra("authorName", url.substring(url.indexOf("bbsqry?userid=") + 14));
		} else if(Pattern.compile(Constants.REG_BOARD, Pattern.CASE_INSENSITIVE).matcher(url).find()){
			intent = new Intent(context, BoardList.class);
			intent.putExtra("boardName", url.substring(url.indexOf("board?board=") + 12));
		} else if(Pattern.compile(Constants.REG_ARTICLE, Pattern.CASE_INSENSITIVE).matcher(url).find()){
			intent = new Intent(context, ArticleActivity.class);
			intent.putExtra("board", url.substring(url.indexOf("bbstcon?board=") + 14, url.indexOf("&file=M.")));
			intent.putExtra("contentUrl", url);
		} else {
			intent = new Intent(context, Url.class);
			intent.putExtra("url", url);
		}
		context.startActivity(intent);
	}
}
