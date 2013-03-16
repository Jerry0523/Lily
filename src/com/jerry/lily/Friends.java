package com.jerry.lily;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jerry.lily.AllBoard.ViewHolder;
import com.jerry.utils.DatabaseDealer;

public class Friends extends ListActivity{
	private Button back;
	private List<String> nameList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		if(DatabaseDealer.getSettings(Friends.this).isNight()) {
			((LinearLayout)findViewById(R.id.search_result_body)).setBackgroundDrawable(null);
		}
		((TextView)findViewById(R.id.search_result_title)).setText("ÊÕ²ØµÄºÃÓÑ");
		initComponents();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, Author.class);
		intent.putExtra("authorName", nameList.get(position));
		intent.putExtra("authorUrl", "http://bbs.nju.edu.cn/bbsqry?userid=" + nameList.get(position));
		startActivity(intent);
	}

	private void initComponents() {
		nameList = DatabaseDealer.getFriendsList(Friends.this);
		back = (Button)findViewById(R.id.search_result_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		setListAdapter(new MyArrayAdapter(R.layout.list_edit_item));
	}
	
	private class MyArrayAdapter extends BaseAdapter{
		private int layoutId;
		
		public MyArrayAdapter(final int layoutId) {
			this.layoutId = layoutId;
		}

		@Override
		public int getCount() {
			return nameList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return nameList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null) {
				convertView = Friends.this.getLayoutInflater().inflate(layoutId, null);
				holder = new ViewHolder();
				holder.boardName = (TextView) convertView.findViewById(R.id.list_textview);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.boardName.setText((String) getItem(position));
			return convertView;
		}

	}
}

