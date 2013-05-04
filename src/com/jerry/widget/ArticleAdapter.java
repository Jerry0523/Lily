package com.jerry.widget;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.lily.R;
import com.jerry.model.Article;
import com.jerry.model.SingleArticle;
import com.jerry.tagdealer.TagDealer;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;


public abstract class ArticleAdapter extends BaseAdapter {
	private int layoutID;
	private LayoutInflater  mInflater;
	private int fontSize;
	private List<SingleArticle> articleList;
	private String userName;
	private Context context;
	private String articleAuthor;

	public abstract void onDeleteArticle(SingleArticle sa);
	public abstract void onModifyArticle(SingleArticle sa);
	public abstract void onClickAuthor(String authorName);
	public abstract void onReplyArticle(String replyUrl, String authorName);

	public ArticleAdapter(int layoutID, Context context, Article article, String userName) {
		this.layoutID = layoutID;
		this.mInflater = LayoutInflater.from(context);
		this.fontSize = DatabaseDealer.getSettings(context).getFontSize();
		this.articleList = article.getSingleArticleList();
		this.userName = userName;
		this.context = context;
		this.articleAuthor = getItem(0).getAuthorName();
	}

	@Override
	public int getCount() {
		return articleList.size();
	}

	@Override
	public SingleArticle getItem(int position) {
		return articleList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutID, null);
			holder = new ViewHolder();
			holder.author = (TextView) convertView.findViewById(R.id.sa_author);
			holder.content = (TextView) convertView.findViewById(R.id.sa_content);
			holder.reply = (TextView) convertView.findViewById(R.id.sa_reply);

			holder.toolbar = (RelativeLayout) convertView.findViewById(R.id.sa_toolbar);
			holder.delete = (ImageButton) convertView.findViewById(R.id.sa_delete);
			holder.edit = (ImageButton) convertView.findViewById(R.id.sa_edit);

			holder.content.setMovementMethod(LinkMovementMethod.getInstance());
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final SingleArticle singleArticle = getItem(position);
		holder.author.setText(getAuthor(singleArticle) + "发表于" + DocParser.parseTime(singleArticle.getTimeValue()));
		holder.reply.setText(context.getResources().getString(R.string.reply_article) +  getFloorValue(position));
		String articleContent = singleArticle.getContent();
		holder.content.setText(Html.fromHtml(articleContent, null, TagDealer.getInstance(context)));
		holder.content.setTextSize(fontSize);

		if(userName.equals(singleArticle.getAuthorName())) {
			holder.toolbar.setVisibility(View.VISIBLE);
			holder.delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onDeleteArticle(singleArticle);
				}
			});

			holder.edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onModifyArticle(singleArticle);
				}
			});
		} else {
			holder.toolbar.setVisibility(View.GONE);
		}

		holder.reply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(singleArticle.getReplyUrl() == null) {
					Toast.makeText(context, "该帖无法回复!", Toast.LENGTH_SHORT).show();
					return;
				}
				onReplyArticle(singleArticle.getReplyUrl(), singleArticle.getAuthorName());
			}
		});

		holder.author.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(singleArticle.getAuthorName().equals("deliver")) {
					Toast.makeText(context, "系统管理员，无信息!", Toast.LENGTH_SHORT).show();
					return;
				}
				onClickAuthor(singleArticle.getAuthorName());
			}
		});
		return convertView;
	}

	private String getAuthor(SingleArticle singleArticle) {
		if(singleArticle.getAuthorName().equals(articleAuthor)) {
			return singleArticle.getAuthorName() + "(楼主)";
		} else {
			return singleArticle.getAuthorName();
		}
	}

	private String getFloorValue(int index) {
		if(index == 0) {
			return "楼主";
		} else if(index == 1) {
			return "沙发";
		} else if(index == 2) {
			return "板凳";
		} else {
			return index + "楼";
		}
	}

	private static final class ViewHolder{
		public TextView author;
		public TextView content;
		public TextView reply;
		public RelativeLayout toolbar;
		public ImageButton edit;
		public ImageButton delete;
	}
}
