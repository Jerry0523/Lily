package com.jerry.lily;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.model.ArticleGroup;
import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.BoardListAdapter;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.PageBackController;
import com.jerry.widget.PageBackController.PageBackListener;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

@SuppressLint("HandlerLeak")
public class BoardList extends ListActivity implements IXListViewListener, OnClickListener, PageBackListener{
	private String boardName;
	private List<Article> articleList;
	private IOSWaitingDialog waitingDialog;
	private PageBackController controller;
	private String nextTargetUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleboard);
		waitingDialog = IOSWaitingDialog.createDialog(this);
		waitingDialog.show();
		initComponents();
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
		articleList = new ArrayList<Article>();
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
		initList();
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(getListAdapter() == null) {
					setListAdapter(new BoardListAdapter(BoardList.this, R.layout.list_board_article, articleList));
				} else {
					getListAdapter().notifyDataSetChanged();
				}
				break;
			case 1:
				Toast.makeText(BoardList.this, "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				break;
			}
			onLoad();
			if (waitingDialog != null) {
				waitingDialog.dismiss();
			}
		}
	};

	private void onLoad() {
		getListView().stopRefresh();
		getListView().stopLoadMore();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}

	private void initList() {
		final String url = "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName;
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				int result = 0;
				try {
					ArticleGroup articleGroup = ArticleGroup.getBoardArticleTitleList(url, boardName, BoardList.this);
					if(articleGroup == null) {
						throw new IOException();
					}
					List<Article> list = articleGroup.getArticleList();
					nextTargetUrl = articleGroup.getNextTargetUrl();
					if(list == null) {
						throw new IOException();
					}
					articleList.clear();
					articleList.addAll(list);
				} catch (IOException e) {
					result = 1;
				} catch (ParseException e) {
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}
			}
		});
		thread.start();
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
			initList();
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
		position--;
		Intent intent = new Intent(this, ArticleActivity.class);
		intent.putExtra("board", boardName);
		intent.putExtra("contentUrl", articleList.get(position).getContentUrl());
		intent.putExtra("title", articleList.get(position).getTitle());
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	@Override
	public void onRefresh() {
		initList();
	}

	@Override
	public void onLoadMore() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int result = 0;
				try {
					ArticleGroup articleGroup = ArticleGroup.getBoardArticleTitleList(nextTargetUrl, boardName, BoardList.this);
					if(articleGroup == null) {
						throw new IOException();
					}
					List<Article> more = articleGroup.getArticleList();
					more.remove(0);
					nextTargetUrl = articleGroup.getNextTargetUrl();
					articleList.addAll(more);
				} catch (IOException e) {
					result = 1;
				} catch (ParseException e) {
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}
			}
		});
		thread.start();
	}

	@Override
	public BoardListAdapter getListAdapter() {
		return (BoardListAdapter) super.getListAdapter();
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
