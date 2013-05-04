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
		public TextView content;
		public TextView left;
		public TextView right;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutID, null);
			holder = new ViewHolder();
			holder.content = (TextView) convertView.findViewById(R.id.content_text);
			holder.left = (TextView) convertView.findViewById(R.id.left_sub_text);
			holder.right = (TextView) convertView.findViewById(R.id.right_sub_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Article article = getItem(position);
		holder.content.setText(article.getTitle());
		holder.left.setText(article.getAuthorName() + "·¢±íÓÚ" + article.getDetailTime());
		holder.right.setText(article.getReplyCount() + "/" + article.getViewCount());
		return convertView;
	}

}