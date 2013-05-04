package com.jerry.lily;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jerry.model.MailGroup;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.MailGroupAdapter;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

public class MailListActivity extends ListActivity implements IXListViewListener, OnClickListener{

	private List<MailGroup> groupList;
	private IOSWaitingDialog waitingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail);
		initComponents();
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		groupList = DatabaseDealer.getMailByGroup(MailListActivity.this);
		setListAdapter(new MailGroupAdapter(R.layout.list_item, MailListActivity.this, groupList));
		onRefresh();
	}

	@Override
	protected void onDestroy() {
		if(waitingDialog != null) {
			waitingDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		position--;
		Intent intent = new Intent(MailListActivity.this, ReadMailByAuthor.class);
		intent.putExtra("poster", groupList.get(position).getPoster());
		startActivity(intent);
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			getListView().stopRefresh();
			switch (msg.what) {
			case 0:
				getListView().setRefreshTime(DocParser.getLastUpdateTime());
				getListAdapter().notifyDataSetChanged();
				Toast.makeText(MailListActivity.this, "站内信刷新成功!", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(MailListActivity.this, "网络异常，请稍后再试!", Toast.LENGTH_SHORT).show();
				break;
			}
			if (waitingDialog != null) {
				waitingDialog.dismiss();
			}
		}
	};

	@Override
	public XListView getListView() {
		return (XListView) super.getListView();
	};

	@Override
	public BaseAdapter getListAdapter() {
		return (BaseAdapter) super.getListAdapter();
	}

	private void initComponents() {
		getListView().setXListViewListener(this);
		getListView().setPullLoadEnable(false);
		getListView().setFooterViewForbidden(true);

		findViewById(R.id.mail_add).setOnClickListener(this);
		findViewById(R.id.mail_quit).setOnClickListener(this);
		findViewById(R.id.mail_load_more).setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	public void onRefresh() {
		if(groupList.size() == 0) {
			waitingDialog.show();
		}
		new Thread(new Runnable() {
			int result = 0;
			@Override
			public void run() {
				try {
					MailGroup.getMailList(MailListActivity.this, false);
					groupList.clear();
					List<MailGroup> group = DatabaseDealer.getMailByGroup(MailListActivity.this);
					groupList.addAll(group);
				} catch (Exception e) {
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		groupList.clear();
		List<MailGroup> group = DatabaseDealer.getMailByGroup(MailListActivity.this);
		groupList.addAll(group);
		getListAdapter().notifyDataSetChanged();
	}

	@Override
	public void onLoadMore() {
		new Thread(new Runnable() {
			int result = 0;
			@Override
			public void run() {
				try {
					MailGroup.getMailList(MailListActivity.this, true);
					groupList.clear();
					List<MailGroup> group = DatabaseDealer.getMailByGroup(MailListActivity.this);
					groupList.addAll(group);
				} catch (Exception e) {
					result = 1;
				} finally {
					mHandler.sendEmptyMessage(result);
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mail_add:
			Intent intent = new Intent(MailListActivity.this, SendNewMail.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;

		case R.id.mail_quit:
			onBackPressed();
			break;
			
		case R.id.mail_load_more:
			onLoadMore();
			break;
		}

	}
}
