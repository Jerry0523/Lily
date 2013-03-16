package com.jerry.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SimpleEditAdapter extends SimpleNameAdapter{

	private int chooseButtonId;
	private int deleteButtonId;

	private Button currentDeleteButton;
	private ToggleButton currentChooseButton;

	private boolean isEditing;

	private Animation rotationAnimation;

	public SimpleEditAdapter(Context context, int layoutId, int textViewId,int chooseButtonId, int deleteButtonId, List<String> data) {
		super(context, layoutId, textViewId, data);
		this.chooseButtonId = chooseButtonId;
		this.deleteButtonId = deleteButtonId;
		this.rotationAnimation =  new RotateAnimation(0.0f, 90.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
		this.rotationAnimation.setDuration(180);
		this.rotationAnimation.setFillAfter(false);
	}

	protected void onDelete(int position) {
		data.remove(position);
		isEditing = true;
		changeEditingState();
	}

	public void deleteItem(int position) {
		data.remove(position);
		isEditing = true;
		changeEditingState();
	}

	public void changeEditingState() {
		this.isEditing = !isEditing;
		if(currentDeleteButton != null && currentDeleteButton.getVisibility() == View.VISIBLE) {
			currentDeleteButton.setVisibility(View.GONE);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutId, null);
			holder = new ViewHolder();
			holder.itemName = (TextView) convertView.findViewById(textViewId);
			holder.chooseButton = (ToggleButton) convertView.findViewById(chooseButtonId);
			holder.deleteButton = (Button) convertView.findViewById(deleteButtonId);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDelete(position);
			}
		});

		if(isEditing) {
			holder.chooseButton.setChecked(false);
			holder.chooseButton.setVisibility(View.VISIBLE);
			holder.chooseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(currentDeleteButton != null) {
						currentDeleteButton.setVisibility(View.GONE);
					}
					if(currentChooseButton != null && currentChooseButton != holder.chooseButton) {
						currentChooseButton.setChecked(false);
					}
					holder.chooseButton.startAnimation(rotationAnimation);
					if(holder.chooseButton.isChecked()) {
						holder.deleteButton.setVisibility(View.VISIBLE);
					} else {
						holder.deleteButton.setVisibility(View.GONE);
					}
					currentDeleteButton = holder.deleteButton;
					currentChooseButton = holder.chooseButton;
				}
			});
		} else {
			holder.chooseButton.setVisibility(View.GONE);
		}

		convertView.setOnTouchListener(new OnTouchListener() {  
			double x;
			double ux;

			public boolean onTouch(View v, MotionEvent event) {
				if(isEditing) {
					return false;
				}
				final ViewHolder holder = (ViewHolder) v.getTag();
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					//					v.setBackgroundColor(Color.rgb(147, 158, 165));
					x = event.getX();
					if (currentDeleteButton != null && currentDeleteButton.getVisibility() == View.VISIBLE) {  
						currentDeleteButton.setVisibility(View.GONE);  
					}
					return false;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					v.setBackgroundColor(Color.WHITE);
					ux = event.getX();  
					if (Math.abs(x - ux) < 20) {
						return false;
					} else {
						holder.deleteButton.setVisibility(View.VISIBLE);  
						currentDeleteButton = holder.deleteButton;
						return true;
					}
				}  
				return true;
			}  
		}); 

		holder.itemName.setText(data.get(position));
		return convertView;
	}

	private static final class ViewHolder{
		public TextView itemName;
		public ToggleButton chooseButton;
		public Button deleteButton;
	}

}
