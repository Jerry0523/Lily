package com.jerry.lily;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.widget.ImageView;

import com.jerry.model.Article;
import com.jerry.model.ArticleGroup;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.FileDealer;
import com.jerry.widget.IOSAlertDialog;

@SuppressLint("HandlerLeak")
public class Welcome extends Activity{
	private Thread thread;
	private OnClickListener positiveLis;
	private OnClickListener negativeLis;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		ImageView imageView = (ImageView) findViewById(R.id.welcome_load);
		((AnimationDrawable) imageView.getDrawable()).start();
		FileDealer.createDir();
		initThread();
	}

	@Override
	public void onBackPressed() {
		thread.interrupt();
		super.onBackPressed();
		System.exit(1);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 0) {
				startActivity((Intent) msg.obj);
				finish();
				overridePendingTransition(R.anim.fade_in, R.anim.keep_origin);
			} else if (msg.what == 1) {
				if(positiveLis == null) {
					positiveLis = new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							initThread();
							dialog.dismiss();
						}
					};
				}

				if(negativeLis == null) {
					negativeLis = new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							onBackPressed();
						}
					};
				}
				new IOSAlertDialog.Builder(Welcome.this).setTitle("注意").setMessage("网络连接失败，是否重试?").setPositiveButton("好", positiveLis).setNegativeButton("退出", negativeLis).create().show();
			}
		};
	};

	private void initThread() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					Bundle userInfo = DatabaseDealer.query(Welcome.this);
					List<Article> topList = ArticleGroup.getTopArticleTitleList(Welcome.this).getArticleList();
					List<Article> hotList = ArticleGroup.getHotArticleTitleList().getArticleList();
					boolean newMail = false;
					if(DatabaseDealer.getSettings(Welcome.this).isLogin()) {
						Intent intent = new Intent(getApplicationContext(), RefreshService.class);
						startService(intent);
						List<com.jerry.model.Mail> mailList;

						mailList = DocParser.getMailList(DatabaseDealer.getBlockList(Welcome.this), Welcome.this);

						if(mailList != null) {
							for(com.jerry.model.Mail mail : mailList) {
								if(mail.isRead()) {
									newMail = true;
									break;
								}
							}
						}
					}

					if(userInfo == null || topList == null || hotList == null) {
						throw new IOException();
					}
					Intent intent;
					if (userInfo.size() == 0) {
						intent = new Intent(Welcome.this, Login.class);
						intent.putParcelableArrayListExtra("topList", (ArrayList<? extends Parcelable>) topList);
						intent.putParcelableArrayListExtra("hotList", (ArrayList<? extends Parcelable>) hotList);
					} else {
						intent = new Intent(Welcome.this, LilyActivity.class);
						intent.putParcelableArrayListExtra("topList", (ArrayList<? extends Parcelable>) topList);
						intent.putParcelableArrayListExtra("hotList", (ArrayList<? extends Parcelable>) hotList);
						intent.putStringArrayListExtra("favList", (ArrayList<String>) DatabaseDealer.getFavList(Welcome.this));
						intent.putExtra("newMail", newMail);
					}
					
					msg.what = 0;
					msg.obj = intent;
				} catch (IOException e) {
					msg.what = 1;
				} finally {
					mHandler.sendMessage(msg);
				}

			}
		});
		thread.start();
	}
}
