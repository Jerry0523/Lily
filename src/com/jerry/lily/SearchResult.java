package com.jerry.lily;

import java.io.IOException;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.TopListAdapter;

public class SearchResult extends ListActivity{
	private IOSWaitingDialog waitingDialog;
	private List<Article> articleList;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		initComponents();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(waitingDialog != null) {
				waitingDialog.dismiss();
			}
			switch (msg.what) {
			case 2:
				Toast.makeText(getApplicationContext(), "网络异常，请稍后重试!", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case 1:
				Toast.makeText(getApplicationContext(), "无搜索结果，请重新输入!", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case 0:
				int textResourceId = DatabaseDealer.getSettings(SearchResult.this).isNight() ? R.layout.list_item_night : R.layout.list_item;
				setListAdapter(new TopListAdapter(textResourceId, SearchResult.this, articleList));
				break;
			case 3:
				String title = articleList.get(msg.arg1).getTitle();
				title = title.replace("Re: ", "");
				Intent intent = new Intent(SearchResult.this, ArticleActivity.class);
				intent.putExtra("board", articleList.get(msg.arg1).getGroup());
				intent.putExtra("contentUrl", (String)msg.obj);
				intent.putExtra("title", title);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		waitingDialog.show();
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					String url = DocParser.getAllTopicArtileUrl(articleList.get(position).getContentUrl());
					msg.what = 3;
					msg.obj = url;
					msg.arg1 = position;
				} catch (IOException e) {
					msg.what = 2;
				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void initComponents() {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();
		findViewById(R.id.search_result_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		final String author = getIntent().getStringExtra("author");
		final String title = getIntent().getStringExtra("title");
		final String title2 = getIntent().getStringExtra("title2");
		final int time = getIntent().getIntExtra("time", 30);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int result = 0;
				try {
					articleList = DocParser.search(author, title, title2, time);
					if(articleList.size() == 0) {
						result = 1;
					}
				} catch (IOException e) {
					result = 2;
				} finally {
					mHandler.sendEmptyMessage(result);
				}

			}
		}).start();
	}
}
