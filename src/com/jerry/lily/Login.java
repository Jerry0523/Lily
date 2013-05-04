package com.jerry.lily;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.jerry.model.LoginInfo;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.widget.IOSAlertDialog;
import com.jerry.widget.IOSWaitingDialog;

@SuppressLint("HandlerLeak")
public class Login extends Activity implements OnClickListener{
	private EditText editUsername;
	private EditText editPassword;

	private IOSWaitingDialog waitingDialog;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(waitingDialog != null) {
				waitingDialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				new IOSAlertDialog.Builder(Login.this).setTitle("提示").setMessage("网络连接失败，请稍后尝试!").setNegativeButtonText("好").create().show();
				break;
			case 1:
				new IOSAlertDialog.Builder(Login.this).setTitle("提示").setMessage("用户名密码错误,\n请检查输入!").setNegativeButtonText("好").create().show();
				break;
			case 2:
				new IOSAlertDialog.Builder(Login.this).setTitle("注意").setMessage("用户名密码不能为空!").setNegativeButtonText("好").create().show();
				break;
			case 3:
				android.content.DialogInterface.OnClickListener removeListener = new android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Uri packageURI = Uri.parse("package:com.jerry.lily");     
						Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);     
						startActivity(uninstallIntent);  
					}
					
				};
				new IOSAlertDialog.Builder(Login.this).setTitle("抱歉").setMessage("您的id被作者屏蔽，请卸载本软体!").setNegativeButton("好", removeListener).create().show();
				break;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initComponents();
	}
	
	@Override
	protected void onDestroy() {
		waitingDialog.dismiss();
		super.onDestroy();
	}

	private void login() throws IOException, AccountsException {
		String username = editUsername.getText().toString().trim();
		String password = editPassword.getText().toString().trim();
		LoginInfo loginInfo = LoginInfo.getInstance(username, password);
		DatabaseDealer.insert(Login.this, username, password);
		List<ContentValues> favList = DocParser.synchronousFav(loginInfo);
		DatabaseDealer.addFav(Login.this, favList);
		
		Intent intent = new Intent(Login.this, LilyActivity.class);
		intent.putParcelableArrayListExtra("topList", getIntent().getParcelableArrayListExtra("topList"));
		intent.putParcelableArrayListExtra("hotList",(ArrayList<? extends Parcelable>) getIntent().getParcelableArrayListExtra("hotList"));
		intent.putStringArrayListExtra("favList", (ArrayList<String>) DatabaseDealer.getFavList(Login.this));
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
		finish();
	}

	private void initComponents() {
		editUsername = (EditText) findViewById(R.id.login_username);
		editPassword = (EditText) findViewById(R.id.login_password);

		editUsername.setText(getIntent().getStringExtra("username"));
		editPassword.setText(getIntent().getStringExtra("password"));

		findViewById(R.id.login_exit).setOnClickListener(this);
		findViewById(R.id.login_submit).setOnClickListener(this);
	}
	
	private List<String> getJerryHateAccount() {
		List<String> banned = new ArrayList<String>();
		banned.add("Zeratul");
		banned.add("superjyq");
		banned.add("nomorestars");
		banned.add("freeme");
		banned.add("nuanbing");
		return banned;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_exit:
			finish();
			overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			break;

		case R.id.login_submit:
			if (editUsername.getText().length() == 0 || editPassword.getText().length() == 0) {
				mHandler.sendEmptyMessage(2);
				return;
			}
			
			if (getJerryHateAccount().contains(editUsername.getText().toString())) {
				mHandler.sendEmptyMessage(3);
				return;
			}
			
			if(waitingDialog == null) {
				waitingDialog = IOSWaitingDialog.createDialog(Login.this);
			}
			waitingDialog.show();
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						login();
					} catch (IOException e) {
						mHandler.sendEmptyMessage(0);
					} catch (AccountsException e) {
						mHandler.sendEmptyMessage(1);
					}

				}
			}).start();
			break;
		}
		
	}
}
