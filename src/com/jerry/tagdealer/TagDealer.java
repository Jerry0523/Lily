package com.jerry.tagdealer;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.jerry.lily.R;

public class TagDealer{
	private static TagHandler mTagHandler;
	private static Context mContext;
	private static boolean isCloseTag = false;
	private static int startIndex = 0;

	public static final TagHandler getInstance(Context context) {
		mContext = context;
		if(mTagHandler == null) {
			init();
		}
		return mTagHandler;
	}

	private static void init() {
		mTagHandler = new TagHandler() {
			@Override
			public void handleTag(boolean opening, String tag, Editable output,
					XMLReader xmlReader) {
				if(!opening) {
					return;
				}
				if(tag.equals("uid")) {
					processUid(opening, output);
				} else if(tag.equals("emotion")) {
					processEmotion(opening, output);
				} else if(tag.equals("pic")) {
					processPicture(opening, output);
				} else if(tag.equals("url")) {
					processUrl(opening, output);
				} else if(tag.equals("wma")) {
					processMusic(opening, output);
				}
			}

			private void processMusic(boolean opening, Editable output) {
				if(!isCloseTag) {
					startIndex = output.length();
					isCloseTag = true;
				} else {
					String musicUrl = output.subSequence(startIndex, output.length()).toString();
					MusicSpan musicSpan = new MusicSpan(musicUrl, mContext);
					output.setSpan(musicSpan, startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					output.setSpan(musicSpan.getClickAction(), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					isCloseTag = false;
				}
			}

			private void processUrl(boolean opening, Editable output) {

				if(!isCloseTag) {
					startIndex = output.length();
					isCloseTag = true;
				} else {
					String url = output.subSequence(startIndex, output.length()).toString();
					output.setSpan(new UrlSpan(url, mContext), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					isCloseTag = false;
				}
			}

			private void processPicture(boolean opening, Editable output) {
				if(!isCloseTag) {
					startIndex = output.length();
					isCloseTag = true;
				} else {
					String picSource = output.subSequence(startIndex, output.length()).toString();
					if(picSource.equals("http://bbs.nju.edu.cn/file/W/WStaotao/sign.png")) {
						Drawable drawable = mContext.getResources().getDrawable(R.drawable.sign);
						drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
						output.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else {
						PictureSpan picSpan = new PictureSpan(picSource, mContext);
						output.setSpan(picSpan, startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						output.setSpan(picSpan.getClickAction(), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					isCloseTag = false;
				}
			}

			private void processEmotion(boolean opening, Editable output) {
				if(!isCloseTag) {
					startIndex = output.length();
					isCloseTag = true;
				} else {
					String emotionSource = output.subSequence(startIndex, output.length()).toString();
					output.setSpan(new ImageSpan(getEmotionDrawable(emotionSource)), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					isCloseTag = false;
				}
			}

			private void processUid(boolean opening, Editable output) {

				if(!isCloseTag) {
					startIndex = output.length();
					isCloseTag = true;
				} else {
					String username = output.subSequence(startIndex, output.length()).toString();
					output.setSpan(new UserSpan(username, mContext), startIndex, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					isCloseTag = false;
				}
			}
		};
	}

	private static final Drawable getEmotionDrawable(String source) {
		Drawable drawable = null;
		if(source.equals("s")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_s);
		} else if(source.equals("o")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_o);
		} else if(source.equals("v")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_v);
		} else if(source.equals("d")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_d);
		} else if(source.equals("x")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_x);
		} else if(source.equals("q")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_q);
		} else if(source.equals("a")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_a);
		}else if(source.equals("p")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_p);
		}else if(source.equals("e")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_e);
		}else if(source.equals("h")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_h);
		}else if(source.equals("b")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_b);
		}else if(source.equals("c")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_c);
		}else if(source.equals("f")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_f);
		}else if(source.equals("g")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_g);
		}else if(source.equals("i")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_i);
		}else if(source.equals("j")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_j);
		}else if(source.equals("k")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_k);
		}else if(source.equals("l")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_l);
		}else if(source.equals("m")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_m);
		}else if(source.equals("n")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_n);
		}else if(source.equals("r")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_r);
		}else if(source.equals("t")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_t);
		}else if(source.equals("u") ) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_u);
		}else if(source.equals("w")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_w);
		}else if(source.equals("y")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_y);
		}else if(source.equals("z")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_z);
		}else if(source.equals("0")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_0);
		}
		else if(source.equals("1")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_1);
		}
		else if(source.equals("2")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_2);
		}
		else if(source.equals("3")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_3);
		}
		else if(source.equals("4")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_4);
		}
		else if(source.equals("5")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_5);
		}
		else if(source.equals("6")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_6);
		}
		else if(source.equals("7")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_7);
		}
		else if(source.equals("8")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_8);
		}
		else if(source.equals("9")) {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_9);
		} else {
			drawable = mContext.getResources().getDrawable(R.drawable.emotion_9);
		}
		if(drawable != null) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		return drawable;
	}

}
