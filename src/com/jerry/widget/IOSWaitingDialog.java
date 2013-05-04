package com.jerry.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;

import com.jerry.lily.R;


public class IOSWaitingDialog extends Dialog {
	private static IOSWaitingDialog myProgressDialog = null;

	public IOSWaitingDialog(Context context, int theme) {
		super(context, theme);
	}

	public static IOSWaitingDialog createDialog(Context context){
		myProgressDialog = new IOSWaitingDialog(context,R.style.iosWaitingDialog);
		myProgressDialog.setContentView(R.layout.custom_progress_dialog);
		myProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		myProgressDialog.setCancelable(false);
		return myProgressDialog;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		if (myProgressDialog == null){
			return;
		}
		ImageView imageView = (ImageView) myProgressDialog.findViewById(R.id.loadingImageView);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
		animationDrawable.start();
	}
	public IOSWaitingDialog setTitile(String strTitle){
		return myProgressDialog;
	}
}

