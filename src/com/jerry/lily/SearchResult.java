package com.jerry.lily;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;

public class SearchResult extends ListActivity{
	private Button back;
	private IOSWaitingDialog waitingDialog;
	private List<Article> articleList;
	private List<Map<String, Object>> dataMap;
	private SimpleAdapter mAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		if(DatabaseDealer.getSettings(SearchResult.this).isNight()) {
			((LinearLayout)findViewById(R.id.search_result_body)).setBackgroundDrawable(null);
		}
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
			switch (msg.arg1) {
			case 10:
				Toast.makeText(getApplicationContext(), "网络异常，请稍后重试!", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case 11:
				Toast.makeText(getApplicationContext(), "无搜索结果，请重新输入!", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case 12:
				initList();
				break;
			}
		}
	};
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			String url = DocParser.getAllTopicArtileUrl(articleList.get(position).getContentUrl());
			String title = articleList.get(position).getTitle();
			title = title.replace("Re: ", "");
			Intent intent = new Intent(this, ArticleActivity.class);
			intent.putExtra("board", articleList.get(position).getBoard());
			intent.putExtra("contentUrl", url);
			intent.putExtra("title", title);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
		} catch (IOException e) {
			Message msg = Message.obtain();
			msg.arg1 = 10;
			mHandler.sendMessage(msg);
		}
	}
	
	private void initList() {
		int textResourceId = DatabaseDealer.getSettings(SearchResult.this).isNight() ? R.layout.list_top10_night : R.layout.list_top10;
		mAdapter = new SimpleAdapter(this, dataMap, textResourceId, new String[] {"title","author","board"}, new int[] {R.id.title, R.id.author, R.id.board});
		setListAdapter(mAdapter);
	}

	private void initComponents() {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();
		back = (Button)findViewById(R.id.search_result_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		final String author = getIntent().getStringExtra("author");
		final String title = getIntent().getStringExtra("title");
		final String title2 = getIntent().getStringExtra("title2");
		final int time = getIntent().getIntExtra("time", 30);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					articleList = DocParser.search(author, title, title2, time);
					if(articleList.size() == 0) {
						msg.arg1 = 11;
						mHandler.sendMessage(msg);
						return;
					}
					dataMap = getData();
					msg.arg1 = 12;
					mHandler.sendMessage(msg);
				} catch (IOException e) {
					msg.arg1 = 10;
					mHandler.sendMessage(msg);
				}
				
			}
		});
		thread.start();
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(Article article : articleList) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", article.getTitle());
			map.put("author", "作者:" + article.getAuthorName());
			map.put("board", "版块:"+ article.getBoard());
			list.add(map);
		}
		return list;
	}
}
