package org.dian.kiyan.ReceiveBroadcast;

import java.util.Calendar;

import org.dian.kiyan.Databases.DataRefreshSvc;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

/**
 * 绑定了重启服务的静态BroadcastReceiver
 * 
 * */
public class TimeListenerSvc extends Service {
	
	/**监听系统时间的broadcastReceiver*/
	private BroadcastReceiver mBroadcastreceiver;
	
	/**
	 * 第一次start或者bind的时候会调用
	 * */
	@Override
	public void onCreate() {
		mBroadcastreceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO 监听系统时间是否到12点
				// TODO 读取数据库数据，看是否有日期变化
				// TODO 到12点时start刷新数据服务
				/*程序活动时监听0点0分*/
				final Calendar c = Calendar.getInstance();
				if(c.get(Calendar.HOUR_OF_DAY) == 0 && 
				 		c.get(Calendar.MINUTE) == 0) {
					Intent i = new Intent();
					i.setClass(TimeListenerSvc.this, DataRefreshSvc.class);
					context.startService(i);
				} else if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
					System.out.println("日期改变了");
					// TODO 系统日期改变情况下的相应操作
				} else {
					System.out.println("不是12点");
				}
			}
		};
		Intent i = new Intent();
		i.setClass(TimeListenerSvc.this, DataRefreshSvc.class);
		this.startService(i);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		registerReceiver(mBroadcastreceiver, filter);
		super.onCreate();
	}
	
	/**
	 * 每次start的时候调用
	 * */
	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 每次绑定的时候调用
	 * */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/**
	 * stop或者unbind之后
	 * 销毁服务的时候调用
	 * */
	@Override
	public void onDestroy() {
		unregisterReceiver(mBroadcastreceiver);
		super.onDestroy();
	}
	
	/**
	 * unbind时调用
	 * */
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
}