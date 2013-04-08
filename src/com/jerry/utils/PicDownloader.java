package com.jerry.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.jerry.lily.ArticleActivity;
import com.jerry.lily.R;

public class PicDownloader {
	private ArticleActivity context;
	private ExecutorService executorService = Executors.newFixedThreadPool(2);
	public Map<String, Drawable> picMemoryCache = new HashMap<String, Drawable>();
	
	private Set<String> downloadingKey = new HashSet<String>();
	private int screenWidth;
	private static PicDownloader THIS;

	public static final PicDownloader getInstance(ArticleActivity articleActivity) {
		if(THIS == null) {
			THIS = new PicDownloader();
			DisplayMetrics metric = new DisplayMetrics();
			articleActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
			THIS.screenWidth = metric.widthPixels;
		}
		THIS.context = articleActivity;
		return THIS;
	}

	public static final PicDownloader getInstance() {
		return THIS;
	}

	public final Drawable getPictureDrawable(String url) {
		Drawable drawable = null;
		String picDir = FileDealer.getCacheDir() + url.substring(url.lastIndexOf("/"));
		if(picMemoryCache.containsKey(picDir)) {
			return dealWithPictureSize(picMemoryCache.get(picDir), null);
		} else if(new File(picDir).exists()) {
			drawable = BitmapDrawable.createFromPath(picDir);
		} else if(!downloadingKey.contains(url)){
			downLoadPic(url);
		}
		return dealWithPictureSize(drawable, drawable == null ? null : picDir);
	}

	private final Drawable dealWithPictureSize(Drawable drawable, String key) {
		if(drawable == null) {
			drawable = context.getResources().getDrawable(R.drawable.downloading);
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

	public final void downLoadPic(final String url) {
		if(context == null || !DatabaseDealer.getSettings(context).isShowPic()) {
			return;
		}
		downloadingKey.add(url);
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					FileDealer.downloadBitmap(url, screenWidth);
					downloadingKey.remove(url);
					context.refreshPic();
				} catch (IOException e) {
				}
			}
		});
	}

	public final void clearMemoryPicCache() {
		for (Object obj : picMemoryCache.keySet()) {
			Drawable pic = picMemoryCache.get(obj);
			if(pic instanceof BitmapDrawable) {
				((BitmapDrawable)pic).getBitmap().recycle();
			}
		}
		picMemoryCache.clear();
		downloadingKey.clear();
	}

	public final void stopDownloadThread() {
		clearMemoryPicCache();
		executorService.shutdownNow();
		executorService = Executors.newFixedThreadPool(2);
		context = null;
	}
}
