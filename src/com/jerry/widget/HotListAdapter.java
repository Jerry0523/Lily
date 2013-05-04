package com.jerry.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jerry.lily.R;
import com.jerry.model.Article;

public class HotListAdapter extends BaseAdapter{
	private int layoutID;
	private LayoutInflater  mInflater;
	private List<Article> articleList;

	public HotListAdapter(int layoutID, Context context, List<Article> articleList) {
		this.layoutID = layoutID;
		this.mInflater = LayoutInflater.from(context);
		this.articleList = articleList;
	}

	@Override
	public int getCount() {
		return articleList.size();
	}

	@Override
	public Article getItem(int position) {
		return articleList.get(position);
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
			holder.board = (TextView) convertView.findViewById(R.id.right_sub_text);
			holder.title = (TextView) convertView.findViewById(R.id.content_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Article article = getItem(position);
		holder.board.setText(article.getGroup());
		holder.title.setText(article.getTitle());
		return convertView;
	}

	private static final class ViewHolder{
		public TextView title;
		public TextView board;
	}
}
