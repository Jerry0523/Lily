package com.jerry.lily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.jerry.utils.AnimCommon;
import com.jerry.utils.ShutDown;

public class Center extends Activity implements OnClickListener{

	private RelativeLayout search;
	private RelativeLayout collection;
	private RelativeLayout friends;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.center);
		initComponents();
	}

	private void initComponents() {
		search = (RelativeLayout)findViewById(R.id.center_search);
		collection = (RelativeLayout)findViewById(R.id.center_collection);
		friends = (RelativeLayout)findViewById(R.id.center_friends);
		search.setOnClickListener(this);
		collection.setOnClickListener(this);
		friends.setOnClickListener(this);
	}
	
	@Override
	public void onBackPressed() {
		ShutDown.shutDownActivity(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.center_search:
				startActivity(new Intent(Center.this, SearchArticle.class));
				AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			case R.id.center_collection:
				startActivity(new Intent(Center.this, ArticleCollection.class));
				AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			case R.id.center_friends:
				startActivity(new Intent(Center.this, Friends.class));
				AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
		}

	}
}
