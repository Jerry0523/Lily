package com.jerry.lily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SearchArticle extends Activity{
	
	private EditText author;
	private EditText title;
	private EditText title2;
	private ToggleButton day;
	private ToggleButton month;
	private ToggleButton week;
	private ToggleButton year;
	
	private Button back;
	private Button submit;
	
	private int time = 30;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_article);
		initComponents();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}
	
	private void initComponents() {
		author = (EditText)findViewById(R.id.search_author);
		title = (EditText)findViewById(R.id.search_title);
		title2 = (EditText)findViewById(R.id.search_title2);
		
		day = (ToggleButton)findViewById(R.id.search_day);
		week = (ToggleButton)findViewById(R.id.search_week);
		month = (ToggleButton)findViewById(R.id.search_month);
		year = (ToggleButton)findViewById(R.id.search_year);
		
		back = (Button)findViewById(R.id.search_back);
		submit = (Button)findViewById(R.id.search_submit);
		
		day.setOnClickListener(myOnClickListener);
		week.setOnClickListener(myOnClickListener);
		month.setOnClickListener(myOnClickListener);
		year.setOnClickListener(myOnClickListener);
		
		back.setOnClickListener(myOnClickListener);
		submit.setOnClickListener(myOnClickListener);
	}
	
	private void submitSeach() {
		Intent intent = new Intent(SearchArticle.this, SearchResult.class);
		intent.putExtra("author", author.getText().toString());
		intent.putExtra("title", title.getText().toString());
		intent.putExtra("title2", title2.getText().toString());
		intent.putExtra("time", time);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}
	
	private OnClickListener myOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.search_day:
				day.setChecked(true);
				week.setChecked(false);
				month.setChecked(false);
				year.setChecked(false);
				time = 1;
				break;
			case R.id.search_week:
				day.setChecked(false);
				week.setChecked(true);
				month.setChecked(false);
				year.setChecked(false);
				time = 7;
				break;
			case R.id.search_month:
				day.setChecked(false);
				week.setChecked(false);
				month.setChecked(true);
				year.setChecked(false);
				time = 30;
				break;
			case R.id.search_year:
				day.setChecked(false);
				week.setChecked(false);
				month.setChecked(false);
				year.setChecked(true);
				time = 365;
				break;
			case R.id.search_back:
				onBackPressed();
				break;
			case R.id.search_submit:
				submitSeach();
				break;
			}
			
		}
	};
}
