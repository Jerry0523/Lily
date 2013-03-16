package com.jerry.lily;

import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Sign extends Activity{
	private Button submit;
	private Button quit;
	private EditText edit;
	
	private ToggleButton preCustom;
	private ToggleButton custom;
	
	private String sign;
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

	private void initComponents() {
		submit = (Button)findViewById(R.id.sign_submit);
		quit = (Button)findViewById(R.id.sign_exit);
		edit = (EditText)findViewById(R.id.sign_sign);
		preCustom = (ToggleButton)findViewById(R.id.sign_precustom);
		custom = (ToggleButton)findViewById(R.id.sign_custom);
		sign = DatabaseDealer.getSettings(Sign.this).getSign();
		
		if(sign.equals(Constants.sign)) {
			preCustom.setChecked(true);
			custom.setChecked(false);
			edit.setVisibility(View.INVISIBLE);
		} else {
			preCustom.setChecked(false);
			custom.setChecked(true);
			edit.setVisibility(View.VISIBLE);
			edit.setText(sign);
		}
		
		preCustom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				preCustom.setChecked(true);
				custom.setChecked(false);
				edit.setVisibility(View.INVISIBLE);
			}
		});
		
		custom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				preCustom.setChecked(false);
				custom.setChecked(true);
				edit.setVisibility(View.VISIBLE);
			}
		});

		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sign = preCustom.isChecked() ? Constants.sign : edit.getText().toString();
				DatabaseDealer.updateSettings(Sign.this, "sign", sign);
				Toast.makeText(getApplicationContext(), "±£´æ³É¹¦", Toast.LENGTH_SHORT).show();
				onBackPressed();
			}
		});

		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
}
