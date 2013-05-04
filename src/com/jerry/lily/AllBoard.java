package com.jerry.lily;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.jerry.utils.DatabaseDealer;

public class AllBoard extends ListActivity implements OnClickListener{
	private List<String> allBoardList;
	private EditText searchEditor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.allboard);
		initComponents();
	}
	
	private void initComponents() {
		if(DatabaseDealer.getSettings(AllBoard.this).isNight()) {
			((LinearLayout)findViewById(R.id.all_board_body)).setBackgroundDrawable(null);
		}
		
		allBoardList = DatabaseDealer.getAllBoardList(this, "");
		searchEditor = (EditText) findViewById(R.id.ab_search_editor);
		setListAdapter(new MyArrayAdapter(R.layout.list_edit_item));
		
		findViewById(R.id.ab_back).setOnClickListener(this);
		findViewById(R.id.ab_search).setOnClickListener(this);
		
		searchEditor.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				allBoardList.clear();
				allBoardList.addAll(DatabaseDealer.getAllBoardList(AllBoard.this, s.toString()));
				getListAdapter().notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public BaseAdapter getListAdapter() {
		// TODO Auto-generated method stub
		return (BaseAdapter) super.getListAdapter();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String fullName = allBoardList.get(position);
		String boardName = fullName.substring(0, fullName.indexOf("("));
		Intent intent = new Intent(this, BoardList.class);
		intent.putExtra("boardName", boardName);
		startActivity(intent);
		super.onListItemClick(l, v, position, id);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.keep_origin,R.anim.out_to_down); 
	}

	public static final class ViewHolder{
		public TextView boardName;
	}
	
	private class MyArrayAdapter extends BaseAdapter implements SectionIndexer{
		private int layoutId;
		private AlphabetIndexer alphabetIndexer;
		
		public MyArrayAdapter(final int layoutId) {
			this.layoutId = layoutId;
			alphabetIndexer = new AlphabetIndexer(DatabaseDealer.getAllBoardCursor(AllBoard.this), 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		}

		@Override
		public int getCount() {
			return allBoardList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return allBoardList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null) {
				convertView = AllBoard.this.getLayoutInflater().inflate(layoutId, null);
				holder = new ViewHolder();
				holder.boardName = (TextView) convertView.findViewById(R.id.list_textview);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.boardName.setText((String) getItem(position));
			return convertView;
		}

		@Override
		public Object[] getSections() {
			return alphabetIndexer.getSections();
		}

		@Override
		public int getPositionForSection(int section) {
			return alphabetIndexer.getPositionForSection(section);
		}

		@Override
		public int getSectionForPosition(int position) {
			return alphabetIndexer.getSectionForPosition(position);
		}
		
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ab_back) {
			onBackPressed();
		} else if(v.getId() == R.id.ab_search) {
			if(searchEditor.getVisibility() == View.VISIBLE) {
				searchEditor.setText("");
				searchEditor.setVisibility(View.GONE);
			} else {
				searchEditor.setVisibility(View.VISIBLE);
				searchEditor.requestFocus();
			}
		}
	}

}
