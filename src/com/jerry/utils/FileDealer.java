package com.jerry.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class FileDealer {

	public static final void createDir() {
		String root = getHomeDirPath(); 
		String pic = getCacheDir();
		String photo = getPhotoDirPath();
		String update = getUpdateDirPath();

		File fileRoot = new File(root);
		File filePic = new File(pic);
		File filePhoto = new File(photo);
		File fileUpdate = new File(update);

		if (!fileRoot.exists()) {
			fileRoot.mkdir();
		}
		if (!filePic.exists()) {
			filePic.mkdir();
		}
		if (!filePhoto.exists()) {
			filePhoto.mkdir();
		}
		if (!fileUpdate.exists()) {
			fileUpdate.mkdir();
		}
	}

	private static String getHomeDirPath() {
		return Environment.getExternalStorageDirectory().getPath() + "/Lily";
	}

	public static void clearCache() {
		File filePic = new File(getCacheDir());
		if(!filePic.exists() && !filePic.isDirectory()) {
			return;
		}
		String[] tempList = filePic.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			temp = new File(getCacheDir() + "/" + tempList[i]);
			if (temp.isFile()) {
				temp.delete();
			}
		}
	}

	public static String getUpdateDirPath() {
		return getHomeDirPath() + "/update";
	}

	public static String getPhotoDirPath() {
		return getHomeDirPath() + "/photo";
	}

	public static String getCacheDir() {
		return getHomeDirPath() + "/.tmp";
	}

	public static final void downloadBitmap(String strUrl, int size) throws IOException{
		URL imageUrl = new URL(strUrl);
		HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
		InputStream is = conn.getInputStream();
		
		BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        BitmapFactory.decodeStream(is, null, options);  
        is.close(); 
        
        Bitmap bitmap = null;
        int i = 0;
        while (true) {  
            if ((options.outWidth >> i <= size)) {  
                is =  ((HttpURLConnection) imageUrl.openConnection()).getInputStream();
                options.inSampleSize = (int) Math.pow(2.0D, i);  
                options.inJustDecodeBounds = false;  
                bitmap = BitmapFactory.decodeStream(is, null, options);  
                break;  
            }  
            i++;  
        }  
		
		if(bitmap == null) {
			return;
		}
		
		File file = new File(FileDealer.getCacheDir() + strUrl.substring(strUrl.lastIndexOf("/")));
		FileOutputStream fOut = new FileOutputStream(file);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

		is.close();
		conn.disconnect();
		fOut.flush();
		fOut.close();
		bitmap.recycle();
	}

}