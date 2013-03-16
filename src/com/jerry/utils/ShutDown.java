package com.jerry.utils;

import java.io.IOException;

import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;

import com.jerry.model.LoginInfo;
import com.jerry.widget.IOSAlertDialog;

public class ShutDown {

	public static Service service;

	public static void shutDownActivity(final Activity activity) {

		android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						FileDealer.clearCache();
						if(service != null) {
							service.stopSelf();
						}
						try {
							LoginInfo.getInstance(activity).logOut(activity);
						} catch (IOException e) {
							System.exit(0);
						}
					}
				});
				thread.start();
				activity.finish();
				System.exit(0);
			}
		};
		new IOSAlertDialog.Builder(activity).setTitle("��ʾ").setMessage("ȷ���˳�Ӧ�ó���?").setPositiveButton("ȷ��", listener).create().show();


	}  
}
