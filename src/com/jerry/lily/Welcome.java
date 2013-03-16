package com.jerry.lily;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ImageView;

import com.jerry.model.Article;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.FileDealer;
import com.jerry.widget.IOSAlertDialog;

public class Welcome extends Activity{
	private AsyncTask<Object, Object, Object[]> thread;
	private OnClickListener positiveLis;
	private OnClickListener negativeLis;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		ImageView imageView = (ImageView) findViewById(R.id.welcome_load);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
		animationDrawable.start();
		FileDealer.createDir();
		initThread();
	}

	@Override
	public void onBackPressed() {
		thread.cancel(true);
		super.onBackPressed();
		System.exit(1);
	}

	private void initThread() {
		thread = new AsyncTask<Object, Object, Object[]>() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void onPostExecute(Object[] result) {
				if(result == null) {
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
					return;
				}
				if (((Bundle) result[0]).size() == 0) {
					Intent intent = new Intent(Welcome.this, Login.class);
					intent.putParcelableArrayListExtra("topList", (ArrayList<? extends Parcelable>) result[1]);
					intent.putParcelableArrayListExtra("hotList", (ArrayList<? extends Parcelable>) result[2]);
					startActivity(intent);
				} else {
					Intent intent = new Intent(Welcome.this, LilyActivity.class);
					intent.putParcelableArrayListExtra("topList", (ArrayList<? extends Parcelable>) result[1]);
					intent.putParcelableArrayListExtra("hotList", (ArrayList<? extends Parcelable>) result[2]);
					intent.putStringArrayListExtra("favList", (ArrayList<String>) DatabaseDealer.getFavList(Welcome.this));
					intent.putExtra("newMail", (Boolean) result[3]);
					startActivity(intent);
				}
				finish();
			}

			@Override
			protected Object[] doInBackground(Object... params) {
				try {
					Bundle userInfo = DatabaseDealer.query(Welcome.this);
					List<Article> topList = DocParser.getArticleTitleList("http://bbs.nju.edu.cn/bbstop10", DatabaseDealer.getBlockList(Welcome.this));
					List<Article> hotList = DocParser.getHotArticleTitleList("http://bbs.nju.edu.cn/bbstopall", null);
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
					
					return new Object[]{userInfo, topList, hotList, newMail};
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		thread.execute("");
	}
}
