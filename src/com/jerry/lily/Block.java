package com.jerry.lily;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jerry.utils.DatabaseDealer;

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
		MyArrayAdapter adatper = new MyArrayAdapter(Block.this, R.layout.list_block, blockList);
		setListAdapter(adatper);
	}

	private void initComponents() {
		edit = (EditText)findViewById(R.id.block_edit);
		
		((Button)findViewById(R.id.block_back)).setOnClickListener(this);
		((Button)findViewById(R.id.block_submit)).setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	private class MyArrayAdapter extends ArrayAdapter<String> {
		int resourceId;
		public MyArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			resourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final String blockName = blockList.get(position);
			LinearLayout userListItem = new LinearLayout(getContext());  
			String inflater = Context.LAYOUT_INFLATER_SERVICE;   
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);   
			vi.inflate(resourceId, userListItem, true);  
			TextView tvName = (TextView)userListItem.findViewById(R.id.lib_name);  
			Button tvDelete = (Button)userListItem.findViewById(R.id.lib_delete);
			tvName.setText(blockName);
			tvDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DatabaseDealer.deleteFromBlock(Block.this, blockName);
					initList();
				}
			});
			return userListItem;  
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.block_back:
			onBackPressed();
			break;

		case R.id.block_submit:
			DatabaseDealer.add2Block(Block.this, edit.getText().toString());
			initList();
			break;
		}
		
	}
}
