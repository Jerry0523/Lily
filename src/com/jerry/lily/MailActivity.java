package com.jerry.lily;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;

public class MailActivity extends Activity{
	private Button send;
	private Button quit;
	private TextView replyContent;
	private EditText postContent;

	private String contentUrl;
	private String content;
	private String postUrl;

	private IOSWaitingDialog waitingDialog;

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 11:
				replyContent.setText(content);
				if (waitingDialog != null) {
					waitingDialog.cancel();
				}
				break;
			case 12:
				if (waitingDialog != null) {
					waitingDialog.cancel();
				}
				Toast.makeText(getApplicationContext(), "·¢ËÍ³É¹¦!", Toast.LENGTH_SHORT).show();
				onBackPressed(); 
				break;
			case 13:
				if (waitingDialog != null) {
					waitingDialog.cancel();
				}
				Toast.makeText(getApplicationContext(), "·¢ËÍÊ§°Ü,Çë¼ì²éÍøÂç!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply_mail);
		initComponents();
		initContent();
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

	private void initComponents() {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();
		contentUrl = getIntent().getStringExtra("contentUrl");
		send = (Button)findViewById(R.id.mreply_send);
		quit = (Button)findViewById(R.id.mreply_back);
		replyContent = (TextView)findViewById(R.id.mreply_content);
		postContent = (EditText)findViewById(R.id.mreply_reply);

		replyContent.setMovementMethod(ScrollingMovementMethod.getInstance());  
		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String sendContent = postContent.getText().toString();
				if(waitingDialog == null) {
					waitingDialog = IOSWaitingDialog.createDialog(MailActivity.this);
				}
				waitingDialog.show();
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						Message msg = Message.obtain();
						try {
							if(DocParser.replyMail(postUrl, sendContent, MailActivity.this)) {
								msg.arg1 = 12;
							} else {
								msg.arg1 = 13;
							}
						} catch (IOException e) {
							msg.arg1 = 13;
							mHandler.handleMessage(msg);
						}
						mHandler.handleMessage(msg);
					}
				});
				thread.start();

			}
		});
	}

	private void initContent() {
		Thread thread = new Thread(
				new Runnable() {
					@Override
					public void run() {
						Message  msg = Message.obtain();
						try {
							Bundle bundle = DocParser.getMailContent(contentUrl,MailActivity.this);
							content = bundle.getString("content");
							postUrl = bundle.getString("postUrl");
							msg.arg1 = 11;
							mHandler.sendMessage(msg);
						} catch (IOException e) {
							msg.arg1 = 13;
							mHandler.sendMessage(msg);
						}
					}
				}
				);
		thread.start();

	}
}
