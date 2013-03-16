package com.jerry.lily;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.model.SingleArticle;
import com.jerry.tagdealer.TagDealer;
import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.PicDownloader;
import com.jerry.widget.IOSAlertDialog;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.MarqueeTextView;
import com.jerry.widget.PageBackController;
import com.jerry.widget.PageBackController.PageBackListener;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

@SuppressLint("HandlerLeak")
public class ArticleActivity extends ListActivity implements IXListViewListener, OnClickListener, PageBackListener{
	private IOSWaitingDialog waitingDialog;
	private Article article;
	private String userName;
	private int currentPage = 0;

	private PageBackController controller;

	private List<Map<String, Object>> contentList;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		initComponents();
		PicDownloader.getInstance(this);
		initList();
		try {
			Method method = ListView.class.getMethod("setLayerType", new Class[]{int.class,Paint.class});
			if(method != null) {
				method.invoke(getListView(), View.LAYER_TYPE_SOFTWARE, null);
			}
		} catch (Exception e) {

		} 
	}

	public void refreshPic() {
		mHandler.sendEmptyMessage(19);
	}

	@Override
	public void onLowMemory() {
		PicDownloader.clearMemoryPicCache();
		super.onLowMemory();
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			if (waitingDialog != null) {
				waitingDialog.cancel();
			}
			switch (msg.what) {
			case 11:
				if(article == null || !article.isArticleListNotEmpty()) {
					return;
				}
				if(article.isNeedPullLoadMore()) {
					getListView().setFooterViewForbidden(false);
					onLoad();
				} else{
					getListView().setFooterViewForbidden(true);
				}
				if(getListAdapter() == null) {
					int layoutId = DatabaseDealer.getSettings(ArticleActivity.this).isNight() ? R.layout.list_single_article_night : R.layout.list_single_article;
					setListAdapter(new MyListAdapter(layoutId));
				} else {
					((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				}
				break;
			case 17:
				Toast.makeText(getApplicationContext(), "网络异常，请稍后重试!", Toast.LENGTH_SHORT).show();
				overridePendingTransition(R.anim.in_from_up,R.anim.out_to_down);
				break;
			case 19:
				if(getListAdapter() == null) {
					return;
				}
				((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				onLoad();
				break;
			case 20:
				onRefresh();
				break;
			}
		}
	};

	protected void onResume() {
		if(getListAdapter() != null) {
			getListAdapter().notifyDataSetChanged();
		}
		super.onResume();
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		startMenuActivity();
		return false;
	}

	private void startMenuActivity() {
		Intent menuIntent = new Intent(ArticleActivity.this, MenuActivity.class);
		List<Integer> removeElements = new ArrayList<Integer>();
		removeElements.add(Constants.POST_ARTICLE);
		menuIntent.putIntegerArrayListExtra("remove", (ArrayList<Integer>) removeElements);
		startActivityForResult(menuIntent, 0);
		overridePendingTransition(R.anim.in_from_up,R.anim.keep_origin);
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	public XListView getListView() {
		return (XListView)super.getListView();
	}

	private void onLoad() {
		getListView().stopRefresh();
		getListView().stopLoadMore();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}

	private void initComponents() {
		Button backButton = (Button) findViewById(R.id.bbutton);
		ImageButton more = (ImageButton) findViewById(R.id.article_more);
		controller = (PageBackController) findViewById(R.id.page_controller);
		MarqueeTextView title = (MarqueeTextView)findViewById(R.id.article_title);

		title.setText(this.getIntent().getStringExtra("title"));
		title.setOnClickListener(this);
		controller.setPageBackListener(this);
		controller.setSibling(getListView());
		backButton.setOnClickListener(this);
		more.setOnClickListener(this);
		getListView().setXListViewListener(ArticleActivity.this);

		userName = DatabaseDealer.query(this).getString("username");
	}

	private List<Map<String, Object>> getData() throws IOException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		article = new Article(this.getIntent().getStringExtra("contentUrl"), DatabaseDealer.getBlockList(ArticleActivity.this));
		for(int i = 0, size = article.getCurrentArticleCount(); i < size; i ++) {
			SingleArticle singleArticle = article.getSingleArticle(i);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("realAuthor", singleArticle.getAuthorName());
			map.put("author",getAuthor(singleArticle) + "发表于" + singleArticle.getDetailTime());
			map.put("floor", getFloorValue(article.getSingleArticleIndex(singleArticle)));
			map.put("content", singleArticle.getContent());
			list.add(map);
		}
		return list;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(resultCode) {
		case Constants.CANCEL_REPLY:
			break;
		case Constants.SEND_REPLY:
			onRefresh();
			break;
		case Constants.ADD_2_FAV:
			Article article = new Article();
			article.setAuthorName(article.getArticleAuthorName());
			article.setTitle(getIntent().getStringExtra("title"));
			article.setBoard(getIntent().getStringExtra("board"));
			article.setContentUrl(getIntent().getStringExtra("contentUrl"));
			DatabaseDealer.insertArticleCollection(ArticleActivity.this, article);
			Toast.makeText(getApplicationContext(), "文章收藏成功!", Toast.LENGTH_SHORT).show();
			break;
		case Constants.SHARE:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);   
			shareIntent.setType("text/plain");   
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, this.getIntent().getStringExtra("title"));   
			shareIntent.putExtra(Intent.EXTRA_TEXT, "南大小百合" + getIntent().getStringExtra("board") + "版  " + "《" + this.getIntent().getStringExtra("title") + "》" + '\n' + 
					"链接: " + this.getIntent().getStringExtra("contentUrl"));    
			shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
			startActivity(Intent.createChooser(shareIntent, "分享"));  
			break;
		case Constants.BROWSER:
			Intent url = new Intent(this, Url.class);
			url.putExtra("url", this.getIntent().getStringExtra("contentUrl"));
			startActivity(url);
			overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
		}
	}

	@Override
	protected void onDestroy() {
		if(waitingDialog != null) {
			waitingDialog.dismiss();
		}
		controller.onDestroy();
		TagDealer.getInstance(null);
		PicDownloader.stopDownloadThread();
		super.onDestroy();
	}

	private void initList() {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		if(getListAdapter() == null) {
			waitingDialog.show();
		}
		Thread gettingData = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					contentList = getData();
				} catch (IOException e) {
					mHandler.sendEmptyMessage(17);
				}
				mHandler.sendEmptyMessage(11);
			}
		});
		gettingData.start();
	}

	public static final class ViewHolder{
		public TextView author;
		public TextView content;
		public TextView reply;
		public RelativeLayout toolbar;
		public ImageButton edit;
		public ImageButton delete;
	}

	@Override
	public BaseAdapter getListAdapter() {
		return (BaseAdapter) super.getListAdapter();
	}

	private class MyListAdapter extends BaseAdapter {
		private int layoutID;
		private LayoutInflater  mInflater;
		private int fontSize;

		public MyListAdapter(int layoutID) {
			this.layoutID = layoutID;
			this.mInflater = ArticleActivity.this.getLayoutInflater();
			this.fontSize = DatabaseDealer.getSettings(ArticleActivity.this).getFontSize();
		}

		@Override
		public int getCount() {
			return contentList.size();
		}

		@Override
		public Object getItem(int position) {
			return contentList.get(position);
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
			Map<String, Object> map = contentList.get(position);
			holder.author.setText((String) map.get("author"));
			holder.reply.setText(getResources().getString(R.string.reply_article) +  map.get("floor"));
			String articleContent = (String) map.get("content");
			holder.content.setText(Html.fromHtml(articleContent, null, TagDealer.getInstance(ArticleActivity.this)));
			holder.content.setTextSize(fontSize);

			if(userName.equals(map.get("realAuthor"))) {
				holder.toolbar.setVisibility(View.VISIBLE);
				holder.delete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Thread thread = new Thread(new Runnable() {

									@Override
									public void run() {
										Message msg = Message.obtain();
										try {
											boolean success = article.getSingleArticle(position).deleteArticle(ArticleActivity.this);
											if(success) {
												msg.what = 20;
											} else {
												msg.what = 17;
											}
										} catch (IOException e) {
											msg.what = 17;
										} finally {
											mHandler.sendMessage(msg);
										}

									}
								});
								thread.start();
								dialog.dismiss();
								waitingDialog.show();
							}

						};
						new IOSAlertDialog.Builder(ArticleActivity.this).setTitle("注意").setMessage("确认删除本篇文章?").setPositiveButton("好", lis).setNegativeButtonText("取消").create().show();

					}
				});

				holder.edit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

					}
				});
			} else {
				holder.toolbar.setVisibility(View.GONE);
			}

			holder.reply.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(article.getSingleArticle(position).getReplyUrl() == null) {
						Toast.makeText(getApplicationContext(), "该帖无法回复!", Toast.LENGTH_SHORT).show();
						return;
					}
					Intent intent = new Intent(ArticleActivity.this, ReplyArticle.class);
					intent.putExtra("isTitleVisiable", false);
					intent.putExtra("board", getIntent().getStringExtra("board"));
					intent.putExtra("replyUrl", article.getSingleArticle(position).getReplyUrl());
					intent.putExtra("authorName", article.getSingleArticle(position).getAuthorName());
					intent.putExtra("contentUrl", getIntent().getStringExtra("contentUrl"));
					intent.putExtra("title", getIntent().getStringExtra("title"));
					startActivityForResult(intent, 1);
					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				}
			});

			holder.author.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(article.getSingleArticle(position).getAuthorName().equals("deliver")) {
						Toast.makeText(getApplicationContext(), "系统管理员，无信息!", Toast.LENGTH_SHORT).show();
						return;
					}
					Intent intent = new Intent(ArticleActivity.this, Author.class);
					intent.putExtra("authorUrl", article.getSingleArticle(position).getAuthorUrl());
					intent.putExtra("authorName", article.getSingleArticle(position).getAuthorName());
					startActivity(intent);
					overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				}
			});
			return convertView;
		}

	}

	private void addList() {
		currentPage += 30;
		Thread gettingData = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					addData();
				} catch (IOException e) {
					mHandler.sendEmptyMessage(17);
				}
				mHandler.sendEmptyMessage(11);
			}
		});
		gettingData.start();
	}

	private void addData() throws IOException {
		String pageString = "";
		if (currentPage > 0) {
			pageString = "&start=" + String.valueOf(currentPage);
		}
		int currentSize = article.getCurrentArticleCount();
		article.addSingleArticles(this.getIntent().getStringExtra("contentUrl") + pageString, DatabaseDealer.getBlockList(ArticleActivity.this));
		for(int i = currentSize, size = article.getCurrentArticleCount(); i < size; i ++) {
			SingleArticle singleArticle = article.getSingleArticle(i);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("realAuthor", singleArticle.getAuthorName());
			map.put("author",getAuthor(singleArticle) + "发表于" + singleArticle.getDetailTime());
			map.put("floor", getFloorValue(article.getSingleArticleIndex(singleArticle)));
			map.put("content", singleArticle.getContent());
			contentList.add(map);
		}
	}

	private String getAuthor(SingleArticle singleArticle) {
		if(singleArticle.getAuthorName().equals(article.getArticleAuthorName())) {
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

	@Override
	public void onLoadMore() {
		addList();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bbutton:
			onBackPressed(); 
			break;
		case R.id.article_more:
			startMenuActivity();
			break;
		case R.id.article_title:
			getListView().setSelectionAfterHeaderView();
			break;
		}

	}

	@Override
	public void onRefresh() {
		initList();
		onLoad();
	}

	@Override
	public void onPageBack() {
		onBackPressed();
	}
}
