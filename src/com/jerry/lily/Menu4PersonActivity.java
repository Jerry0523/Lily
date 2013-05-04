package com.jerry.lily;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;

import com.jerry.utils.Constants;

public class Menu4PersonActivity extends Activity implements OnClickListener{
	private View top;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_person);
		initComponents();
	}

	@Override
	public void onBackPressed() {
		top.setBackgroundResource(R.drawable.transparent);
		super.onBackPressed();
		overridePendingTransition(R.anim.keep_origin,R.anim.out_to_down);
	}

	private void initComponents() {
		top = findViewById(R.id.menutop);

		findViewById(R.id.person_add_friend).setOnClickListener(this);
		findViewById(R.id.person_add_block).setOnClickListener(this);
		findViewById(R.id.person_blog).setOnClickListener(this);
		findViewById(R.id.person_send_mail).setOnClickListener(this);
		findViewById(R.id.mm_back).setOnClickListener(this);
		top.setOnClickListener(this);
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				top.setBackgroundResource(R.drawable.transparent_grey);
				top.startAnimation(AnimationUtils.loadAnimation(Menu4PersonActivity.this, R.anim.fade_in));
			}
		}, 300);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		onBackPressed();
		return false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.person_add_block:
			setResult(Constants.ADD_BLOCK);
			onBackPressed();
			break;
		case R.id.person_add_friend:
			setResult(Constants.ADD_FRIEND);
			onBackPressed();
			break;
		case R.id.person_blog:
			setResult(Constants.GO_TO_BLOG);
			onBackPressed();
			break;
		case R.id.person_send_mail :
			setResult(Constants.SEND_MAIL);
			onBackPressed();
			break;
		case R.id.mm_back:
		default:
			onBackPressed();
			break;
		}

	}

}
