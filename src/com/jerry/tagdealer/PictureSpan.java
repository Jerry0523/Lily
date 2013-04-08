package com.jerry.tagdealer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.view.View;

import com.jerry.lily.PicView;
import com.jerry.utils.PicDownloader;

public class PictureSpan extends DynamicDrawableSpan{
	private String picSource;
	private Context context;

	public PictureSpan(String picSource, Context context) {
		this.picSource = picSource;
		this.context = context;
	}

	@Override
	public Drawable getDrawable() {
		Drawable drawable = PicDownloader.getInstance().getPictureDrawable(picSource);
		if(drawable.getBounds() == null) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		return drawable;
	}

	public ClickableSpan getClickAction() {
		return new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(context, PicView.class);
				intent.putExtra("source", picSource);
				context.startActivity(intent);
			}
		};
	}
}
