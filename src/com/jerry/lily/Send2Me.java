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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.utils.DocParser;
import com.jerry.widget.IOSWaitingDialog;

public class Send2Me extends Activity implements OnClickListener{
	private EditText edit;
	private IOSWaitingDialog waitingDialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign);
		initComponents();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 10:
				if(waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "发送失败，请检查网络!", Toast.LENGTH_SHORT).show();
				break;

			case 11:
				if(waitingDialog != null) {
					waitingDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "发送成功!", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			}
		};
	};

	private void initComponents() {
		((RelativeLayout)findViewById(R.id.sign_ra1)).setVisibility(View.GONE);
		((RelativeLayout)findViewById(R.id.sign_ra2)).setVisibility(View.GONE);
		((Button)findViewById(R.id.sign_exit)).setOnClickListener(this);
		((TextView)findViewById(R.id.sign_title)).setText("发送反馈");
		
		Button submit = (Button)findViewById(R.id.sign_submit);
		submit.setText("提交");
		submit.setOnClickListener(this);
		
		edit = (EditText)findViewById(R.id.sign_sign);
		edit.setHint("输入反馈内容");

	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.sign_submit:
			if(waitingDialog == null) {
				waitingDialog = IOSWaitingDialog.createDialog(Send2Me.this);
			}
			waitingDialog.show();
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Message msg = Message.obtain();
					try {
						DocParser.sendMail("mysterious", edit.getText().toString(), getApplicationContext());
						msg.arg1 = 11;
						mHandler.sendMessage(msg);
					} catch (IOException e) {
						msg.arg1 = 10;
						mHandler.sendMessage(msg);
					}

				}
			});
			thread.start();
			break;
		case R.id.sign_exit:
			onBackPressed();
			break;
		}

	}
}
