package com.jerry.widget;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MainPageAdapter extends PagerAdapter{

	private List<View> mListView;

	public MainPageAdapter(List<View> list) {
		// TODO Auto-generated method stub
		this.mListView= list;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewGroup)arg0).removeView(mListView.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {

	}

	@Override
	public int getCount() {
		return mListView.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewGroup)arg0).addView(mListView.get(arg1), 0);
		return mListView.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==(arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {
		return null;
	}
	
	@Override
	public void startUpdate(View arg0) {

	}

}