package com.jerry.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView{

	
	
	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	public MarqueeTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	@Override
	public boolean isFocused() {
		return true;
	}

}
