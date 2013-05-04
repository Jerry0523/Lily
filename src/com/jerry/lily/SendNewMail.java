package com.jerry.lily;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;

@SuppressLint("HandlerLeak")
public class SendNewMail extends Activity implements OnClickListener{

	private EditText title;
	private EditText content;

	private IOSWaitingDialog waitingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply);
		initComponents();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(waitingDialog != null) {
				waitingDialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				Toast.makeText(getApplicationContext(), "信件发送成功!",Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case 1:
				Toast.makeText(getApplicationContext(), "信件发送失败!",Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private void initComponents() {
		String sender = getIntent().getStringExtra("sender");

		content = (EditText)findViewById(R.id.reply_edit);
		title = (EditText) findViewById(R.id.reply_title);

		findViewById(R.id.reply_photo).setVisibility(View.GONE);
		findViewById(R.id.reply_pic).setVisibility(View.GONE);

		title.setHint("请输入收件人");
		if(sender != null && sender.length() > 0) {
			title.setText(sender);
		}

		findViewById(R.id.reply_submit).setOnClickListener(this);
		findViewById(R.id.reply_quit).setOnClickListener(this);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reply_submit:
			if(title.getText() == null || title.getText().length() == 0 || content.getText() == null || content.getText().length() == 0) {
				Toast.makeText(getApplicationContext(), "收件人和信件内容不能为空!",Toast.LENGTH_SHORT).show();
				return;
			}
			if(waitingDialog == null) {
				waitingDialog = IOSWaitingDialog.createDialog(this);
			}
			waitingDialog.show();
			new Thread(new Runnable() {
				@Override
				public void run() {
					int result = 0;
					try {
						DocParser.sendMail(title.getText().toString(), content.getText().toString(), SendNewMail.this);
					} catch (IOException e) {
						result = 1;
					} finally {
						mHandler.sendEmptyMessage(result);
					}

				}
			}).start();
			break;
		case R.id.reply_quit:
			onBackPressed();
			break;
		default:
			break;
		}

	}
}
