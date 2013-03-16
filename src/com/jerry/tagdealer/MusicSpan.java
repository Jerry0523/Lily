package com.jerry.tagdealer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.view.View;

import com.jerry.lily.R;

public class MusicSpan extends DynamicDrawableSpan{
	private Context context;
	private String musicUrl;

	public MusicSpan(String musicUrl, Context context) {
		this.context = context;
		this.musicUrl = musicUrl;
	}

	@Override
	public Drawable getDrawable() {
		Drawable drawable = context.getResources().getDrawable(R.drawable.music);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		return drawable;
	}
	
	public ClickableSpan getClickAction() {
		return new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent i=new Intent(Intent.ACTION_VIEW);
				Uri uri=Uri.parse(musicUrl);
				i.setDataAndType(uri, "audio/*");
				context.startActivity(i);
			}
		};
	}
}
