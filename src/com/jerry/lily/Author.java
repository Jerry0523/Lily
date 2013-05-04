package com.jerry.lily;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.tagdealer.TagDealer;
import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.PageBackController;
import com.jerry.widget.PageBackController.PageBackListener;

@SuppressLint("HandlerLeak")
public class Author extends Activity implements PageBackListener, OnClickListener{
	private String authorName;
	private TextView name;
	private ImageView image;

	private IOSWaitingDialog waitingDialog;

	private AlphaAnimation fadein;
	private TextView infor;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.author);
		initComponents();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	protected void onDestroy() {
		if(waitingDialog != null) {
			waitingDialog.dismiss();
		}
		super.onDestroy();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				Bundle bundle = (Bundle) msg.obj;
				boolean isMale = bundle.getBoolean("isMale");
				boolean isOnline = bundle.getBoolean("isOnline");
				String nameString = bundle.getString("name");

				String totalLogin = bundle.getString("totalLogin");
				String totalPost = bundle.getString("totalPost");
				String actValue = bundle.getString("actValue");
				String lifeValue = bundle.getString("lifeValue");
				String experienceValue = bundle.getString("experienceValue");
				String personal = bundle.getString("personal");

				name.setText(nameString);
				name.startAnimation(fadein);
				if(isMale && isOnline) {
					image.setImageResource(R.drawable.male);
				} else if(isMale && !isOnline) {
					image.setImageResource(R.drawable.male_offline);
				} else if(!isMale && isOnline) {
					image.setImageResource(R.drawable.female);
				} else {
					image.setImageResource(R.drawable.female_offline);
				}
				image.startAnimation(fadein);

				((TextView)findViewById(R.id.author_post)).setText("发文数\n"+ totalPost.substring(0, totalPost.length() - 1));
				((TextView)findViewById(R.id.author_login)).setText("上站数\n" + totalLogin.substring(0, totalLogin.length() - 1));
				((TextView)findViewById(R.id.author_experience)).setText("经验值\n" + experienceValue.substring(0, experienceValue.length() - 1));
				((TextView)findViewById(R.id.author_act)).setText("表现值\n" + actValue.substring(0, actValue.length() - 1));
				((TextView)findViewById(R.id.author_life)).setText("生命力\n" + lifeValue);

				infor.setMovementMethod(LinkMovementMethod.getInstance());
				infor.setText(Html.fromHtml(personal, null, TagDealer.getInstance(Author.this)));

				if(waitingDialog != null) {
					waitingDialog.dismiss();
				}
				break;
			case 1:
				if(waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(Author.this, "获取信息失败，请稍后重试!", Toast.LENGTH_SHORT).show();
			}
		};
	};

	private void initComponents() {
		fadein = new AlphaAnimation(0, 1);
		fadein.setDuration(1500);

		name = (TextView)findViewById(R.id.author_name);
		image = (ImageView)findViewById(R.id.author_pic);
		infor = ((TextView)findViewById(R.id.author_personal));

		PageBackController controller = (PageBackController) findViewById(R.id.page_controller);
		controller.setPageBackListener(this);
		controller.setSibling(infor);

		authorName = getIntent().getStringExtra("authorName");
		((TextView)findViewById(R.id.author_acc)).setText(authorName);

		findViewById(R.id.author_quit).setOnClickListener(this);
		findViewById(R.id.author_menu).setOnClickListener(this);

		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					Bundle bundle = DocParser.getAuthorInfo(authorName);
					msg.what = 0;
					msg.obj = bundle;

				} catch (IOException e) {
					msg.what = 1;
				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	public void onPageBack() {
		onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		startMenuActivity();
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Constants.ADD_FRIEND:
			DatabaseDealer.insertFriends(Author.this, authorName);
			Toast.makeText(Author.this, "已将" + authorName + "添加为好友!", Toast.LENGTH_SHORT).show();
			break;
		case Constants.ADD_BLOCK:
			DatabaseDealer.add2Block(Author.this, authorName);
			Toast.makeText(Author.this, "已将" + authorName + "屏蔽，请刷新页面!", Toast.LENGTH_SHORT).show();
			break;
		case Constants.SEND_MAIL:
			Intent intent = new Intent(Author.this, SendNewMail.class);
			intent.putExtra("sender", authorName);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		case Constants.GO_TO_BLOG:
			Intent blogIntent = new Intent(Author.this, Blog.class);
			blogIntent.putExtra("name", authorName);
			startActivity(blogIntent);
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;
		}
	}

	private void startMenuActivity() {
		Intent menuIntent = new Intent(this, Menu4PersonActivity.class);
		startActivityForResult(menuIntent, 0);
		overridePendingTransition(R.anim.in_from_up,R.anim.keep_origin);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.author_quit:
			onBackPressed();
			break;
		case R.id.author_menu:
			startMenuActivity();
			break;
		}

	}
}
