package com.jerry.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jerry.lily.R;
import com.jerry.model.MailGroup;
import com.jerry.utils.DocParser;

public class MailGroupAdapter extends BaseAdapter{
	private int layoutID;
	private LayoutInflater  mInflater;
	private List<MailGroup> mailGroup;

	public MailGroupAdapter(int layoutID, Context context, List<MailGroup> mailGroup) {
		this.layoutID = layoutID;
		this.mInflater = LayoutInflater.from(context);
		this.mailGroup = mailGroup;
	}

	@Override
	public int getCount() {
		return mailGroup.size();
	}

	@Override
	public MailGroup getItem(int position) {
		return mailGroup.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutID, null);
			holder = new ViewHolder();
			holder.left = (TextView) convertView.findViewById(R.id.left_sub_text);
			holder.right = (TextView) convertView.findViewById(R.id.right_sub_text);
			holder.content = (TextView) convertView.findViewById(R.id.content_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MailGroup group = getItem(position);
		holder.left.setText(group.getPoster());
		holder.right.setText(DocParser.parseTime(group.getLatestMail().getTime()));
		holder.content.setText(group.getLatestMail().getContent());
		if(!group.getLatestMail().isRead()) {
			holder.content.setTextColor(Color.RED);
		} else {
			holder.content.setTextColor(Color.BLACK);
		}
		return convertView;
	}

	private static final class ViewHolder{
		public TextView content;
		public TextView left;
		public TextView right;
	}
}
