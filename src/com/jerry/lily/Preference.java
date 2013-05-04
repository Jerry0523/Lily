package com.jerry.lily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.jerry.model.Settings;
import com.jerry.utils.DatabaseDealer;
import com.jerry.widget.Switcher;

public class Preference extends Activity{
	private Settings settings;
	private Switcher isLogin;
	private Switcher isShowPic;
	private Switcher isReplyMail;
	private Switcher isNight;

	private RelativeLayout acc;
	private RelativeLayout sign;
	private RelativeLayout block;
	private RelativeLayout about;
	private RelativeLayout send2Me;

	private RadioButton small;
	private RadioButton middle;
	private RadioButton big;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set);
		initComponents();
		updateComponents();
	}

	@Override
	protected void onResume() {
		updateComponents();
		super.onResume();
	}

	private void initComponents() {
		acc = (RelativeLayout)findViewById(R.id.set_acc);
		sign = (RelativeLayout)findViewById(R.id.set_sign);
		block = (RelativeLayout)findViewById(R.id.set_block);
		about = (RelativeLayout)findViewById(R.id.set_about);
		send2Me = (RelativeLayout)findViewById(R.id.set_send2me);

		isLogin = (Switcher)findViewById(R.id.set_autoLogin);
		isShowPic = (Switcher)findViewById(R.id.set_showPic);
		isReplyMail = (Switcher)findViewById(R.id.set_reply_mail);
		isNight = (Switcher)findViewById(R.id.set_night);

		small = (RadioButton)findViewById(R.id.set_font_small);
		middle = (RadioButton)findViewById(R.id.set_font_middle);
		big = (RadioButton)findViewById(R.id.set_font_big);

		send2Me.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Preference.this, Send2Me.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});

		findViewById(R.id.set_backButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		about.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Preference.this, About.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});

		isLogin.addChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DatabaseDealer.updateSettings(Preference.this, "isLogin", isChecked ? "1" : "0");
			}
		});

		isNight.addChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DatabaseDealer.updateSettings(Preference.this, "isNight", isChecked ? "1" : "0");
//				Intent intent = new Intent(Preference.this, Welcome.class);
//				startActivity(intent);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});
		
		isReplyMail.addChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DatabaseDealer.updateSettings(Preference.this, "isSendMail", isChecked ? "1" : "0");
			}
		});
		
		isShowPic.addChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DatabaseDealer.updateSettings(Preference.this, "isShowPic", isChecked ? "1" : "0");
			}
		});

		acc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Preference.this, Login.class);
				intent.putExtra("username", DatabaseDealer.query(Preference.this).getString("username"));
				intent.putExtra("pass", DatabaseDealer.query(Preference.this).getString("password"));
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});

		sign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Preference.this, Sign.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});

		block.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Preference.this, Block.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});

		small.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DatabaseDealer.updateSettings(Preference.this, "fontSize", "15");
			}
		});

		middle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DatabaseDealer.updateSettings(Preference.this, "fontSize", "19");
			}
		});

		big.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DatabaseDealer.updateSettings(Preference.this, "fontSize", "22");
			}
		});
	}

	private void updateComponents() {
		settings = DatabaseDealer.getSettings(Preference.this);
		isLogin.setValue(settings.isLogin());
		isShowPic.setValue(settings.isShowPic());
		isReplyMail.setValue(settings.isSendMail());
		isNight.setValue(settings.isNight());
		if(settings.getFontSize() == 19) {
			middle.setChecked(true);
		} else if(settings.getFontSize() > 19) {
			big.setChecked(true);
		} else {
			small.setChecked(true);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.keep_origin,R.anim.out_to_down); 
	}
}
