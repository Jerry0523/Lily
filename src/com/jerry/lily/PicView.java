package com.jerry.lily;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.jerry.utils.FileDealer;
import com.jerry.utils.PicDownloader;
import com.jerry.widget.IOSAlertDialog;
import com.jerry.widget.PinchableImageView;

public class PicView extends Activity implements OnClickListener{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageview);
		LinearLayout body = (LinearLayout) findViewById(R.id.imageview_body);
		PinchableImageView imageView = new PinchableImageView(PicView.this, body.getWidth(), body.getHeight());
		imageView.setImageDrawable(PicDownloader.getInstance().getPictureDrawable(getIntent().getStringExtra("source")));
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		body.addView(imageView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		((Button)findViewById(R.id.imageview_bbutton)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.imageview_save)).setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.imageview_bbutton:
			onBackPressed();
			break;
		case R.id.imageview_save:
			BitmapDrawable drawable = (BitmapDrawable) PicDownloader.getInstance().getPictureDrawable(getIntent().getStringExtra("source"));
			Bitmap bitmap = drawable.getBitmap();
			File file = new File(FileDealer.getPhotoDirPath() + getIntent().getStringExtra("source").substring(getIntent().getStringExtra("source").lastIndexOf("/")));
			try {
				FileOutputStream fOut = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				new IOSAlertDialog.Builder(PicView.this).setTitle("提示").setMessage("图像保存成功\n保存在/Lily/photo下").setNegativeButtonText("好").create().show();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
				new IOSAlertDialog.Builder(PicView.this).setTitle("警告").setMessage("文件创建失败").setNegativeButtonText("确定").create().show();
			}

			break;
		}
	}
}
