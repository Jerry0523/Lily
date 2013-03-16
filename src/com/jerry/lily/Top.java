package com.jerry.lily;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.utils.AnimCommon;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.ShutDown;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

@SuppressLint("HandlerLeak")
public class Top extends ListActivity implements IXListViewListener, OnClickListener{
	private List<Article> articleList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.top);
		articleList = getIntent().getParcelableArrayListExtra("topList");
		((ImageButton)findViewById(R.id.top_set)).setOnClickListener(this);
		getListView().setXListViewListener(this);
		getListView().setPullLoadEnable(false);
		int textResourceId = DatabaseDealer.getSettings(Top.this).isNight() ? R.layout.list_top10_night : R.layout.list_top10;
		setListAdapter(new TopListAdapter(textResourceId));
	}
	
	private void onLoad() {
		getListView().stopRefresh();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}
	
	@Override
	public XListView getListView() {
		return (XListView)super.getListView();
	}
	
	@Override
	public TopListAdapter getListAdapter() {
		return (TopListAdapter)super.getListAdapter();
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, ArticleActivity.class);
		intent.putExtra("board", articleList.get(position - 1).getBoard());
		intent.putExtra("contentUrl", articleList.get(position - 1).getContentUrl());
		intent.putExtra("title", articleList.get(position - 1).getTitle());
		AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		ShutDown.shutDownActivity(this);
	}

	@Override
	public void onRefresh() {
		Thread refreshThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					articleList = DocParser.getArticleTitleList("http://bbs.nju.edu.cn/bbstop10", DatabaseDealer.getBlockList(Top.this));
					msg.arg1 = 11;
					mHandler.sendMessage(msg);
				} catch (IOException e) {
					msg.arg1 = 12;
					mHandler.sendMessage(msg);
				}
			}
		});
		refreshThread.start();
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.top_set) {
			Intent intent = new Intent(getApplicationContext(), Set.class);
			AnimCommon.set(R.anim.in_from_up,R.anim.keep_origin); 
			startActivity(intent);
		}
	}
	
	public static final class ViewHolder{
		public TextView title;
		public TextView author;
		public TextView board;
	}
	
	private class TopListAdapter extends BaseAdapter {
		private int layoutID;
		private LayoutInflater  mInflater;

		public TopListAdapter(int layoutID) {
			this.layoutID = layoutID;
			this.mInflater = Top.this.getLayoutInflater();
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

	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		
	}

}
