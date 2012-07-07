package org.dian.kiyan.ReceiveBroadcast;

import static org.dian.kiyan.Constants.Constants.*;

import org.dian.kiyan.R;
import org.dian.kiyan.Activities.MainActivity;
import org.dian.kiyan.Activities.HabitDetailsActivity;
import org.dian.kiyan.Databases.HabitDatabase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	private int messagenotificationid = 1; 
	
	public static int NOTIFICATION_STATE = NOTIFICATION_DISAPPEAR;
	
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("org.dian.kiyan"))
			System.out.println("this is the intent");
		
		String Clock_Flag_on = intent.getStringExtra(UUID);
		String TAG = "ScrollLayout";
		//Intent i = intent.getIntent(UUID);
		Log.e(TAG, Clock_Flag_on); 	 
		//Toast.makeText(context, Clock_Flag_on, Toast.LENGTH_LONG).show();
		//if(AGoodHabitPerMonthActivity.Clock_Settings == true){
			 String ns = Context.NOTIFICATION_SERVICE;
	         NotificationManager mNotificationManager =
	                 (NotificationManager) context.getSystemService(ns);
	         CharSequence tickerText = "别忘了，快去记录习惯";
	         Notification notification = new Notification(R.drawable.icon, tickerText, messagenotificationid);
	         //notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
	         notification.flags|=Notification.FLAG_AUTO_CANCEL; //自动终止

	         notification.vibrate = new long[] {100,250,100,500};
	         notification.defaults = Notification.DEFAULT_SOUND; // 使用默认的声音   
	         notification.defaults = Notification.DEFAULT_VIBRATE; // 使用默认的震动   
	         notification.defaults = Notification.DEFAULT_LIGHTS; // 使用默认的Light   
	         notification.defaults = Notification.DEFAULT_ALL; // 所有的都使用默认值   
	         notification.defaults = Notification.DEFAULT_SOUND;   
	            
	         //设置音乐   
	         notification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");   
	         notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");   
	         CharSequence contentTitle = "别忘了，快去记录习惯";
	         CharSequence contentText = "快去记录习惯吧";
	         //来判断程序进入哪一个Activity
	         Intent notificationIntent = new Intent();
	         notificationIntent.putExtra(UUID, Clock_Flag_on);
	         notificationIntent.putExtra(NOTIFICATION, 0);
	         if(NOTIFICATION_STATE == NOTIFICATION_DISAPPEAR){
	        	 notificationIntent.setClass(context, HabitDetailsActivity.class);
	        	 NOTIFICATION_STATE = NOTIFICATION_SHOW;
	        	 HabitDatabase database = new HabitDatabase(context);
	        	 ContentValues values = database.queryStatistics(Clock_Flag_on);
	        	 String s = values.getAsString(TITLE);
	        	 contentTitle = "您的" + s + "习惯还没记录";
	         }else{
	        	 notificationIntent.setClass(context, MainActivity.class);
	         }
	         PendingIntent contentIntent =
	        		 PendingIntent.getActivity(context, 0, notificationIntent, 0);
	         notification.setLatestEventInfo(
	                         context, contentTitle, contentText, contentIntent);
	         mNotificationManager.notify(messagenotificationid, notification);
	         messagenotificationid++;
		//}
	}
}
