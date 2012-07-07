package org.dian.kiyan.Databases;

import static org.dian.kiyan.Constants.Constants.*;

import java.security.PublicKey;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dian.kiyan.R;
import org.dian.kiyan.Databases.Habit;
import org.dian.kiyan.Utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.AvoidXfermode.Mode;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

/**
 * 习惯的数据库
 * 请在每次数据库操作后加上记住操作的时间日期
 * */
public class HabitDatabase extends SQLiteOpenHelper {
	
	/**上下文环境*/
	private Context mContext;
	
	public HabitDatabase(Context context) {
		super(context, HABITS, null, HABITS_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + ALL_HABITS + " (" 
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TITLE + " TEXT NOT NULL, "
				+ UUID + " TEXT NOT NULL)"; 
		System.out.println(sql);
		db.execSQL(sql);
		String sql1 = "CREATE TABLE " + ALL_HABITS_LOG + " (" 
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TITLE + " TEXT NOT NULL, "
				+ TAG + " INTEGER, "
				+ DAYS + " INTEGER, "
				+ CURRENT_STATE + " INTEGER NOT NULL, "
				+ WORDS + " TEXT, "
				+ TIMESTAMP + " DATETIME DEFAULT (datetime('now', 'localtime')), "
				+ UUID + " TEXT NOT NULL)";
		System.out.println(sql1);
		db.execSQL(sql1);
		String sql2 = "CREATE TABLE " + SINGLE_HABIT_STATISTICS + " (" 
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TITLE + " TEXT NOT NULL, "
				+ LATEST_STATE + " INTEGER, "
				+ IN_T_STATE + " INTEGER, "
				+ SUCCESSFUL_TIMES + " INTEGER, "
				+ FAIL_TIMES + " INTEGER, "
				+ WITHOUT_RECORD_TIMES + " INTEGER, "
				+ BACKGROUND + " TEXT, "
				+ SHARE_TO_RENREN + " BIT, "
				+ SHARE_TO_WEIBO + " BIT, "
				+ TIMESTAMP + " DATETIME DEFAULT (datetime('now', 'localtime')), "
				+ INSIST_DAYS + " INTEGER, "
				+ TAG + " INTEGER NOT NULL, "
				+ UUID + " TEXT NOT NULL)";
		System.out.println(sql2);
		db.execSQL(sql2);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 具体研究后再更新数据库的版本
		db.execSQL("DROP TABLE IF EXISTS " + ALL_HABITS);
		db.execSQL("DROP TABLE IF EXISTS " + ALL_HABITS_LOG);
		db.execSQL("DROP TABLE IF EXISTS " + SINGLE_HABIT_STATISTICS);
	}
	
	/* 封装后的数据库CRUD操作 */
	/**
	 * 插入数据方法
	 * @param Habit h 
	 * @param Message when
	 * @param Context context
	 * @return boolean insertOutcome
	 * */
	public boolean insert(Habit h, Message when, Context context) {
		boolean insertOutcome = false;
		SQLiteDatabase db = this.getWritableDatabase();
		switch (when.what) {
		case WHEN_CREATE:
			// 准备数据
			ContentValues values1 = new ContentValues();
			ContentValues values2 = new ContentValues();
			values1.put(TITLE, h.title);
			values1.put(UUID, h.uuid);
			if(db.insert(ALL_HABITS, null, values1) == -1) {
				insertOutcome = false;
				break;
			}
			values1.put(TAG, h.tag);
			values1.put(DAYS, h.days);
			values1.put(CURRENT_STATE, HABIT_STATE_CREATE);
			if(h.wordsBoolean) {
				values1.put(WORDS, h.words);
			} else {
				String createWords = mContext.getResources()
						.getString(R.string.createWords);
				values1.put(WORDS, createWords);
			} 
			if(db.insert(ALL_HABITS_LOG, null, values1) == -1) {
				insertOutcome = false;
				break;
			}
			values2.put(TAG, h.tag);
			values2.put(TITLE, h.title);
			values2.put(LATEST_STATE, h.currentState);
			values2.put(IN_T_STATE, HABIT_STATE_NOT_RECORDED);
			values2.put(SUCCESSFUL_TIMES, h.successfulDays);
			values2.put(FAIL_TIMES, h.failDays);
			values2.put(WITHOUT_RECORD_TIMES, h.withoutrecordDays);
			values2.put(BACKGROUND, h.background);
			values2.put(SHARE_TO_RENREN, h.shareToRenren);
			values2.put(SHARE_TO_WEIBO, h.shareToWeibo);
			values2.put(UUID, h.uuid);
//			values2.put(CLOCK, h.clock);
			if(db.insert(SINGLE_HABIT_STATISTICS, null, values2) == -1) {
				Log.d("database", "failed");
				insertOutcome = false;
				break;
			}
			insertOutcome = true;
			break;
		case WHEN_RECORD:
			ContentValues values3 = new ContentValues();
			values3.put(TITLE, h.title);
			values3.put(TAG, h.tag);
			values3.put(DAYS, h.days);
			values3.put(CURRENT_STATE, h.currentState);
			values3.put(WORDS, h.words);
			values3.put(UUID, h.uuid);
			if(db.insert(ALL_HABITS_LOG, null, values3) == -1) {
				insertOutcome = false;
				break;
			}
			ContentValues values4 = new ContentValues();
			values4.put(LATEST_STATE, h.latestState);
			db.beginTransaction();
			try {
				int temp = db.update(SINGLE_HABIT_STATISTICS, values4, 
						"uuid=?", new String[]{h.uuid});
				updateLastOperateTime(h.uuid); // 刷新最后操作的时间
				db.setTransactionSuccessful();
			} catch (Exception e) {
			} finally {
				db.endTransaction();
			}
			if(h.inTStateBoolean && h.successfulDaysBoolean) {
				values4.put(IN_T_STATE, h.inTState);
				values4.put(SUCCESSFUL_TIMES, h.successfulDays);
				db.beginTransaction();
				try {
					db.update(SINGLE_HABIT_STATISTICS, values4, 
							"uuid=?", new String[]{h.uuid});
					updateLastOperateTime(h.uuid); // 刷新最后操作的时间
					db.setTransactionSuccessful();
				} catch (Exception e) {
				} finally {
					db.endTransaction();
				}
			}
			if(h.inTStateBoolean && h.failDaysBoolean) {
				values4.put(IN_T_STATE, h.inTState);
				values4.put(FAIL_TIMES, h.failDays);
				db.beginTransaction();
				try {
					db.update(SINGLE_HABIT_STATISTICS, values4, 
							"uuid=?", new String[]{h.uuid});
					updateLastOperateTime(h.uuid); // 刷新最后操作的时间
					db.setTransactionSuccessful();
				} catch (Exception e) {
				} finally {
					db.endTransaction();
				}
			}
			insertOutcome = true;
			break;
		default:
			break;
		}
		db.close();
		return insertOutcome;
	}
	
	/**
	 * 删除数据方法
	 * */
	public void delete(String uuid) {
		// TODO 写放弃习惯方法的时候再说吧
		SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ALL_HABITS, "uuid=?", new String[]{uuid});
        db.delete(ALL_HABITS_LOG, "uuid=?", new String[]{uuid});
        db.delete(SINGLE_HABIT_STATISTICS, "uuid=?", new String[]{uuid});
        db.close();
	}
	
	public boolean update(Habit h){
		boolean temp = false;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(TAG, h.tag);
		values.put(BACKGROUND, h.background);
		if(db.update(SINGLE_HABIT_STATISTICS, values, "uuid=?", 
				new String[]{h.uuid}) > 0) {
			updateLastOperateTime(h.uuid); // 刷新最后操作的时间
			temp = true;
		}
		db.close();
		return temp;
	}
	
	/**
	 * 根据UUID更新单个习惯的状态数据
	 * @param String title
	 * */
	public boolean update(Habit h, Message what) {
		boolean temp = false;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		switch (what.what) {
		case UPDATE_SINA_STATE:
			values.put(SHARE_TO_WEIBO, h.shareToWeibo);
			if(db.update(SINGLE_HABIT_STATISTICS, values, "uuid=?", 
					new String[]{h.uuid}) > 0) {
				updateLastOperateTime(h.uuid); // 刷新最后操作的时间
				temp = true;
			}
			break;
		case UPDATE_RENREN_STATE:
			values.put(SHARE_TO_RENREN, h.shareToRenren);
			if(db.update(SINGLE_HABIT_STATISTICS, values, "uuid=?", 
					new String[]{h.uuid}) > 0) {
				updateLastOperateTime(h.uuid); // 刷新最后操作的时间
				temp = true;
			}
			break;
		default:
			break;
		}
		db.close();
		return temp;
	}
	
	/**
	 * 根据UUID查询全部的习惯日志表
	 * @param String uuid
	 * @return Bundle tempValue
	 * */
	public Bundle query(String uuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(ALL_HABITS_LOG, null, 
        		"uuid=?", new String[]{uuid}, null, null, 
        		"_id DESC");
        Bundle tempValue = new Bundle();
        int i = 0;
        if(c != null) {
        	//c.moveToFirst();
        	while(c.moveToNext()){
        		Bundle values = new Bundle();
        		values.putInt(_ID, c.getInt(0));
        		values.putString(TITLE, c.getString(1));
        		values.putInt(TAG, c.getInt(2));
        		values.putInt(DAYS, c.getInt(3));
        		values.putInt(CURRENT_STATE, c.getInt(4));
        		values.putString(WORDS, c.getString(5));
        		values.putString(TIMESTAMP, c.getString(6));
        		values.putString(UUID, uuid);
        		final String temp = "temp" + i;
        		tempValue.putBundle(temp, values);
        		i++;
        	}
        	c.close();
		} else {
			System.out.println("no such habit log found");
		}
        db.close();
        return tempValue;
	}
	
	/**
	 * 查询全部的习惯日志表
	 * 返回所有数据
	 * @return Bundle tempValue
	 * */
	public Bundle queryAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(ALL_HABITS_LOG, null, null, null, 
        		null, null, "_id DESC");
        Bundle tempValue = new Bundle();
        int i = 0;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
        	Bundle values = new Bundle();
    		values.putInt(_ID, c.getInt(0));
    		values.putString(TITLE, c.getString(1));
    		values.putInt(TAG, c.getInt(2));
    		values.putInt(DAYS, c.getInt(3));
    		values.putInt(CURRENT_STATE, c.getInt(4));
    		values.putString(WORDS, c.getString(5));
    		values.putString(TIMESTAMP, c.getString(6));
    		values.putString(UUID, c.getString(7));
    		final String temp = "temp" + i;
    		tempValue.putBundle(temp, values);
    		i++;
        }
        c.close();
        db.close();
        return tempValue;
	}
	
	/**
	 * 用UUID查询全部习惯列表
	 * 获得某个习惯的最新数据
	 * @param String title
	 * @return ContentValues values
	 * */
	public ContentValues queryLatestWithUUID(String uuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor c = db.query(ALL_HABITS_LOG, null, 
        		"uuid=?", new String[]{uuid}, null, null, 
        		"_id DESC");
        ContentValues values = new ContentValues();
        if(c != null) {
        	c.moveToFirst();
        	values.put(_ID, c.getInt(0));
            values.put(TITLE, c.getString(1));
            values.put(TAG, c.getInt(2));
            values.put(DAYS, c.getInt(3));
            values.put(CURRENT_STATE, c.getInt(4));
            values.put(WORDS, c.getString(5));
            values.put(TIMESTAMP, c.getString(6));
            values.put(UUID, uuid);
        	c.close();
		} else {
			System.out.println("no such habit log found");
		}
        db.close();
        return values;
	}
	
	/**
	 * 查询全部习惯列表
	 * 获得最近添加的数据
	 * */
	public HashMap<String, Object> queryLatest() {
		return null;
	}
	
	/**
	 * 根据uuid查询单个习惯的状态
	 * @param String title
	 * @return ContentValues values
	 * */
	public ContentValues queryStatistics(String uuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(SINGLE_HABIT_STATISTICS, null, 
        		"uuid=?", new String[]{uuid}, null, null, null);
        ContentValues values = new ContentValues();
        int i = 0;
        if(c != null) {
        	c.moveToFirst();
        	while(c.isLast()) {
	        	values.put(_ID, c.getInt(0));
	            values.put(TITLE, c.getString(1));
	            values.put(LATEST_STATE, c.getInt(2));
	            values.put(IN_T_STATE, c.getInt(3));
	            values.put(SUCCESSFUL_TIMES, c.getInt(4));
	            values.put(FAIL_TIMES, c.getInt(5));
	            values.put(WITHOUT_RECORD_TIMES, c.getInt(6));
	            values.put(BACKGROUND, c.getString(7));
	            values.put(SHARE_TO_RENREN, c.getInt(8));
	            values.put(SHARE_TO_WEIBO, c.getInt(9));
	            values.put(TIMESTAMP, c.getString(10));
	            values.put(INSIST_DAYS, c.getInt(11));
	            values.put(TAG, c.getInt(12));
	            values.put(UUID, uuid);
//	            values.put(CLOCK, c.getString(13));
	            i++;
	            c.moveToNext();
        	}
        	c.close();
		} else {
			System.out.println("no such habit log found");
		}
        db.close();
        return values;
	}
	
	/**
	 * 查询habit的总个数
	 * @return int
	 * */
	public int queryTotalCount() {
		SQLiteDatabase db = this.getWritableDatabase();
        // 查询记录条数
        Cursor c = db.query(ALL_HABITS, null, null, null, null, null, null);
        c.moveToFirst();
        int count = c.getCount();
        c.close();
        db.close();
        return count;
	}
	
	/**
	 * 得到今天的数据报表
	 * @return int[]{成功次数，失败次数，未记录次数，总成功率}
	 * */
	public float[] queryStatistics() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(SINGLE_HABIT_STATISTICS, 
				null, null, null, null, null, null);
		int successCount = 0; // 今天的成功数量
		int failCount = 0; // 今天的失败数量
		int waitCount = 0; // 今天的未记录数量
		
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()) {
			switch (c.getInt(3)) {
			case HABIT_STATE_SUCCESSFUL:
				successCount++;
				break;
			case HABIT_STATE_FAIL:
				failCount++;
				break;
			case HABIT_STATE_NOT_RECORDED:
				waitCount++;
				break;
			default:
				break;
			}
		}
		
		/*计算总成功率*/
		float successRate = 0f;
		int successTimes = 0;
		int failTimes = 0;
		int withourRecordTimes = 0;
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			successTimes += c.getInt(4);
			failTimes += c.getInt(5);
			withourRecordTimes += c.getInt(6);
		}
		if(successTimes + failTimes + withourRecordTimes == 0 ) {
			successRate = 0;
		} else {
			successRate = successTimes / (float)(successTimes + failTimes + withourRecordTimes);
		}
		/*返回所有结果*/
		float[] count = new float[]{successCount, failCount, waitCount, successRate};
		/*关闭cursor和数据库*/
		c.close();
		db.close();
		return count;
	}
	
	/**
	 * 读取当前日期
	 * 算出每个习惯日期差
	 * 根据得到的日期差里对数据库进行操作
	 * 操作完后需要加入更新的一条状态
	 * */
	public void updateFromService() {
		System.out.println("datebase---run update from service");
		// 得到数据库全部习惯日志中当前习惯最后的操作日期
		SQLiteDatabase db = this.getWritableDatabase();
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Cursor habitIDCursor = db.query(ALL_HABITS, 
				new String[]{UUID}, null, null, null, null, null);
		System.out.println(habitIDCursor.getCount() + "");
		// 得到当前日期
		Calendar cNow = Calendar.getInstance();
		for(habitIDCursor.moveToFirst(); !habitIDCursor
				.isAfterLast(); habitIDCursor.moveToNext()) {
			String habitID = habitIDCursor.getString(0);
			Cursor lastModifiedTimestamp = db.query(SINGLE_HABIT_STATISTICS, 
					new String[]{TIMESTAMP}, "uuid=?", 
					new String[]{habitID}, null, null, "_id desc");
			System.out.println(lastModifiedTimestamp.getCount() + "");
			lastModifiedTimestamp.moveToFirst();
			String timestamp = lastModifiedTimestamp.getString(0);
			System.out.println(timestamp); // 得到了该习惯最后一条记录的时间
			Date date;
			int dateMinus = 0;
			try {
				date = df.parse(timestamp);
				Calendar cBefore = Calendar.getInstance();
				cBefore.setTime(date);
				// 算出日期差
				long dateMinusLong = Utils.differ(cBefore, cNow);
				Long tempLong = new Long(dateMinusLong);
				dateMinus = tempLong.intValue();
				System.out.println("dateMinus---" + dateMinus);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// 根据日期差进行对应的操作
			if(dateMinus != 0) {
				ContentValues values1 = new ContentValues();
				// 所有习惯的LATEST_STATE改为NOT_RECORDED
				values1.put(LATEST_STATE, HABIT_STATE_NOT_RECORDED); 
				Timestamp timeNow = new Timestamp(System.currentTimeMillis());
				String now = timeNow.toString();
				// 所有受影响的timestamp改成现在
				values1.put(TIMESTAMP, now); 
				db.update(SINGLE_HABIT_STATISTICS, values1, "uuid=?", 
						new String[]{habitID});
				/* 
				 * 获取列表中的TITLE,IN_T_STATE,INSIST_DAYS,
				 * WITHOUT_RECORD_TIMES,SUCCESSFUL_TIMES,FAIL_TIMES
				 * */
				Cursor cStatistics = db.query(SINGLE_HABIT_STATISTICS, 
						new String[]{TITLE, IN_T_STATE, INSIST_DAYS, 
						WITHOUT_RECORD_TIMES, SUCCESSFUL_TIMES, FAIL_TIMES}, 
						"uuid=?", new String[]{habitID}, null, null, 
						null);
				cStatistics.moveToFirst(); 
				/*对应习惯的INSIST_DAYS要增加n天*/
				int insistDays = cStatistics.getInt(2);
				int today = insistDays + dateMinus;
				ContentValues values2 = new ContentValues();
				values2.put(INSIST_DAYS, today);
				db.update(SINGLE_HABIT_STATISTICS, values2, 
						"uuid=?", new String[]{habitID});
				/*获取习惯的周期*/
				Cursor cT = db.query(ALL_HABITS_LOG, new String[]{DAYS}, 
						"uuid=?", new String[]{habitID}, null, null, null);
				cT.moveToFirst();
				int t = cT.getInt(0);
				/*用周期和日期差判断各种情况*/
				int situation = dateMinus / t - 1 ;
				int withoutRecordTimes = cStatistics.getInt(3);
				int successfulTimes = cStatistics.getInt(4);
				int failTimes = cStatistics.getInt(5);
				if(situation < 0) {
					/*周期内好像可以不做操作*/
				} else if (situation == 0) {
					/*下一个周期*/
					System.out.println("situation---" + situation);
					ContentValues values4 = new ContentValues();
					int lastTState = cStatistics.getInt(1);
					switch (lastTState) {
					case HABIT_STATE_NOT_RECORDED:
						values4.put(WITHOUT_RECORD_TIMES, 
								withoutRecordTimes + 1);
						break;
					default:
						break;
					}
					values4.put(IN_T_STATE, HABIT_STATE_NOT_RECORDED);
					db.update(SINGLE_HABIT_STATISTICS, values4, 
							"uuid=?", new String[]{habitID});
				} else if (situation > 0) {
					System.out.println("situation---" + situation);
					/*下N个周期*/
					ContentValues values5 = new ContentValues();
					int lastTState = cStatistics.getInt(1);
					switch (lastTState) {
					case HABIT_STATE_NOT_RECORDED:
						values5.put(WITHOUT_RECORD_TIMES, 
								withoutRecordTimes + situation);
						break;
					case HABIT_STATE_SUCCESSFUL:
						values5.put(SUCCESSFUL_TIMES, 
								successfulTimes + 1);
						break;
					case HABIT_STATE_FAIL:
						values5.put(FAIL_TIMES, failTimes + 1);
						break;
					default:
						break;
					}
					values5.put(IN_T_STATE, 
							HABIT_STATE_NOT_RECORDED);
					db.update(SINGLE_HABIT_STATISTICS, values5, 
							"uuid=?", new String[]{habitID});
				}
				cStatistics.close(); // 关闭游标
			}
			SharedPreferences settings = mContext.getSharedPreferences(AUTHORIZATION, Context.MODE_WORLD_WRITEABLE);
			Editor editor = settings.edit();
			editor.putBoolean(WEIBO_AUTHORIZE, false);
			editor.putBoolean(RENREN_AUTHORIZE, false);
			editor.commit();
		}
		habitIDCursor.close();
		db.close();
		System.out.print("db: close()");
	}
	
	/** 
	 * 每次写数据库后更新最后写操作的时间 
	 * @param String uuid
	 * @return boolean outcome
	 * */
	public boolean updateLastOperateTime(String uuid) {
		boolean outcome = false;
		// 产生timestamp
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String now = timestamp.toString();
		// 根据title更新对应的习惯
		SQLiteDatabase temp = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(TIMESTAMP, now);
		if(temp.update(SINGLE_HABIT_STATISTICS, values, "uuid=?", 
				new String[]{uuid}) > 0)
			outcome = true;
		//temp.close();
		return outcome;
	}
}	