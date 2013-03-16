package com.jerry.lily;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.PageBackController;
import com.jerry.widget.PageBackController.PageBackListener;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

public class BoardList extends ListActivity implements IXListViewListener, OnClickListener, PageBackListener{
	private String boardName;
	private List<Article> articleList;
	private IOSWaitingDialog waitingDialog;
	private List<Map<String, String>> contentList;
	private boolean isPull = false;
	private PageBackController controller;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleboard);
		initComponents();
		initList("http://bbs.nju.edu.cn/bbstdoc?board=" + boardName);
	}
	
	@Override
	protected void onDestroy() {
		controller.onDestroy();
		super.onDestroy();
	}

	@Override
	public XListView getListView() {
		return (XListView) super.getListView();
	}

	private void initComponents() {
		boardName = getIntent().getStringExtra("boardName");
		Button backButton = (Button) findViewById(R.id.sb_bbutton);
		ImageButton moreMenu = (ImageButton) findViewById(R.id.sb_menu);
		TextView title = (TextView) findViewById(R.id.sb_board);
		controller = (PageBackController) findViewById(R.id.page_controller);
		title.setText(boardName);

		backButton.setOnClickListener(this);
		moreMenu.setOnClickListener(this);
		title.setOnClickListener(this);
		controller.setPageBackListener(this);
		controller.setSibling(getListView());
		getListView().setXListViewListener(this);
	}

	public static final class ViewHolder{
		public TextView title;
		public TextView author;
		public TextView reply;
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 12:
				if (waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case 13:
				if (waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				break;
			case 16:
				afterThread();
				break;
			case 17:
				Toast.makeText(getApplicationContext(), "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				onLoad();
				break;
			}
		}
	};

	private void onLoad() {
		getListView().stopRefresh();
		getListView().stopLoadMore();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}

	private void afterThread() {
		if(getListAdapter() == null) {
			setListAdapter(new MyAdapter(DatabaseDealer.getSettings(BoardList.this).isNight() ? R.layout.list_board_article_night : R.layout.list_board_article));
		} else {
			getListAdapter().notifyDataSetChanged();
		}
		onLoad();
		if (waitingDialog != null) {
			waitingDialog.dismiss();
		}

	}

	private void initList(String url) {
		final String boardUrl = url;
		if(getListAdapter() == null) {
			if(waitingDialog == null) {
				waitingDialog = IOSWaitingDialog.createDialog(this);
			}
			waitingDialog.show();
		}

		(new AsyncTask<Object, Object, List<Article> >() {

			@Override
			protected void onPostExecute(List<Article> result) {
				articleList = (result == null ? new ArrayList<Article>() : result);
				Collections.reverse(articleList);
				contentList = getData();
				afterThread();
				if(articleList.size() == 0) {
					Toast.makeText(getApplicationContext(), "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			protected List<Article>  doInBackground(Object... params) {
				try {
					return DocParser.getBoardArticleTitleList(boardUrl, boardName, DatabaseDealer.getBlockList(BoardList.this));
				} catch (Exception e) {
					e.printStackTrace();
				} 
				return null;
			}
		}).execute("");
	}

	private void loadMoreList(String url) {
		final String boardUrl = url;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message  msg = Message.obtain();
				List<Article> more = null;
				try {
					more = DocParser.getBoardArticleTitleList(boardUrl, boardName, DatabaseDealer.getBlockList(BoardList.this));
				} catch (IOException e) {
					msg.arg1 = 17;
					mHandler.sendMessage(msg);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(more == null) {
					msg.arg1 = 17;
					mHandler.sendMessage(msg);
					return;
				}
				Collections.reverse(more);
				articleList.set(0, more.get(0));
				addData(more);
				if(!isPull) {
					isPull = true;
				}
				msg.arg1 = 16;
				mHandler.sendMessage(msg);
			}
		});
		thread.start();
	}
	private void addData(List<Article> more) {
		if(contentList == null) {
			contentList = new ArrayList<Map<String, String>>();
		}
		int start = isPull ? 2 : 1;
		for (int i = start; i < more.size(); i ++) {
			Article article = more.get(i);
			articleList.add(article);
			Map<String, String> map = new HashMap<String, String>();
			map.put("title", article.getTitle());
			map.put("author", article.getAuthorName() + "发表于" + article.getDetailTime());
			map.put("reply", article.getReplyCount() + "/" + article.getViewCount());
			contentList.add(map);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	private void startMenuActivity() {
		Intent menuIntent = new Intent(BoardList.this, MenuActivity.class);
		List<Integer> removeElements = new ArrayList<Integer>();
		removeElements.add(Constants.BROWSER);
		menuIntent.putIntegerArrayListExtra("remove", (ArrayList<Integer>) removeElements);
		startActivityForResult(menuIntent, 1);
		overridePendingTransition(R.anim.in_from_up,R.anim.keep_origin);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(resultCode) {
		case Constants.ADD_2_FAV:
			boolean isFav = DatabaseDealer.isFav(BoardList.this, boardName);
			if (isFav) {
				Toast.makeText(BoardList.this, "该版面已在收藏夹中", Toast.LENGTH_SHORT).show();
			} else {
				DatabaseDealer.addBoard2LocalFav(BoardList.this, boardName);
				Toast.makeText(BoardList.this, "已将该版面加入本地收藏夹", Toast.LENGTH_SHORT).show();
			}
			break;
		case Constants.POST_ARTICLE:
			Intent intent = new Intent(BoardList.this, ReplyArticle.class);
			intent.putExtra("isTitleVisiable", true);
			intent.putExtra("board", boardName);
			startActivityForResult(intent, 1);
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case Constants.SEND_REPLY:
			initList("http://bbs.nju.edu.cn/bbstdoc?board=" + boardName);
			break;
		case Constants.CANCEL_REPLY:
			break;
		case Constants.SHARE:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);   
			shareIntent.setType("text/plain");   
			shareIntent.putExtra(Intent.EXTRA_SUBJECT,"南大小百合" + boardName);   
			shareIntent.putExtra(Intent.EXTRA_TEXT, "南大小百合" + boardName + "版  链接:" + "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName);    
			shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
			startActivity(Intent.createChooser(shareIntent, "分享"));  
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		startMenuActivity();
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(position == articleList.size()) {
			return;
		}
		Intent intent = new Intent(this, ArticleActivity.class);
		intent.putExtra("board", boardName);
		intent.putExtra("contentUrl", articleList.get(position).getContentUrl());
		intent.putExtra("title", articleList.get(position).getTitle());
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	private List<Map<String, String>> getData() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (int i = 1; i < articleList.size(); i ++) {
			Article article = articleList.get(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("title", article.getTitle());
			map.put("author", article.getAuthorName() + "发表于" + article.getDetailTime());
			map.put("reply", article.getReplyCount() + "/" + article.getViewCount());
			list.add(map);
		}
		return list;
	}

	@Override
	public void onRefresh() {
		final String boardUrl = "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName;
		initList(boardUrl);
	}

	@Override
	public void onLoadMore() {
		final String boardUrl = "http://bbs.nju.edu.cn/" + articleList.get(0).getBoard();
		loadMoreList(boardUrl);
	}

	@Override
	public MyAdapter getListAdapter() {
		return (MyAdapter) super.getListAdapter();
	}

	private class MyAdapter extends BaseAdapter {

		private int layoutID;
		private LayoutInflater  mInflater;

		public MyAdapter(int layoutID) {
			this.layoutID = layoutID;
			this.mInflater = BoardList.this.getLayoutInflater();
		}
		@Override
		public int getCount() {
			return contentList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
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
			Map<String, String> map = contentList.get(position);
			holder.title.setText(map.get("title").substring(1));
			holder.author.setText(map.get("author"));
			holder.reply.setText(map.get("reply"));
			return convertView;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sb_bbutton:
			onBackPressed();
			break;
		case R.id.sb_menu:
			startMenuActivity();
			break;
		case R.id.sb_board:
			getListView().setSelectionAfterHeaderView();
			break;
		}
	}

	@Override
	public void onPageBack() {
		onBackPressed();
	}

}
