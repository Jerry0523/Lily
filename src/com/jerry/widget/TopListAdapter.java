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

public class TopListAdapter extends BaseAdapter{
	private int layoutID;
	private LayoutInflater  mInflater;
	private List<Article> articleList;

	public TopListAdapter(int layoutID, Context context, List<Article> articleList) {
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
			holder.author = (TextView) convertView.findViewById(R.id.author);
			holder.board = (TextView) convertView.findViewById(R.id.board);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Article article = getItem(position);
		holder.author.setText(article.getAuthorName());
		holder.board.setText(article.getBoard());
		holder.title.setText(article.getTitle());
		return convertView;
	}

	private static final class ViewHolder{
		public TextView title;
		public TextView author;
		public TextView board;
	}
}
