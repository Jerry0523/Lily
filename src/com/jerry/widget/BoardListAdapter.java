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

public class BoardListAdapter extends BaseAdapter {
	private int layoutID;
	private LayoutInflater  mInflater;
	private List<Article> articleList;

	public BoardListAdapter(Context context, int layoutID, List<Article> articleList) {
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

	private static final class ViewHolder{
		public TextView title;
		public TextView author;
		public TextView reply;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutID, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.lb_title);
			holder.author = (TextView) convertView.findViewById(R.id.lb_author);
			holder.reply = (TextView) convertView.findViewById(R.id.lb_reply);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Article article = getItem(position);
		holder.title.setText(article.getTitle());
		holder.author.setText(article.getAuthorName() + "·¢±íÓÚ" + article.getDetailTime());
		holder.reply.setText(article.getReplyCount() + "/" + article.getViewCount());
		return convertView;
	}

}