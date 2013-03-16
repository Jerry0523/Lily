package com.jerry.lily;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.jerry.utils.DocParser;
import com.jerry.utils.ShutDown;

public class RefreshService extends Service{

	private Timer timer;  
	private TimerTask task;
	

	@Override
	public void onCreate() {
		super.onCreate();
		ShutDown.service = this;
		timer = new Timer();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		task.cancel();
		timer = null;
		task = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (task != null){
			task.cancel(); //将原任务从队列中移除
		}
		task = new TimerTask(){  
			public void run() {
				try {
					DocParser.keepConnected(RefreshService.this);
				} catch (IOException e) {
					stopSelf();
				}
			}  
		};
		timer.schedule(task, 600000,600000);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
