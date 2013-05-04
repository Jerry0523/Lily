package com.jerry.lily;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.jerry.utils.DatabaseDealer;
import com.jerry.widget.SimpleEditAdapter;

public class Block extends ListActivity implements OnClickListener{
	private EditText edit;
	private List<String> blockList;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.block);
		initComponents();
		initList();
	}

	private void initList() {
		blockList = DatabaseDealer.getBlockList(Block.this);
		SimpleEditAdapter adapter = new SimpleEditAdapter(this, R.layout.list_edit_item, R.id.list_textview, R.id.list_select, R.id.list_delete, blockList) {
			@Override
			protected void onDelete(final int position) {
				DatabaseDealer.deleteFromBlock(Block.this, getItem(position));
				onResume();
			}
		};
		setListAdapter(adapter);
	}

	private void initComponents() {
		edit = (EditText)findViewById(R.id.block_edit);

		findViewById(R.id.block_back).setOnClickListener(this);
		findViewById(R.id.block_submit).setOnClickListener(this);
		findViewById(R.id.block_modify).setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	public SimpleEditAdapter getListAdapter() {
		return (SimpleEditAdapter) super.getListAdapter();
	}

	@Override
	protected void onResume() {
		super.onResume();
		List<String> newList = DatabaseDealer.getBlockList(Block.this);
		blockList.clear();
		for(String s : newList) {
			blockList.add(s);
		}
		getListAdapter().notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.block_back:
			onBackPressed();
			break;

		case R.id.block_submit:
			DatabaseDealer.add2Block(Block.this, edit.getText().toString());
			onResume();
			break;
		case R.id.block_modify:
			getListAdapter().changeEditingState();
			break;
		}

	}
}
