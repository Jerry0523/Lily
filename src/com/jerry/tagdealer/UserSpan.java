package com.jerry.tagdealer;

import android.content.Context;
import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import com.jerry.lily.Author;

public class UserSpan extends ClickableSpan{
	private String userName;
	private Context context;
	
	public UserSpan(String userName, Context context) {
		this.userName = userName;
		this.context = context;
	}
	@Override
	public void onClick(View widget) {
		if(userName.equals("deliver")) {
			Toast.makeText(context, "系统管理员，无信息!", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(context, Author.class);
		intent.putExtra("authorUrl", "http://bbs.nju.edu.cn/bbsqry?userid=" + userName);
		intent.putExtra("authorName", userName);
		context.startActivity(intent);
	}
}