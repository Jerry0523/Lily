package com.jerry.tagdealer;

import android.content.Context;
import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;

import com.jerry.lily.Url;

public class UrlSpan extends ClickableSpan{
	private String url;
	private Context context;
	
	public UrlSpan(String url, Context context) {
		this.url = url;
		this.context = context;
	}
	@Override
	public void onClick(View widget) {
		Intent intent = new Intent(context, Url.class);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}
}
