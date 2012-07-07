package org.dian.kiyan.ReceiveBroadcast;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 静态注册的BroadcastReceiver
 * 启动后台服务
 * */
public class MidnightReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent();
		i.setClass(context, TimeListenerSvc.class);
		context.startService(i);
	}
}
