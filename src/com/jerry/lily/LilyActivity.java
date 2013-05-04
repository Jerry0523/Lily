package com.jerry.lily;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
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
	private ImageView imageView;// 动画图片
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度

	private long mExitTime;


	private List<View> viewList;

	private List<Article> topList;
	private List<String> favList;
	private List<Article> hotList;

	private XListView[] listViewArray;
	private BaseAdapter[] adapterArray;

	private ImageButton setButton;
	private ImageButton boardEditButton;
	private ImageButton allBoardButton;

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

		initViewPager();

		setButton = ((ImageButton)findViewById(R.id.top_set));
		boardEditButton = ((ImageButton)findViewById(R.id.board_edit));
		allBoardButton = ((ImageButton)findViewById(R.id.all_board));

		setButton.setOnClickListener(this);
		boardEditButton.setOnClickListener(this);
		allBoardButton.setOnClickListener(this);
	}

	private void initViewPager() {
		imageView= (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.cursor).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 4 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageView.setImageMatrix(matrix);// 设置动画初始位置

		viewPager = (ViewPager) findViewById(R.id.v_Pager);
		viewPager.setAdapter(new MainPageAdapter(viewList));
		viewPager.setOnPageChangeListener(this);

		findViewById(R.id.main_tab_top).setOnClickListener(this);
		findViewById(R.id.main_tab_board).setOnClickListener(this);
		findViewById(R.id.main_tab_hot).setOnClickListener(this);
		findViewById(R.id.main_tab_center).setOnClickListener(this);
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
							List<Article> tmp = ArticleGroup.getTopArticleGroup(LilyActivity.this).getArticleList();
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
		topListView.setFooterViewForbidden(true);
		topListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = new Intent(LilyActivity.this, ArticleActivity.class);
				intent.putExtra("board", topList.get(position - 1).getGroup());
				intent.putExtra("contentUrl", topList.get(position - 1).getContentUrl());
				intent.putExtra("title", topList.get(position - 1).getTitle());
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}

		});
		TopListAdapter topAdapter = new TopListAdapter(R.layout.list_item, this, topList);
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
							List<Article> tmp = ArticleGroup.getHotArticleGroup().getArticleList();
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
		int textResourceId = R.layout.list_item;
		hotListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = new Intent(LilyActivity.this, ArticleActivity.class);
				intent.putExtra("board", hotList.get(position - 1).getGroup());
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
		centerView.findViewById(R.id.center_blog).setOnClickListener(this);
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
			startActivity(new Intent(LilyActivity.this, MailListActivity.class));
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case R.id.center_blog:
			startActivity(new Intent(LilyActivity.this, BlogTop.class));
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case R.id.top_set:
			Intent setIntent = new Intent(LilyActivity.this, Preference.class);
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
		case R.id.main_tab_top:
			viewPager.setCurrentItem(0, true);
			break;
		case R.id.main_tab_board:
			viewPager.setCurrentItem(1, true);
			break;
		case R.id.main_tab_hot:
			viewPager.setCurrentItem(2, true);
			break;
		case R.id.main_tab_center:
			viewPager.setCurrentItem(3, true);
			break;

		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(final int index) {
		int one = offset * 2 + bmpW;
		Animation animation = new TranslateAnimation(one * currIndex, one * index, 0, 0);//显然这个比较简洁，只有一行代码。
		currIndex = index;
		animation.setFillAfter(true);// True:图片停在动画结束位置
		animation.setDuration(300);
		imageView.startAnimation(animation);

		if(index == 0) {
			boardEditButton.setVisibility(View.GONE);
			allBoardButton.setVisibility(View.GONE);
		} else if(index == 1) {
			boardEditButton.setVisibility(View.VISIBLE);
			allBoardButton.setVisibility(View.VISIBLE);
		} else if(index == 2) {
			boardEditButton.setVisibility(View.GONE);
			allBoardButton.setVisibility(View.GONE);
		} else if(index == 3) {
			boardEditButton.setVisibility(View.GONE);
			allBoardButton.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		if ((System.currentTimeMillis() - mExitTime) > 2000) {
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			mExitTime = System.currentTimeMillis();
		} else {
			ShutDown.shutDownActivity(this);
			finish();
			System.exit(0);
		}
	}
}  
