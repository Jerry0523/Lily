package com.jerry.lily;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jerry.utils.DocParser;

public class NewMail extends Activity{

	private Button reply;
	private Button quit;
	private ImageButton photo;
	private ImageButton pic;
	private EditText title;
	private EditText content;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply);
		initComponents();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 10:
				Toast.makeText(getApplicationContext(), "信件发送成功!",Toast.LENGTH_SHORT).show();
				finish();
				break;
			case 11:
				Toast.makeText(getApplicationContext(), "信件发送失败!",Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private void initComponents() {
		reply = (Button)findViewById(R.id.reply_submit);
		quit = (Button)findViewById(R.id.reply_quit);
		content = (EditText)findViewById(R.id.reply_edit);
		title = (EditText) findViewById(R.id.reply_title);

		photo = (ImageButton)findViewById(R.id.reply_photo);
		pic = (ImageButton)findViewById(R.id.reply_pic);
		photo.setVisibility(View.GONE);
		pic.setVisibility(View.GONE);

		title.setHint("请输入收件人");

		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		reply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(title.getText() == null || title.getText().length() == 0 || content.getText() == null || content.getText().length() == 0) {
					Toast.makeText(getApplicationContext(), "收件人和信件内容不能为空!",Toast.LENGTH_SHORT).show();
					return;
				}

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = Message.obtain();

						try {
							DocParser.sendMail(title.getText().toString(), content.getText().toString(), NewMail.this);
							msg.arg1 = 10;
							mHandler.sendMessage(msg);
						} catch (IOException e) {
							msg.arg1 = 11;
							mHandler.sendMessage(msg);
						}

					}
				});
				thread.start();

			}
		});
	}
}
