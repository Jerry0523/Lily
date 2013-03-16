package com.jerry.lily;

import java.io.IOException;
import java.util.List;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.model.LoginInfo;
import com.jerry.utils.AnimCommon;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.ShutDown;
import com.jerry.widget.IOSAlertDialog;
import com.jerry.widget.SimpleEditAdapter;
import com.jerry.widget.XListView;
import com.jerry.widget.XListView.IXListViewListener;

public class Board extends ListActivity implements OnClickListener, IXListViewListener{
	private List<String> favList;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				onResume();
				getListView().stopRefresh();
				getListView().setRefreshTime(DocParser.getLastUpdateTime());
				break;
			case 1:
				Toast.makeText(Board.this, "ÍøÂçÒì³££¬ÇëÉÔºóÔÙÊÔ!", Toast.LENGTH_SHORT).show();
				getListView().stopRefresh();
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.board);
		initComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		List<String> newList = DatabaseDealer.getFavList(Board.this);
		favList.clear();
		for(String s : newList) {
			favList.add(s);
		}
		getListAdapter().notifyDataSetChanged();
	}

	private void initComponents() {
		favList = getIntent().getStringArrayListExtra("favList");
		((ImageButton)findViewById(R.id.all_board)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.board_edit)).setOnClickListener(this);
		((TextView)findViewById(R.id.board_title)).setOnClickListener(this);

		SimpleEditAdapter adapter = new SimpleEditAdapter(this, R.layout.list_edit_item, R.id.list_textview, R.id.list_select, R.id.list_delete, favList) {
			@Override
			protected void onDelete(final int position) {
				android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DatabaseDealer.deleteSpecialFav(Board.this, getItem(position));
						onResume();
						dialog.dismiss();

					}
				};
				new IOSAlertDialog.Builder(Board.this).setTitle("×¢Òâ").setMessage("¼´½«É¾³ý" + getItem(position) + '\n' + "ÊÇ·ñ¼ÌÐø?").setPositiveButton("¼ÌÐø", listener).create().show();
			}
		};
		getListView().setXListViewListener(this);
		getListView().setPullLoadEnable(false);
		getListView().setFooterViewForbidden(true);
		setListAdapter(adapter);
	}

	@Override
	public void onBackPressed() {
		ShutDown.shutDownActivity(this);
	}

	@Override
	public XListView getListView() {
		return (XListView) super.getListView();
	}

	@Override
	public SimpleEditAdapter getListAdapter() {
		return (SimpleEditAdapter) super.getListAdapter();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.all_board:
			Intent intent = new Intent(Board.this, AllBoard.class);
			startActivity(intent);
			AnimCommon.set(R.anim.in_from_up,R.anim.keep_origin);
			break;
		case R.id.board_edit:
			getListAdapter().changeEditingState();
		case R.id.board_title:
			getListView().setSelectionAfterHeaderView();
		}

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(Board.this, BoardList.class);
		intent.putExtra("boardName", favList.get(position - 1));
		startActivity(intent);
		AnimCommon.set(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	@Override
	public void onRefresh() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DatabaseDealer.addFav(Board.this, DocParser.synchronousFav(LoginInfo.getInstance(Board.this)));
				} catch (IOException e) {
					mHandler.sendEmptyMessage(1);
				}

				mHandler.sendEmptyMessage(0);
			}
		});
		thread.start();
	}

	@Override
	public void onLoadMore() {

	}
}
