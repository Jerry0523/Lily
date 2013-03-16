package com.jerry.widget;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class TextPage extends EditText {

	public TextPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		setMovementMethod(LinkMovementMethod.getInstance());
		setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				setMovementMethod(getDefaultMovementMethod());
				(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						return false;
					}
				}).onLongClick(TextPage.this);
				return false;
			}
		});
	}

	// 点击menu中的选定item的具体处理方法，捕捉点击文本复制、剪切等按钮的动作
	// 如果要在点击复制按钮之后取消该textview的cursor可见性的具体监听写在这里
	@Override
	public boolean onTextContextMenuItem(int id) {
		setCursorVisible(true);
		boolean flag;
		if (id != android.R.id.switchInputMethod) {
			flag = super.onTextContextMenuItem(id);
		} else {
			setCursorVisible(false);
			return false;
		}
		if (id == android.R.id.copy) {
			setCursorVisible(false);
			cursorStart = -1;
		}
		return flag;
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		super.onCreateContextMenu(menu);
		if (isInputMethodTarget()) {
			menu.removeItem(android.R.id.switchInputMethod);
		}
	}
	
	@Override
	public void setCursorVisible(boolean visible) {
		super.setCursorVisible(visible);
		if(!visible) {
			setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	// textview的点击捕捉
	// 如果双击textview选中了具体文字，则使cursor可见
	int cursorStart = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean flag = super.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_DOWN && hasSelection()) {
			if (cursorStart == -1) {// 由于点击选中文字后，再点击其他位置，第一次点击时显示的hasSelection依然为true，这样一来cursor会依然还在，为了避免这种情况，我这里多对selectionStart进行了一次验证
				setCursorVisible(true);
				cursorStart = getSelectionStart();
			} else {
				setCursorVisible(false);
				cursorStart = -1;
			}
		}
		return flag;
	}

	// 当按返回键取消文字复制时，使cursor再次不可见
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = super.onKeyDown(keyCode, event);

		setCursorVisible(false);
		cursorStart = -1;
		return flag;
	}

}

