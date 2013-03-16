package com.jerry.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.jerry.lily.R;

public class PageBackController extends View implements OnGestureListener, OnTouchListener{
	private Bitmap controller;
	private Paint paint;
	private int dragDistance;
	private GestureDetector myGesture;
	private Rect rect;
	private int width;
	private int height;
	private boolean isDrag;

	private PageBackListener listener;
	private int alpha = 255;
	private View sibling;

	public PageBackController(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		controller = BitmapFactory.decodeResource(getResources(), R.drawable.page_back);
		width = controller.getWidth();
		height = controller.getHeight();

		myGesture = new GestureDetector(context, this);
		myGesture.setIsLongpressEnabled(false);
		setClickable(true);
		setOnTouchListener(this);
		reset();
	}

	public void onDestroy() {
		controller.recycle();
		myGesture = null;
		listener = null;
		sibling = null;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(rect == null) {
			rect = new Rect(dragDistance, (getHeight() - height) / 2, dragDistance + width, (getHeight() - height) / 2 + height);
		} else {
			rect.left = dragDistance;
			rect.right = dragDistance + width;
		}
		paint.setAlpha(alpha);
		canvas.drawBitmap(controller, null, rect, paint);
		super.onDraw(canvas);
	}

	public void setPageBackListener(PageBackListener listener) {
		this.listener = listener;
	}

	public void setSibling(View view) {
		this.sibling = view;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	private void reset() {
		dragDistance = - width;
		isDrag = false;
	}

	private void onDrag(float distance) {
		isDrag = true;
		dragDistance += distance;
		if(dragDistance > 0) {
			dragDistance = 0;
		}
		alpha = (int) ((width + dragDistance) * 255 / width);
		postInvalidate();
	}

	private void afterDrag() {
		if(dragDistance == 0) {
			setVisibility(View.GONE);
			if(listener != null) {
				listener.onPageBack();
			}
		} else {
			new  AsynMove().execute(new Integer[] {20});
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if(distanceX > -5 || Math.abs(distanceY) > 10) {
			return false;
		}
		onDrag(-distanceX);
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP && isDrag) {
			afterDrag();
			return true;
		}

		if(event.getAction() == MotionEvent.ACTION_MOVE && myGesture.onTouchEvent(event)) {
			return true;
		}
		if(dragDistance == -width && sibling != null && !isDrag) {
			return sibling.dispatchTouchEvent(event);
		}
		return true;
	}

	private class AsynMove extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			int times;
			if ((width + dragDistance) % Math.abs(params[0])  ==  0) {
				times  =  (width + dragDistance) / Math.abs(params[0]);
			} else {
				times  =  (width + dragDistance) / Math.abs(params[0]) + 1;
			}

			for (int i  =  0; i < times; i++) {
				publishProgress(params);
				try {
					Thread.sleep(Math.abs(params[0]));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... params) {
			dragDistance -= params[0];
			if(dragDistance < -width) {
				reset();
			}
			postInvalidate();
		}
	}

	public interface PageBackListener {
		public void onPageBack();
	}
}
