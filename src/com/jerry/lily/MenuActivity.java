package com.jerry.lily;


import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.jerry.utils.Constants;

public class MenuActivity extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		initComponents();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.keep_origin,R.anim.out_to_down);
	}

	private void initComponents() {
		List<Integer> remove = getIntent().getIntegerArrayListExtra("remove");

		Button add2Fav = (Button) findViewById(R.id.add2_fav);
		Button postNewArticle = (Button) findViewById(R.id.post_article);
		Button share = (Button) findViewById(R.id.share);
		Button browser = (Button) findViewById(R.id.browser);
		Button back = (Button) findViewById(R.id.mm_back);

		((RelativeLayout)findViewById(R.id.menu_body)).setOnClickListener(this);
		add2Fav.setOnClickListener(this);
		postNewArticle.setOnClickListener(this);
		share.setOnClickListener(this);
		browser.setOnClickListener(this);
		back.setOnClickListener(this);

		if(remove.contains(Constants.ADD_2_FAV)) {
			add2Fav.setVisibility(View.GONE);
		}
		if(remove.contains(Constants.POST_ARTICLE)) {
			postNewArticle.setVisibility(View.GONE);
		}
		if(remove.contains(Constants.SHARE)) {
			share.setVisibility(View.GONE);
		}
		if(remove.contains(Constants.BROWSER)) {
			browser.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		onBackPressed();
		return false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.add2_fav:
			setResult(Constants.ADD_2_FAV);
			onBackPressed();
			break;
		case R.id.post_article:
			setResult(Constants.POST_ARTICLE);
			onBackPressed();
			break;
		case R.id.share:
			setResult(Constants.SHARE);
			onBackPressed();
			break;
		case R.id.browser :
			setResult(Constants.BROWSER);
			onBackPressed();
			break;
		case R.id.mm_back:
			onBackPressed();
			break;
		default:
			onBackPressed();
			break;
		}

	}

}
