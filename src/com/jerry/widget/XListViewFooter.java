package com.jerry.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jerry.lily.R;

public class XListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;
	public final static int STATE_NONE = 3;

	private Context mContext;

	private View mProgressBar;
	private TextView mHintView;
	private LinearLayout mContainer;

	public XListViewFooter(Context context) {
		super(context);
		initView(context);
	}

	public XListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}


	public void setState(int state) {
		mProgressBar.setVisibility(View.INVISIBLE);
		mHintView.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.xlistview_footer_hint_ready);
		} else if (state == STATE_LOADING) {
			setVisiableHeight(XListView.PULL_LOAD_MORE_DELTA);
			mProgressBar.setVisibility(View.VISIBLE);
		} else if(state != STATE_NONE) {
			mHintView.setVisibility(View.GONE);
		}
	}
	
	public int getVisiableHeight() {
		return mContainer.getHeight();
	}

	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	private void initView(Context context) {
		mContext = context;
		mContainer = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.xlistview_footer, null);
		addView(mContainer, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
		setGravity(Gravity.TOP);
		mProgressBar = mContainer.findViewById(R.id.xlistview_footer_progressbar);
		mHintView = (TextView)mContainer.findViewById(R.id.xlistview_footer_hint_textview);
	}
}
