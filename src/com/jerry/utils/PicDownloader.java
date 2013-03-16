package com.jerry.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.jerry.lily.ArticleActivity;
import com.jerry.lily.R;

public class PicDownloader {
	private static ArticleActivity context;
	private static ExecutorService executorService = Executors.newSingleThreadExecutor();
	public static Map<String, Drawable> picMemoryCache = new HashMap<String, Drawable>();

	private static int screenWidth;

	public static final void getInstance(ArticleActivity articleActivity) {
		context = articleActivity;
		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels;
	}

	public static final int getScreenWidth() {
		return screenWidth;
	}

	public static final Drawable getPictureDrawable(String url) {
		Drawable drawable = null;
		String picDir = FileDealer.getCacheDir() + url.substring(url.lastIndexOf("/"));
		if(picMemoryCache.containsKey(picDir)) {
			return dealWithPictureSize(picMemoryCache.get(picDir), picDir);
		} else if(new File(picDir).exists()) {
			drawable = BitmapDrawable.createFromPath(picDir);
		} else {
			downLoadPic(url);
		}
		return dealWithPictureSize(drawable, drawable == null ? null : picDir);
	}

	private static final Drawable dealWithPictureSize(Drawable drawable, String key) {
		if(drawable == null) {
			drawable = context.getResources().getDrawable(R.drawable.downloading);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			return drawable;
		}
		int realWidth = drawable.getIntrinsicWidth();
		int realHeight = drawable.getIntrinsicHeight();
		if(realWidth < 100 || realHeight < 100) {
			drawable.setBounds(0, 0, realWidth, realHeight);
		} else {
			drawable.setBounds((int) (0.01 * screenWidth), 0, (int) (screenWidth * 0.94), (int) ((screenWidth * 0.96) * realHeight/ realWidth));
		}
		if(key != null) {
			picMemoryCache.put(key, drawable);
		}
		return drawable;
	}

	public static final void downLoadPic(final String url) {
		if(context == null || !DatabaseDealer.getSettings(context).isShowPic()) {
			return;
		}
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					FileDealer.downloadBitmap(url, screenWidth);
					context.refreshPic();
				} catch (IOException e) {
				}
			}
		});
	}

	public static final void clearMemoryPicCache() {
		for (Object obj : picMemoryCache.keySet()) {
			 Drawable pic = picMemoryCache.get(obj);
			 if(pic instanceof BitmapDrawable) {
				 ((BitmapDrawable)pic).getBitmap().recycle();
			 }
		}
		picMemoryCache.clear();
	}

	public static final void stopDownloadThread() {
		clearMemoryPicCache();
		executorService.shutdownNow();
		executorService = Executors.newSingleThreadExecutor();
		context = null;
	}
}
