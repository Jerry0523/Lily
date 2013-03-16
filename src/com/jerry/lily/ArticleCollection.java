package com.jerry.lily;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jerry.model.Article;
import com.jerry.utils.DatabaseDealer;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ArticleCollection extends ListActivity{
	private Button back;
	private SimpleAdapter mAdapter;
	private List<Map<String, Object>> dataMap;
	private List<Article> articleList;
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		if(DatabaseDealer.getSettings(ArticleCollection.this).isNight()) {
			((LinearLayout)findViewById(R.id.search_result_body)).setBackgroundDrawable(null);
		}
		((TextView)findViewById(R.id.search_result_title)).setText("收藏的文章");
		initComponents();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, ArticleActivity.class);
		intent.putExtra("board", articleList.get(position).getBoard());
		intent.putExtra("contentUrl", articleList.get(position).getContentUrl());
		intent.putExtra("title", articleList.get(position).getTitle());
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	private void initComponents() {
		articleList = DatabaseDealer.getArticleColliection(ArticleCollection.this);
		dataMap = getData();
		back = (Button)findViewById(R.id.search_result_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		int textResourceId = DatabaseDealer.getSettings(ArticleCollection.this).isNight() ? R.layout.list_top10_night : R.layout.list_top10;
		mAdapter = new SimpleAdapter(this, dataMap, textResourceId, new String[] {"title","author","board"}, new int[] {R.id.title, R.id.author, R.id.board});
		setListAdapter(mAdapter);
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
