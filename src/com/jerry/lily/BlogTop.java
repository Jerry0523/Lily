package com.jerry.lily;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jerry.model.Article;
import com.jerry.model.ArticleGroup;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.PageBackController;
import com.jerry.widget.PageBackController.PageBackListener;
import com.jerry.widget.TopListAdapter;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

public class BlogTop extends ListActivity implements OnClickListener, IXListViewListener, PageBackListener{
	private List<Article> blogList;
	private IOSWaitingDialog waitingDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_top);

		findViewById(R.id.blog_back).setOnClickListener(this);
		findViewById(R.id.blog_search).setOnClickListener(this);
		findViewById(R.id.blog_title).setOnClickListener(this);

		PageBackController controller = (PageBackController) findViewById(R.id.page_controller);
		controller.setPageBackListener(this);
		controller.setSibling(getListView());

		waitingDialog = IOSWaitingDialog.createDialog(this);
		getListView().setPullLoadEnable(false);
		getListView().setXListViewListener(this);
		initList();
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(getListAdapter() == null) {
					setListAdapter(new TopListAdapter(R.layout.list_item, BlogTop.this, blogList));
				} else {
					getListAdapter().notifyDataSetChanged();
				}
				break;
			case 1:
				Toast.makeText(BlogTop.this, "Õ¯¬Á¡¨Ω” ß∞‹£°", Toast.LENGTH_SHORT).show();
				break;
			}
			afterLoad();
			waitingDialog.dismiss();
		}
	};

	private void initList() {
		if(blogList == null) {
			waitingDialog.show();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				int result = 0;
				try {
					List<Article> tmp = ArticleGroup.getTopBlogArticleList(BlogTop.this).getArticleList();
					if(blogList == null) {
						blogList = tmp;
					} else {
						blogList.clear();
						blogList.addAll(tmp);
					}

				} catch (Exception e) {
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}

			}
		}).start();
	}

	@Override
	public XListView getListView() {
		return (XListView) super.getListView();
	}

	@Override
	public BaseAdapter getListAdapter() {
		return (BaseAdapter) super.getListAdapter();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	@Override
	public void onRefresh() {
		initList();
	}

	private void afterLoad() {
		getListView().stopRefresh();
		getListView().stopLoadMore();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}

	@Override
	public void onLoadMore() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.blog_back:
			onBackPressed();
			break;

		case R.id.blog_search:
			break;
		case R.id.blog_title:
			getListView().setSelectionAfterHeaderView();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	public void onPageBack() {
		onBackPressed();
	}


}
