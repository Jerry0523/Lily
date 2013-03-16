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

	// ���menu�е�ѡ��item�ľ��崦��������׽����ı����ơ����еȰ�ť�Ķ���
	// ���Ҫ�ڵ�����ư�ť֮��ȡ����textview��cursor�ɼ��Եľ������д������
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

	// textview�ĵ����׽
	// ���˫��textviewѡ���˾������֣���ʹcursor�ɼ�
	int cursorStart = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean flag = super.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_DOWN && hasSelection()) {
			if (cursorStart == -1) {// ���ڵ��ѡ�����ֺ��ٵ������λ�ã���һ�ε��ʱ��ʾ��hasSelection��ȻΪtrue������һ��cursor����Ȼ���ڣ�Ϊ�˱��������������������selectionStart������һ����֤
				setCursorVisible(true);
				cursorStart = getSelectionStart();
			} else {
				setCursorVisible(false);
				cursorStart = -1;
			}
		}
		return flag;
	}

	// �������ؼ�ȡ�����ָ���ʱ��ʹcursor�ٴβ��ɼ�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = super.onKeyDown(keyCode, event);

		setCursorVisible(false);
		cursorStart = -1;
		return flag;
	}

}

