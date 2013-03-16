package com.jerry.widget;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SimpleNameAdapter extends BaseAdapter {
	protected int layoutId;
	protected int textViewId;

	protected List<String> data;
	protected LayoutInflater mInflater;

	public SimpleNameAdapter(Context context, int layoutId,int textViewId,List<String> data) {
		this.data = data;
		this.textViewId = textViewId;
		this.layoutId = layoutId;
		this.mInflater = ((Activity) context).getLayoutInflater();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public String getItem(int arg0) {
		return data.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutId, null);
			holder = new ViewHolder();
			holder.itemName = (TextView) convertView.findViewById(textViewId);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.itemName.setText(data.get(position));
		return convertView;
	}

	private static final class ViewHolder{
		public TextView itemName;
	}
}
