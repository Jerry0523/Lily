package com.jerry.lily;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent.OnFinished;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.jerry.widget.IOSWaitingDialog;

public class Url extends Activity implements OnClickListener{
	private IOSWaitingDialog waitingDialog;
	private WebView webView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		waitingDialog = IOSWaitingDialog.createDialog(Url.this);
		waitingDialog.show();
		setContentView(R.layout.url);
		String url = getIntent().getStringExtra("url");
		
		Button backButton = (Button) findViewById(R.id.url_bbutton);
		webView = (WebView) findViewById(R.id.url_webview);
		
		backButton.setOnClickListener(this);
		webView.setWebViewClient(new WebViewClient() {  
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				waitingDialog.show();
				view.loadUrl(url); 
				return true;  
			}  
		});
		
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {//载入进度改变而触发
				if (progress == 100) {
					waitingDialog.dismiss();
				}
				super.onProgressChanged(view, progress);
			}
		});
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUseWideViewPort(true); 
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setBuiltInZoomControls(true);
		
		webView.loadUrl(url);
	}
	
	@Override
	public void onBackPressed() {
		if(webView.canGoBack()) {
			webView.goBack();
			return;
		}
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.url_bbutton:
			finish();
			overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
			break;
		}
		
	}
}
