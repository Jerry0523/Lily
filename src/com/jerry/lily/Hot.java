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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.utils.AnimCommon;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.ShutDown;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

public class Hot extends ListActivity implements IXListViewListener{
	private List<Article> hotList;
	private List<Map<String, Object>> dataMap = new ArrayList<Map<String,Object>>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hot);
		hotList = getIntent().getParcelableArrayListExtra("hotList");
		getData();
		getListView().setXListViewListener(this);
		getListView().setPullLoadEnable(false);
		int textResourceId = DatabaseDealer.getSettings(Hot.this).isNight() ? R.layout.list_hot_night : R.layout.list_hot;
		setListAdapter(new SimpleAdapter(this, dataMap, textResourceId, new String[] {"title","board"}, new int[] {R.id.lh_title, R.id.lh_board}));
		((TextView)findViewById(R.id.hot_title)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getListView().setSelectionAfterHeaderView();
			}
		});
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1) {
			case 10:
				Toast.makeText(getApplicationContext(), "Õ¯¬Á“Ï≥££¨«Î…‘∫Û÷ÿ ‘!", Toast.LENGTH_SHORT).show();
				break;
			case 11:
				getListAdapter().notifyDataSetChanged();
				onLoad();
				break;
			case 12:
				Toast.makeText(getApplicationContext(), "Õ¯¬Á“Ï≥££¨«Î…‘∫Û÷ÿ ‘!", Toast.LENGTH_SHORT).show();
				onLoad();
				break;
			}
		}
	};

	private void onLoad() {
		getListView().stopRefresh();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}
	
	@Override
	public BaseAdapter getListAdapter() {
		return (BaseAdapter) super.getListAdapter();
	}

	@Override
	public XListView getListView() {
		return (XListView) super.getListView();
	}

	private void getData() {
		dataMap.clear();
		for(Article article : hotList) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", article.getTitle());
			map.put("board", "∞ÊøÈ:"+ article.getBoard());
			dataMap.add(map);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, ArticleActivity.class);
		intent.putExtra("board", hotList.get(position - 1).getBoard());
		intent.putExtra("contentUrl", hotList.get(position - 1).getContentUrl());
		intent.putExtra("title", hotList.get(position - 1).getTitle());
		startActivity(intent);
		AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	@Override
	public void onBackPressed() {
		ShutDown.shutDownActivity(this);
	}

	@Override
	public void onRefresh() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					DocParser.getHotArticleTitleList("http://bbs.nju.edu.cn/bbstopall", hotList);
					getData();
					msg.arg1 = 11;
					mHandler.sendMessage(msg);
				} catch (IOException e) {
					msg.arg1 = 12;
					mHandler.sendMessage(msg);
					return;
				}
			}
		});
		thread.start();
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub

	}
}
