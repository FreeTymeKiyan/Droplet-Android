package org.dian.kiyan.Databases;

import static org.dian.kiyan.Constants.Constants.*;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

/**
 * 通过后台服务
 * 对数据库进行必要的刷新
 * 比如每天的状态更新
 * 有刚开启app和开启app时接收12:00的broadcast两个时机
 * */
public class DataRefreshSvc extends Service{
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// 用bind方法可以保证activity销毁后，这个service也销毁
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		/*根据日期差是否为0操作数据库*/
		/* 
		 * 数据库中受影响的数据包括
		 * CURRENT_STATE,LATEST_STATE,IN_T_STATE,
		 * WITHOUT_RECORD_TIMES,SUCCESSFUL_TIMES,FAIL_TIMES
		 * */
		super.onStart(intent, startId);
		asyncUpdate au = new asyncUpdate();
		au.execute();
	}
	
	/**
	 * 更新数据库中的数据
	 * INSIST_DAYS+dateMinus
	 * LATEST_STATE变为HABIT_STATE_NOT_RECORDED
	 * IN_T_STATE有几种情况
	 * 如果是HABIT_STATE_NOT_RECORDED
	 * 直接在WITHOUT_RECORD_TIMES上面加dateMinus / T - 1天
	 * 如果是HABIT_STATE_SUCCESSFUL或者HABIT_STATE_FAIL
	 * 根据坚持天数和习惯周期属性判断是否是新周期
	 * 是新周期则改为HABIT_STATE_NOT_RECORDED
	 * */
	private void updateDataBase() {
		HabitDatabase h = new HabitDatabase(this);
		h.updateFromService();
	}
	
	private class asyncUpdate extends AsyncTask<Integer, Integer, Boolean> {

		
		protected void onPreExecute() {
			CharSequence text = "刷新习惯状态中...";
			Toast.makeText(getApplicationContext(), text , Toast.LENGTH_SHORT);
		};
		
		@Override
		protected void onPostExecute(Boolean result) {
			System.out.println("asynctask---onPostExecute");
			super.onPostExecute(result);
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			updateDataBase();
			return true;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}