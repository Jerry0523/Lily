package com.jerry.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.jerry.lily.R;

public class Switcher extends View implements OnTouchListener{

	private Bitmap ball;
	private Bitmap backgroundOn;
	private Bitmap backgroundOff;
	private Bitmap backgroundOffDisable;
	private Bitmap backgroundOnDisable;

	private Paint paint;
	private float eventX;
	private int padding = 5;

	private OnCheckedChangeListener changeListener;

	public Switcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		ball = BitmapFactory.decodeResource(getResources(), R.drawable.switch_thumb);
		backgroundOn = BitmapFactory.decodeResource(getResources(), R.drawable.switch_bg_on);
		backgroundOff = BitmapFactory.decodeResource(getResources(), R.drawable.switch_bg_off);
		backgroundOnDisable = BitmapFactory.decodeResource(getResources(), R.drawable.switch_bg_on_disable);
		backgroundOffDisable = BitmapFactory.decodeResource(getResources(), R.drawable.switch_bg_off_disable);
		setValue(true);
		setOnTouchListener(this);
	}

	public void recyle() {
		ball.recycle();
		backgroundOn.recycle();
		backgroundOff.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(backgroundOn.getWidth() + padding * 2, backgroundOn.getHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap toBeDrawed;
		if(isEnabled()) {
			if(getValue()) {
				toBeDrawed = backgroundOn;	
			} else {
				toBeDrawed = backgroundOff;
			}
		} else {
			if(getValue()) {
				toBeDrawed = backgroundOnDisable;	
			} else {
				toBeDrawed = backgroundOffDisable;
			}
		}
		canvas.drawBitmap(toBeDrawed, getLeftPaddingOffset() + padding, 0, paint);

		canvas.drawBitmap(ball, eventX - ball.getWidth(), 0, paint);
		super.onDraw(canvas);
	}

	public boolean getValue() {
		return (eventX - ball.getWidth())/ getWidth() > 0.5;
	}

	public void setValue(boolean value) {
		eventX = value ? padding + getPaddingLeft() + backgroundOn.getWidth() : padding + getPaddingLeft() + ball.getWidth();
		postInvalidate();
	}

	public void addChangeListener(OnCheckedChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		eventX = event.getX();
		eventX = eventX < padding + getPaddingLeft() + ball.getWidth()? padding + getPaddingLeft() + ball.getWidth(): eventX;
		eventX = eventX > getWidth() - padding - getPaddingRight() ? getWidth() - padding - getPaddingRight() : eventX;
		postInvalidate();

		if(event.getAction() == MotionEvent.ACTION_UP) {
			setValue(getValue());
			if(changeListener != null) {
				changeListener.onCheckedChanged(null, getValue());
			}
		}

		return true;
	}
}
