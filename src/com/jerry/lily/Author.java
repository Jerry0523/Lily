package com.jerry.lily;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.tagdealer.TagDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.PageBackController;
import com.jerry.widget.PageBackController.PageBackListener;

public class Author extends Activity implements PageBackListener{
	private Button quit;
//	private Button submit;

	private TextView acc;
	private TextView name;
	private ImageView image;
	private IOSWaitingDialog waitingDialog;

//	private Button block;
//	private Button add;
//	private Button info;
//	private Button send;
//	private EditText content;

	private boolean isMale;
	private boolean isOnline;
	private String nameString;
	
	private AlphaAnimation fadein;
	
	private String totalLogin;
	private String totalPost;
	private String actValue;
	private String lifeValue;
	private String experienceValue;
	private String personal;
	
	private PageBackController controller;
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
			switch(msg.arg1) {
			case 10:
				if(waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "获取信息失败，请稍后重试!", Toast.LENGTH_SHORT).show();
				finish();
			case 11:
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
//			case 12:
//				Toast.makeText(getApplicationContext(), "信息发送成功", Toast.LENGTH_SHORT).show();
//				acc.setVisibility(View.VISIBLE);
//				name.setVisibility(View.VISIBLE);
//				image.setVisibility(View.VISIBLE);
//				send.setVisibility(View.VISIBLE);
//
//				info.setVisibility(View.INVISIBLE);
//				submit.setVisibility(View.INVISIBLE);
//				content.setVisibility(View.INVISIBLE);
//				break;
			case 13:
				Toast.makeText(getApplicationContext(), "信息发送失败，请重试！", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};

	private void initComponents() {
		fadein = new AlphaAnimation(0, 1);
		fadein.setDuration(1500);
		
		quit = (Button)findViewById(R.id.author_quit);
		acc = (TextView)findViewById(R.id.author_acc);
		name = (TextView)findViewById(R.id.author_name);
		image = (ImageView)findViewById(R.id.author_pic);
		controller = (PageBackController) findViewById(R.id.page_controller);
		infor = ((TextView)findViewById(R.id.author_personal));
		
		controller.setPageBackListener(this);
		controller.setSibling(infor);
		
//		block = (Button)findViewById(R.id.author_block);
//		submit = (Button)findViewById(R.id.author_submit);
//		add = (Button)findViewById(R.id.author_friend);
//		info = (Button)findViewById(R.id.author_info);
//		send = (Button)findViewById(R.id.author_send_mail);
//		content = (EditText)findViewById(R.id.author_content);

		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		final String authorName = getIntent().getStringExtra("authorName");
		acc.setText(authorName);

//		submit.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Thread thread = new Thread(new Runnable() {
//					@Override
//					public void run() {
//						Message msg = Message.obtain();
//						try {
//							DocParser.sendMail(authorName, content.getText().toString(), getApplicationContext());
//							msg.arg1 = 12;
//							mHandler.sendMessage(msg);
//						} catch (IOException e) {
//							msg.arg1 = 13;
//							mHandler.sendMessage(msg);
//						}
//						msg.arg1 = 12;
//						mHandler.sendMessage(msg);
//					}
//				});
//				thread.start();
//			}
//		});
//
//		block.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				DatabaseDealer.add2Block(Author.this, authorName);
//				Toast.makeText(getApplicationContext(), "已将" + authorName + "屏蔽，请刷新页面!", Toast.LENGTH_SHORT).show();
//				finish();
//			}
//		});
//
//		add.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				DatabaseDealer.insertFriends(Author.this, authorName);
//				Toast.makeText(getApplicationContext(), "已将" + authorName + "添加为好友!", Toast.LENGTH_SHORT).show();
//			}
//		});
//
//		send.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				acc.setVisibility(View.INVISIBLE);
//				name.setVisibility(View.INVISIBLE);
//				image.setVisibility(View.INVISIBLE);
//				send.setVisibility(View.INVISIBLE);
//
//				info.setVisibility(View.VISIBLE);
//				submit.setVisibility(View.VISIBLE);
//				content.setVisibility(View.VISIBLE);
//			}
//		});
//
//		info.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				acc.setVisibility(View.VISIBLE);
//				name.setVisibility(View.VISIBLE);
//				image.setVisibility(View.VISIBLE);
//				send.setVisibility(View.VISIBLE);
//
//				info.setVisibility(View.INVISIBLE);
//				submit.setVisibility(View.INVISIBLE);
//				content.setVisibility(View.INVISIBLE);
//
//			}
//		});

		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				Bundle bundle;
				try {
					bundle = DocParser.getAuthorInfo(authorName);
				} catch (IOException e) {
					msg.arg1 = 10;
					mHandler.sendMessage(msg);
					return;
				}
				isMale = bundle.getBoolean("isMale");
				isOnline = bundle.getBoolean("isOnline");
				nameString = bundle.getString("name");
				
				totalLogin = bundle.getString("totalLogin");
				totalPost = bundle.getString("totalPost");
				actValue = bundle.getString("actValue");
				lifeValue = bundle.getString("lifeValue");
				experienceValue = bundle.getString("experienceValue");
				personal = bundle.getString("personal");
				
				msg.arg1 = 11;
				mHandler.sendMessage(msg);
			}
		});
		thread.start();
	}

	@Override
	public void onPageBack() {
		onBackPressed();
	}
}
