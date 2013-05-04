package com.jerry.lily;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.Mail;
import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;
import com.jerry.widget.MarqueeTextView;

public class ReadMailByAuthor extends Activity implements OnClickListener{
	private List<Mail> mailList;
	private IOSWaitingDialog waitingDialog;

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


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply_mail);
		initComponents();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	private void initComponents() {
		findViewById(R.id.mreply_send).setOnClickListener(this);
		findViewById(R.id.mreply_back).setOnClickListener(this);

		String poster = getIntent().getStringExtra("poster");
		((MarqueeTextView)findViewById(R.id.mreply_title)).setText("与" + poster + "的对话");
		LinearLayout mailBlock = (LinearLayout) findViewById(R.id.mreply_block);
		mailList = DatabaseDealer.getMailListByPoster(this, poster);
		for(Mail mail : mailList) {
			int resourceId;
			if(mail.getType() == Constants.MAIL_TYPE_SENT) {
				resourceId = R.layout.mail_reply_block;
			} else {
				resourceId = R.layout.mail_send_block;
			}
			TextView view = (TextView) LayoutInflater.from(this).inflate(resourceId, null);
			view.setText(mail.getContent());
			mailBlock.addView(view);
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mreply_send:
			if(waitingDialog == null) {
				waitingDialog = IOSWaitingDialog.createDialog(this);
			}
			waitingDialog.show();
			new Thread(new Runnable() {
				@Override
				public void run() {
					int result = 0;
					try {
						DocParser.sendMail(getIntent().getStringExtra("poster"), 
								((EditText)findViewById(R.id.mreply_input)).getText().toString(),
								ReadMailByAuthor.this);
					} catch (IOException e) {
						result = 1;
					} finally {
						mHandler.sendEmptyMessage(result);
					}

				}
			}).start();
			break;

		case R.id.mreply_back:
			onBackPressed();
			break;
		}

	}
}
