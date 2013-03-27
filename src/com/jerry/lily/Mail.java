package com.jerry.lily;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.utils.AnimCommon;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

public class Mail extends ListActivity implements IXListViewListener, OnClickListener{

	private List<com.jerry.model.Mail> mailList;
	private IOSWaitingDialog waitingDialog;
	private List<Map<String, Object>> contentList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail);
		initComponents();
		initList();
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
		Intent intent = new Intent(Mail.this, MailActivity.class);
		intent.putExtra("contentUrl", mailList.get(position - 1).getContentUrl());
		startActivity(intent);
		AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 10:
				if (waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "µ«¬º ß∞‹£¨«ÎºÏ≤ÈÕ¯¬Á", Toast.LENGTH_SHORT).show();
				break;
			case 11:
				if (waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "Õ¯¬Á“Ï≥££¨«Î…‘∫Û÷ÿ ‘", Toast.LENGTH_SHORT).show();
				break;
			case 12:
				if(getListAdapter() == null) {
					int textResourceId = DatabaseDealer.getSettings(Mail.this).isNight() ? R.layout.list_mail_night : R.layout.list_mail;
					setListAdapter(new MySimpleAdapter(Mail.this, contentList, textResourceId, new String[] {"title","author","time"}, new int[] {R.id.lm_title, R.id.lm_author, R.id.lm_time}));
				} else {
					((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				}

				if (waitingDialog != null) {
					waitingDialog.dismiss();
				}
				break;
			case 13:
				((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				onLoad();
				break;
			case 14:
				Toast.makeText(getApplicationContext(), "Õ¯¬Á“Ï≥££¨«Î…‘∫Û÷ÿ ‘", Toast.LENGTH_SHORT).show();
				onLoad();
				break;
			}
		}
	};

	@Override
	public XListView getListView() {
		return (XListView) super.getListView();
	};

	private void onLoad() {
		getListView().stopRefresh();
		getListView().setRefreshTime(DocParser.getLastUpdateTime());
	}

	private void initComponents() {
		getListView().setXListViewListener(this);
		((ImageButton)findViewById(R.id.mail_add)).setOnClickListener(this);
		((Button)findViewById(R.id.mail_quit)).setOnClickListener(this);
	}

	private void initList() {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message  msg = Message.obtain();
				try {
					mailList = DocParser.getMailList(DatabaseDealer.getBlockList(Mail.this), Mail.this);
					Collections.reverse(mailList);
					contentList = getData();
					msg.arg1 = 12;
					mHandler.sendMessage(msg);
				} catch (IOException e) {
					msg.arg1 = 11;
					mHandler.sendMessage(msg);
				}
			}
		});
		thread.start();
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i =  0; i < mailList.size(); i++) {
			com.jerry.model.Mail mail = mailList.get(i);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", mail.getTitle());
			map.put("author", "∑¢–≈»À:" + mail.getPoster());
			map.put("time", mail.getTime());
			list.add(map);
		}
		return list;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	class MySimpleAdapter extends SimpleAdapter {

		public MySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
						String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			final TextView title = (TextView)view.findViewById(R.id.lm_title);
			if(mailList.get(position).isRead()) {
				title.setTextColor(Color.RED);
			} else {
				title.setTextColor(DatabaseDealer.getSettings(Mail.this).isNight() ? Color.rgb(108, 108, 108) : Color.BLACK);
			}
			return view;
		}

	}

	@Override
	public void onRefresh() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message  msg = Message.obtain();
				try {
					mailList = DocParser.getMailList(DatabaseDealer.getBlockList(Mail.this), Mail.this);
					Collections.reverse(mailList);
					contentList = getData();
					msg.arg1 = 13;
					mHandler.sendMessage(msg);
				} catch (IOException e) {
					msg.arg1 = 14;
					mHandler.sendMessage(msg);
				}
			}
		});
		thread.start();
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mail_add:
			Intent intent = new Intent(Mail.this, NewMail.class);
			startActivity(intent);
			break;

		case R.id.mail_quit:
			onBackPressed();
			break;
		}

	}
}
