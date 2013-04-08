package com.jerry.utils;

import java.io.IOException;

import android.app.Service;
import android.content.Context;

import com.jerry.model.LoginInfo;

public class ShutDown {

	public static Service service;

	public static void shutDownActivity(final Context context) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				FileDealer.clearCache();
				if(service != null) {
					service.stopSelf();
				}
				try {
					LoginInfo.getInstance(context).logOut(context);
				} catch (IOException e) {
					System.exit(0);
				}
			}
		});
		thread.start();
	}  
}
