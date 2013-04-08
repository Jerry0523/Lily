package com.jerry.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
		}
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
		alpha = 255;
	}

	private void onDrag(float distance) {
		isDrag = true;
		dragDistance += distance;
		if(dragDistance > 0) {
			dragDistance = 0;
		}

		if(dragDistance < -width) {
			dragDistance = -width;
		}
		alpha = (int) ((width + dragDistance) * 255 / width);
		rect.left = dragDistance;
		rect.right = dragDistance + width;
		paint.setAlpha(alpha);

		postInvalidate(rect.left, rect.top, rect.right, rect.bottom);
	}

	private void afterDrag() {
		if(dragDistance == 0) {
			setVisibility(View.GONE);
			if(listener != null) {
				listener.onPageBack();
			}
		} else {
			reset();
			rect.left = dragDistance;
			rect.right = dragDistance + width;
			postInvalidate();
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if(Math.abs(distanceX) < 10 || Math.abs(distanceY) > 10) {
			return false;
		}

		if(distanceX > 0 && !isDrag) {
			return false;
		}
		onDrag(-distanceX * 2 / 3);
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

	public interface PageBackListener {
		public void onPageBack();
	}
}
