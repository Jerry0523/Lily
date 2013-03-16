package com.jerry.lily;


import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.jerry.utils.AnimCommon;
import com.jerry.utils.DatabaseDealer;

public class LilyActivity extends TabActivity implements OnCheckedChangeListener{
	private TabHost tabHost;
	private RadioGroup radioGroup;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainui);
		initComponents();
	}

	@Override
	protected void onPause() {
		if(AnimCommon.in!=0 && AnimCommon.out!=0){
			super.overridePendingTransition(AnimCommon.in, AnimCommon.out);
			AnimCommon.clear();
		}
		super.onPause();
	}

	private void initComponents() {
		boolean newMail = getIntent().getBooleanExtra("newMail", false);
		tabHost = getTabHost();
		if(DatabaseDealer.getSettings(LilyActivity.this).isNight()) {
			tabHost.getTabContentView().setBackgroundDrawable(null);
		}
		radioGroup = (RadioGroup)findViewById(R.id.radio);
		radioGroup.setOnCheckedChangeListener(this);
		Intent intent1 = new Intent(this, Top.class);
		intent1.putParcelableArrayListExtra("topList",(ArrayList<? extends Parcelable>) getIntent().getParcelableArrayListExtra("topList"));
		Intent intent2 = new Intent(this, Board.class);
		intent2.putStringArrayListExtra("favList", getIntent().getStringArrayListExtra("favList"));
		Intent intent3 = new Intent(this, Hot.class);
		intent3.putParcelableArrayListExtra("hotList",(ArrayList<? extends Parcelable>) getIntent().getParcelableArrayListExtra("hotList"));
		Intent intent4 = new Intent(this, Mail.class);
		Intent intent5 = new Intent(this, Center.class);

		tabHost.addTab(tabHost.newTabSpec("top")
				.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_call))
				.setContent(intent1));
		tabHost.addTab(tabHost.newTabSpec("board")
				.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_add))
				.setContent(intent2));
		tabHost.addTab(tabHost.newTabSpec("center")
				.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_add))
				.setContent(intent5));
		tabHost.addTab(tabHost.newTabSpec("hot")
				.setIndicator("",getResources().getDrawable(android.R.drawable.ic_media_play))
				.setContent(intent3));
		tabHost.addTab(tabHost.newTabSpec("search")
				.setIndicator("",getResources().getDrawable(android.R.drawable.ic_menu_camera))
				.setContent(intent4));

//		if(newMail) {
//			((RadioButton)findViewById(R.id.search)).setChecked(true);
//		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.top:
			this.tabHost.setCurrentTabByTag("top");
			break;
		case R.id.board:
			this.tabHost.setCurrentTabByTag("board");
			break;
		case R.id.center:
			this.tabHost.setCurrentTabByTag("center");
			break;
		case R.id.hot:
			this.tabHost.setCurrentTabByTag("hot");
			break;
//		case R.id.search:
//			this.tabHost.setCurrentTabByTag("search");
//			break;
		default:
			this.tabHost.setCurrentTabByTag("top");
			break;
		}
	}
}  
