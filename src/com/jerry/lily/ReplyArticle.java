package com.jerry.lily;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;
import com.jerry.utils.FileDealer;
import com.jerry.widget.IOSAlertDialog;
import com.jerry.widget.IOSWaitingDialog;

public class ReplyArticle extends Activity implements OnClickListener{
	private EditText title;
	private EditText content;

	private boolean isTitleVisiable;

	private GridView gridView;

	private int[] emotionId = getEmotionIdArray();
	private String[] emotionString = getEmotionStringArray();

	private File tmpPhoto;

	private IOSWaitingDialog waitingDialog;
	private android.content.DialogInterface.OnClickListener positiveLis;
	private android.content.DialogInterface.OnClickListener negativeLis;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.reply);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		initComponents();
	}

	private Handler mHandler = new Handler(){ 
		@Override
		public void handleMessage(Message msg) {
			if (waitingDialog != null) {
				waitingDialog.cancel();
			}
			switch (msg.what) {
			case 0:
				android.content.DialogInterface.OnClickListener success = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						setResult(Constants.SEND_REPLY);
						onBackPressed();
					}
				};
				new IOSAlertDialog.Builder(ReplyArticle.this).setTitle("提示").setMessage("发送成功").setNegativeButton("好", success).create().show();
				break;
			case 1:
				if(positiveLis == null) {
					positiveLis = new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							sendArticle();
							dialog.dismiss();
						}
					};
				}

				if(negativeLis == null) {
					negativeLis = new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							onBackPressed();
						}
					};
				}
				new IOSAlertDialog.Builder(ReplyArticle.this).setTitle("警告").setMessage("发送失败，是否重试?").setPositiveButton("好", positiveLis).setNegativeButton("取消", negativeLis).create().show();
				break;
			case 2:
				String picUrl = (String) msg.obj;
				if(content.length() == 0) {
					content.append(picUrl + '\n');
				} else {
					content.append('\n' + picUrl + '\n');
				}
				
				break;
			case 3:
				new IOSAlertDialog.Builder(ReplyArticle.this).setTitle("警告").setMessage("图片上传失败").setNegativeButtonText("好").create().show();
				break;
			}

		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case Constants.GALLERY:
			if (data == null) {
				return;
			}
			Uri uri = data.getData();
			if (uri == null) {
				return;
			}
			String[] proj = { MediaStore.Images.Media.DATA };  
			Cursor actualimagecursor = managedQuery(uri,proj,null,null,null);  
			int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
			actualimagecursor.moveToFirst();  
			String imgPath= actualimagecursor.getString(actual_image_column_index); 
			actualimagecursor.close();
			uploadPicture(imgPath);
			break;
		case Constants.CAMERA:
			if(tmpPhoto == null || !tmpPhoto.exists()) {
				return;
			}
			uploadPicture(tmpPhoto.getPath());
			break;
		}
	}

	private void initComponents() {
		content = (EditText)findViewById(R.id.reply_edit);
		title = (EditText) findViewById(R.id.reply_title);

		content.setOnClickListener(this);
		title.setOnClickListener(this);

		((Button)findViewById(R.id.reply_submit)).setOnClickListener(this);
		((Button)findViewById(R.id.reply_quit)).setOnClickListener(this);

		((ImageButton)findViewById(R.id.reply_input)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.reply_pic)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.reply_photo)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.reply_expression)).setOnClickListener(this);
		gridView = (GridView)findViewById(R.id.gridview);

		gridView.setAdapter(new EmotionAdapter());

		isTitleVisiable = getIntent().getBooleanExtra("isTitleVisiable", true);
		if(!isTitleVisiable) {
			title.setVisibility(View.GONE);
		}
	}

	private int[] getEmotionIdArray() {
		int[] array = {R.drawable.emotion_0,R.drawable.emotion_1, R.drawable.emotion_2, R.drawable.emotion_3, R.drawable.emotion_4,
				R.drawable.emotion_5, R.drawable.emotion_6, R.drawable.emotion_7, R.drawable.emotion_8, R.drawable.emotion_9,
				R.drawable.emotion_a,R.drawable.emotion_b, R.drawable.emotion_c, R.drawable.emotion_d, R.drawable.emotion_e,
				R.drawable.emotion_f, R.drawable.emotion_g, R.drawable.emotion_h, R.drawable.emotion_i, R.drawable.emotion_j,
				R.drawable.emotion_k, R.drawable.emotion_l, R.drawable.emotion_m, R.drawable.emotion_n, R.drawable.emotion_o,
				R.drawable.emotion_p, R.drawable.emotion_q, R.drawable.emotion_r, R.drawable.emotion_s, R.drawable.emotion_t,
				R.drawable.emotion_u, R.drawable.emotion_v, R.drawable.emotion_w, R.drawable.emotion_x, R.drawable.emotion_y,
				R.drawable.emotion_z};
		return array;

	}

	private String[] getEmotionStringArray() {
		String[] array = {"[:-b]","[:-8]","[;PT]","[:hx]","[;K]","[:E]","[:-(]","[;hx]","[:-v]","[;xx]",
				"[:@]","[:)]","[:(]","[:$]","[:D]","[:Q]","[:T]","[:-|]","[;P]","[;-D]","[:!]","[:L]",
				"[:?]","[:U]","[:O]","[:P]","[:'(]","[:K]","[:s]","[:C-]","[;X]","[:|]","[:H]","[:X]",
				"[;bye]","[;cool]"};
		return array;
	}

	private class EmotionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return emotionId.length;
		}

		@Override
		public Object getItem(int position) {
			return emotionString[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final TextView img = new TextView(ReplyArticle.this);
			img.setBackgroundResource(emotionId[position]);
			img.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						img.setWidth(img.getWidth() + 5);
						img.setHeight(img.getHeight() + 5);
					} else if(event.getAction() == MotionEvent.ACTION_UP){
						img.setWidth(img.getWidth() - 5);
						img.setHeight(img.getHeight() - 5);
						content.append((String)getItem(position));
					}
					return true;
				}
			});
			return img;
		}

	}

	private void uploadPicture(final String picPath) {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();
		Thread uploadPic = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Message msg  = Message.obtain();
					msg.what = 2;
					msg.obj = DocParser.upLoadPic2Server(picPath, getIntent().getStringExtra("board"), ReplyArticle.this);
					mHandler.sendMessage(msg);
				} catch (IOException e) {
					mHandler.sendEmptyMessage(3);
				}			
			}
		});

		uploadPic.start();
	}

	private void sendArticle() {
		if(waitingDialog == null) {
			waitingDialog = IOSWaitingDialog.createDialog(this);
		}
		waitingDialog.show();
		Thread sendReply = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					boolean success = isTitleVisiable ? postNewArticle() : sendReply();
					if(!isTitleVisiable && DatabaseDealer.getSettings(ReplyArticle.this).isSendMail()) {
						String authorName = getIntent().getStringExtra("authorName");
						String boardName = getIntent().getStringExtra("board");
						String content = "我在[" + boardName + "]版回复了您的帖子《" + getIntent().getStringExtra("title") + "》" + '\n' 
								+ "帖子链接为   " + getIntent().getStringExtra("contentUrl") + '\n' 
								+ "我的回复是:" + ReplyArticle.this.content.getText().toString() + '\n' + '\n' + "以上由小百合Android客户端自动发出，欢迎试用！~";
						DocParser.sendMail(authorName, content, ReplyArticle.this);

					}
					mHandler.sendEmptyMessage(success ? 0 : 1);
				} catch (IOException e1) {
					mHandler.sendEmptyMessage(1);
				}
			}
		});
		sendReply.start();
	}

	private boolean postNewArticle() throws IOException {
		String boardName = getIntent().getStringExtra("board");
		return DocParser.sendReply(boardName, title.getText().toString(), "0", "0", content.getText().toString(), null, ReplyArticle.this);
	}

	private boolean sendReply() throws IOException {

		String replyUrl = getIntent().getStringExtra("replyUrl");
		String authorName = getIntent().getStringExtra("authorName");
		String boardName = getIntent().getStringExtra("board");
		String title = getIntent().getStringExtra("title");
		String reIdString = replyUrl.substring(replyUrl.indexOf("M.") + 2);
		reIdString = reIdString.substring(0, reIdString.indexOf(".A"));

		String pidString = DocParser.getPid(replyUrl, ReplyArticle.this);
		if (pidString == null) {
			return false;
		}
		title = title.contains("○") ? title.replace("○", "Re:") : ("Re: " + title);
		return DocParser.sendReply(boardName, title, pidString, reIdString, content.getText().toString(), authorName, ReplyArticle.this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reply_quit:
			setResult(Constants.CANCEL_REPLY);
			onBackPressed();
			break;
		case R.id.reply_submit:
			if (content.getText() == null || content.getText().length() == 0) {
				new IOSAlertDialog.Builder(ReplyArticle.this).setTitle("警告").setMessage("内容为空，本次发送取消").setNegativeButtonText("好").create().show();
				return;
			}
			if (isTitleVisiable && (title.getText() == null || title.getText().length() == 0)) {
				new IOSAlertDialog.Builder(ReplyArticle.this).setTitle("警告").setMessage("标题为空，本次发送取消").setNegativeButtonText("好").create().show();
				return;
			}
			sendArticle();
			break;

		case R.id.reply_edit:
			gridView.setVisibility(View.GONE);
			break;
		case R.id.reply_title:
			gridView.setVisibility(View.GONE);
			break;
		case R.id.reply_input:
			gridView.setVisibility(View.GONE);
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			break;
		case R.id.reply_expression:
			gridView.setVisibility(View.VISIBLE);
			((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			break;
		case R.id.reply_photo:
			try {
				String filePath = FileDealer.getPhotoDirPath() + "/" + UUID.randomUUID() + ".jpg";
				tmpPhoto = new File(filePath);
				Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
				photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpPhoto));
				startActivityForResult(photoIntent, Constants.CAMERA);
			} catch (ActivityNotFoundException e) {  
				Toast.makeText(ReplyArticle.this, "无法打开相册",Toast.LENGTH_LONG).show();  
			}  
			break;
		case R.id.reply_pic:
			try {  
				Intent picIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
				picIntent.setType("image/*");
				picIntent.putExtra("return-data", true);  
				startActivityForResult(picIntent, Constants.GALLERY);  
			} catch (ActivityNotFoundException e) {  
				Toast.makeText(ReplyArticle.this, "无法打开相册",Toast.LENGTH_LONG).show();  
			}  
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out); 
	}

}
