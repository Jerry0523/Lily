package com.jerry.lily;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.model.SingleArticle;
import com.jerry.tagdealer.TagDealer;
import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.PicDownloader;
import com.jerry.widget.ArticleAdapter;
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
		mHandler.sendEmptyMessage(2);
	}

	@Override
	public void onLowMemory() {
		PicDownloader.getInstance().clearMemoryPicCache();
		super.onLowMemory();
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(article == null || !article.isArticleListNotEmpty()) {
					return;
				}
				if(article.isNeedPullLoadMore()) {
					getListView().setFooterViewForbidden(false);
				} else{
					getListView().setFooterViewForbidden(true);
				}
				if(getListAdapter() == null) {
					setListAdapter(getArticleAdapter());
				} else {
					((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				}
				break;
			case 1:
				Toast.makeText(getApplicationContext(), "网络异常，请稍后重试!", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				if(getListAdapter() == null) {
					return;
				}
				((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				break;
			case 3:
				onRefresh();
				break;
			}
			onLoad();
			if (waitingDialog != null) {
				waitingDialog.dismiss();
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
		controller = (PageBackController) findViewById(R.id.page_controller);
		MarqueeTextView title = (MarqueeTextView)findViewById(R.id.article_title);

		title.setText(this.getIntent().getStringExtra("title"));
		title.setOnClickListener(this);
		controller.setPageBackListener(this);
		controller.setSibling(getListView());
		findViewById(R.id.bbutton).setOnClickListener(this);
		findViewById(R.id.article_more).setOnClickListener(this);
		getListView().setXListViewListener(ArticleActivity.this);
		userName = DatabaseDealer.query(this).getString("username");

		waitingDialog = IOSWaitingDialog.createDialog(this);
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
			article.setGroup(getIntent().getStringExtra("board"));
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
		PicDownloader.getInstance().stopDownloadThread();
		super.onDestroy();
	}

	private void initList() {
		if(getListAdapter() == null) {
			waitingDialog.show();
		}
		Thread gettingData = new Thread(new Runnable() {
			@Override
			public void run() {
				int result = 0;
				try {
					if(article == null) {
						article = new Article(ArticleActivity.this.getIntent().getStringExtra("contentUrl"), ArticleActivity.this, false);
					} else {
						currentPage = 0;
						article.refresh(ArticleActivity.this.getIntent().getStringExtra("contentUrl"), ArticleActivity.this);
					}
				} catch (Exception e) {
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}
			}
		});
		gettingData.start();
	}

	@Override
	public BaseAdapter getListAdapter() {
		return (BaseAdapter) super.getListAdapter();
	}



	private void addList() {
		Thread gettingData = new Thread(new Runnable() {
			@Override
			public void run() {
				int result = 0;
				try {
					currentPage += 30;
					String pageString = "";
					if (currentPage > 0) {
						pageString = "&start=" + String.valueOf(currentPage);
					}
					article.addSingleArticles(ArticleActivity.this.getIntent().getStringExtra("contentUrl") + pageString, ArticleActivity.this);
				} catch (IOException e) {
					currentPage -= 30;
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}
			}
		});
		gettingData.start();
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
	}

	@Override
	public void onPageBack() {
		onBackPressed();
	}

	private ArticleAdapter getArticleAdapter() {
		int layoutId = DatabaseDealer.getSettings(ArticleActivity.this).isNight() ? R.layout.list_single_article_night : R.layout.list_single_article;
		return new ArticleAdapter(layoutId, ArticleActivity.this, article, userName) {

			@Override
			public void onReplyArticle(String replyUrl, String authorName) {
				Intent intent = new Intent(ArticleActivity.this, ReplyArticle.class);
				intent.putExtra("isTitleVisiable", false);
				intent.putExtra("board", getIntent().getStringExtra("board"));
				intent.putExtra("replyUrl", replyUrl);
				intent.putExtra("authorName", authorName);
				intent.putExtra("contentUrl", getIntent().getStringExtra("contentUrl"));
				intent.putExtra("title", getIntent().getStringExtra("title"));
				startActivityForResult(intent, 1);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}

			@Override
			public void onModifyArticle(SingleArticle sa) {
				Intent intent = new Intent(ArticleActivity.this, ReplyArticle.class);
				intent.putExtra("isTitleVisiable", false);
				intent.putExtra("isModify", true);
				intent.putExtra("authorName", sa.getAuthorName());
				intent.putExtra("time", sa.getOriginalTimeString());
				intent.putExtra("content", sa.getOriginalContent());
				intent.putExtra("board", getIntent().getStringExtra("board"));
				intent.putExtra("replyUrl", sa.getReplyUrl());
				intent.putExtra("title", getIntent().getStringExtra("title"));
				startActivityForResult(intent, 1);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}

			@Override
			public void onDeleteArticle(final SingleArticle sa) {
				DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						Thread thread = new Thread(new Runnable() {

							@Override
							public void run() {
								int result = 3;
								try {
									boolean success = sa.deleteArticle(ArticleActivity.this);
									if(!success) {
										result = 1;
									}
								} catch (IOException e) {
									result = 1;
								} finally {
									mHandler.sendEmptyMessage(result);
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

			@Override
			public void onClickAuthor(String authorName) {
				Intent intent = new Intent(ArticleActivity.this, Author.class);
				intent.putExtra("authorName", authorName);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		};
	}
}
