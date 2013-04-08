package com.jerry.lily;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.model.ArticleGroup;
import com.jerry.model.LoginInfo;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.ShutDown;
import com.jerry.widget.HotListAdapter;
import com.jerry.widget.IOSAlertDialog;
import com.jerry.widget.MainPageAdapter;
import com.jerry.widget.SimpleEditAdapter;
import com.jerry.widget.TopListAdapter;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

@SuppressLint("HandlerLeak")
public class LilyActivity extends Activity implements OnClickListener,OnPageChangeListener{
	private ViewPager viewPager;
	private List<View> viewList;

	private List<Article> topList;
	private List<String> favList;
	private List<Article> hotList;

	private RadioGroup tabRadioGroup;

	private XListView[] listViewArray;
	private BaseAdapter[] adapterArray;

	private ImageButton setButton;
	private ImageButton boardEditButton;
	private ImageButton allBoardButton;
	private TextView mainTitle;

	private IOSAlertDialog quitDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainui);
		initComponents();
	}

	private void initComponents() {
		topList = getIntent().getParcelableArrayListExtra("topList");
		favList = getIntent().getStringArrayListExtra("favList");
		hotList = getIntent().getParcelableArrayListExtra("hotList");

		listViewArray = new XListView[3];
		adapterArray = new BaseAdapter[3];

		viewList = new ArrayList<View>();
		viewList.add(initTopListView());
		viewList.add(initBorardView());
		viewList.add(initHotView());
		viewList.add(initCenterView());

		viewPager = (ViewPager) findViewById(R.id.v_Pager);
		viewPager.setAdapter(new MainPageAdapter(viewList));
		viewPager.setOnPageChangeListener(this);

		tabRadioGroup = (RadioGroup) findViewById(R.id.radio);
		findViewById(R.id.top).setOnClickListener(this);
		findViewById(R.id.board).setOnClickListener(this);
		findViewById(R.id.hot).setOnClickListener(this);
		findViewById(R.id.center).setOnClickListener(this);

		setButton = ((ImageButton)findViewById(R.id.top_set));
		boardEditButton = ((ImageButton)findViewById(R.id.board_edit));
		allBoardButton = ((ImageButton)findViewById(R.id.all_board));
		mainTitle = ((TextView)findViewById(R.id.main_title));

		setButton.setOnClickListener(this);
		boardEditButton.setOnClickListener(this);
		allBoardButton.setOnClickListener(this);
		mainTitle.setOnClickListener(this);

		afterPageChanged(0);
		initQuitDialog();
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			listViewArray[msg.arg2].stopRefresh();
			if(msg.arg1 == 1) {
				Toast.makeText(LilyActivity.this, "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
			} else if(adapterArray[msg.arg2] != null) {
				adapterArray[msg.arg2].notifyDataSetChanged();
				listViewArray[msg.arg2].setRefreshTime(DocParser.getLastUpdateTime());
			}
		}
	};

	private View initTopListView() {
		View topView = View.inflate(this, R.layout.top, null);
		XListView topListView = (XListView) topView.findViewById(android.R.id.list);

		IXListViewListener topListViewListener = new IXListViewListener() {

			@Override
			public void onRefresh() {
				Thread refreshThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = Message.obtain();
						msg.arg2 = 0;
						try {
							List<Article> tmp = ArticleGroup.getTopArticleTitleList(LilyActivity.this).getArticleList();
							topList.clear();
							topList.addAll(tmp);
							msg.arg1 = 0;
						} catch (IOException e) {
							msg.arg1 = 1;
						} finally {
							mHandler.sendMessage(msg);
						}
					}
				});
				refreshThread.start();
			}
			@Override
			public void onLoadMore() {
			}
		};
		topListView.setXListViewListener(topListViewListener);
		topListView.setPullLoadEnable(false);
		topListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = new Intent(LilyActivity.this, ArticleActivity.class);
				intent.putExtra("board", topList.get(position - 1).getBoard());
				intent.putExtra("contentUrl", topList.get(position - 1).getContentUrl());
				intent.putExtra("title", topList.get(position - 1).getTitle());
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}

		});
		TopListAdapter topAdapter = new TopListAdapter(R.layout.list_top10, this, topList);
		pushListViewAndListAdapter(topListView, topAdapter, 0);
		return topView;
	}

	private void pushListViewAndListAdapter(XListView listView, BaseAdapter adapter, int index) {
		listViewArray[index] = listView;
		adapterArray[index] = adapter;
		listView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		favList.clear();
		favList.addAll(DatabaseDealer.getFavList(LilyActivity.this));
		Message msg = Message.obtain();
		msg.arg1 = 0;
		msg.arg2 = 1;
		mHandler.sendMessage(msg);
	}

	private View initBorardView() {
		View boardView = View.inflate(this, R.layout.top, null);
		XListView boardListView = (XListView) boardView.findViewById(android.R.id.list);
		IXListViewListener boardListViewListener = new IXListViewListener() {
			@Override
			public void onRefresh() {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = Message.obtain();
						msg.arg2 = 1;
						try {
							DatabaseDealer.addFav(LilyActivity.this, DocParser.synchronousFav(LoginInfo.getInstance(LilyActivity.this)));
							favList.clear();
							favList.addAll(DatabaseDealer.getFavList(LilyActivity.this));
							msg.arg1 = 0;
						} catch (IOException e) {
							msg.arg1 = 1;
						} finally {
							mHandler.sendMessage(msg);
						}
					}
				});
				thread.start();
			}

			@Override
			public void onLoadMore() {

			}
		};
		boardListView.setXListViewListener(boardListViewListener);
		boardListView.setPullLoadEnable(false);
		boardListView.setFooterViewForbidden(true);
		SimpleEditAdapter boardAdapter = new SimpleEditAdapter(this, R.layout.list_edit_item, R.id.list_textview, R.id.list_select, R.id.list_delete, favList) {
			@Override
			protected void onDelete(final int position) {
				android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DatabaseDealer.deleteSpecialFav(LilyActivity.this, getItem(position));
						onResume();
						dialog.dismiss();

					}
				};
				new IOSAlertDialog.Builder(LilyActivity.this).setTitle("注意").setMessage("即将删除" + getItem(position) + '\n' + "是否继续?").setPositiveButton("继续", listener).create().show();
			}
		};
		boardListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = new Intent(LilyActivity.this, BoardList.class);
				intent.putExtra("boardName", favList.get(position - 1));
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});
		pushListViewAndListAdapter(boardListView, boardAdapter, 1);
		return boardView;
	}

	private View initHotView() {
		View hotView = View.inflate(this, R.layout.top, null);
		XListView hotListView = (XListView) hotView.findViewById(android.R.id.list);
		IXListViewListener hotListViewListener = new IXListViewListener() {
			@Override
			public void onRefresh() {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = Message.obtain();
						msg.arg2 = 2;
						try {
							List<Article> tmp = ArticleGroup.getHotArticleTitleList().getArticleList();
							hotList.clear();
							hotList.addAll(tmp);
							msg.arg1 = 0;
						} catch (IOException e) {
							msg.arg1 = 1;
						} finally {
							mHandler.sendMessage(msg);
						}
					}
				});
				thread.start();
			}

			@Override
			public void onLoadMore() {
			}
		};
		hotListView.setXListViewListener(hotListViewListener);
		hotListView.setPullLoadEnable(false);
		int textResourceId = R.layout.list_hot;
		hotListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = new Intent(LilyActivity.this, ArticleActivity.class);
				intent.putExtra("board", hotList.get(position - 1).getBoard());
				intent.putExtra("contentUrl", hotList.get(position - 1).getContentUrl());
				intent.putExtra("title", hotList.get(position - 1).getTitle());
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});
		HotListAdapter hotAdapter = new HotListAdapter(textResourceId, LilyActivity.this, hotList);
		pushListViewAndListAdapter(hotListView, hotAdapter, 2);
		return hotView;
	}

	private View initCenterView() {
		View centerView = View.inflate(this, R.layout.center, null);
		centerView.findViewById(R.id.center_search).setOnClickListener(this);
		centerView.findViewById(R.id.center_collection).setOnClickListener(this);
		centerView.findViewById(R.id.center_friends).setOnClickListener(this);
		centerView.findViewById(R.id.center_mail).setOnClickListener(this);
		return centerView;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.center_search:
			startActivity(new Intent(LilyActivity.this, SearchArticle.class));
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case R.id.center_collection:
			startActivity(new Intent(LilyActivity.this, ArticleCollection.class));
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case R.id.center_friends:
			startActivity(new Intent(LilyActivity.this, Friends.class));
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case R.id.center_mail:
			startActivity(new Intent(LilyActivity.this, Mail.class));
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case R.id.top_set:
			Intent setIntent = new Intent(LilyActivity.this, Set.class);
			startActivity(setIntent);
			overridePendingTransition(R.anim.in_from_up,R.anim.keep_origin);
			break;
		case R.id.board_edit:
			((SimpleEditAdapter)adapterArray[1]).changeEditingState();
			break;
		case R.id.all_board:
			Intent boardIntent = new Intent(LilyActivity.this, AllBoard.class);
			startActivity(boardIntent);
			overridePendingTransition(R.anim.in_from_up,R.anim.keep_origin);
			break;
		case R.id.main_title:
			if(viewPager.getCurrentItem() <= 3) {
				listViewArray[viewPager.getCurrentItem()].setSelectionAfterHeaderView();
			}
			break;
		case R.id.top:
		case R.id.board:
		case R.id.hot:
		case R.id.center:
			onCheckRadioButton(v.getId());
			break;
		}

	}

	private void onCheckRadioButton(int buttonId) {
		tabRadioGroup.check(buttonId);
		int index = 0;
		switch (buttonId) {
		case R.id.top:
			index = 0;
			break;
		case R.id.board:
			index = 1;
			break;
		case R.id.hot:
			index = 2;
			break;
		case R.id.center:
			index = 3;
			break;
		}
		viewPager.setCurrentItem(index, false);
		viewList.get(index).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(final int index) {
		afterPageChanged(index);
		int id = R.id.top;
		switch (index) {
		case 1:
			id = R.id.board;
			break;
		case 2:
			id = R.id.hot;
			break;
		case 3:
			id = R.id.center;
			break;
		}
		tabRadioGroup.check(id);
	}

	private void afterPageChanged(int index) {
		if(index == 0) {
			mainTitle.setText(getResources().getString(R.string.top));
			setButton.setVisibility(View.VISIBLE);
			boardEditButton.setVisibility(View.GONE);
			allBoardButton.setVisibility(View.GONE);
		} else if(index == 1) {
			mainTitle.setText(getResources().getString(R.string.pre_board));
			setButton.setVisibility(View.GONE);
			boardEditButton.setVisibility(View.VISIBLE);
			allBoardButton.setVisibility(View.VISIBLE);
		} else if(index == 2) {
			mainTitle.setText(getResources().getString(R.string.hot_board));
			setButton.setVisibility(View.GONE);
			boardEditButton.setVisibility(View.GONE);
			allBoardButton.setVisibility(View.GONE);
		} else if(index == 3) {
			mainTitle.setText(getResources().getString(R.string.information2));
			setButton.setVisibility(View.GONE);
			boardEditButton.setVisibility(View.GONE);
			allBoardButton.setVisibility(View.GONE);
		}
	}

	private void initQuitDialog() {
		android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ShutDown.shutDownActivity(LilyActivity.this);
				finish();
				System.exit(0);
			}
		};
		quitDialog = new IOSAlertDialog.Builder(this).setTitle("提示").setMessage("确定退出应用程序?").setPositiveButton("确定", listener).create();
	}

	@Override
	public void onBackPressed() {
		quitDialog.show();
	}
}  
