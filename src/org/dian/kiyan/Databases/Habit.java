package org.dian.kiyan.Databases;

import static org.dian.kiyan.Constants.Constants.*;
import android.content.ContentValues;

/**
 * 用现有的信息生成Habit对象方便进行的信息传递
 * */
public class Habit {
	
	/**习惯的title*/
	public String title = null;
	/**记录习惯的话*/
	public String words = null;
	/**习惯的色彩标签*/
	public int tag = -1;
	/**习惯的周期*/
	public int days = 1;
	/**习惯当前的状态*/
	public int currentState = HABIT_STATE_NOT_RECORDED;
	/**习惯成功的天数*/
	public int successfulDays = 0;
	/**习惯失败的天数*/
	public int failDays = 0;
	/**习惯没有记录的天数*/
	public int withoutrecordDays = 0;
	/**习惯详情页面背景的uri地址*/
	public String background = "#000000";
	/**习惯的最近状态*/
	public int latestState = HABIT_STATE_NOT_RECORDED;
	/**习惯的周期内状态*/
	public int inTState = HABIT_STATE_NOT_RECORDED;
	/**习惯是否分享到人人*/
	public int shareToRenren = STATE_NOT_PUBLISH;
	/**习惯是否分享到微博*/
	public int shareToWeibo = STATE_NOT_PUBLISH;
	/**习惯的全局唯一ID UUID*/
	public String uuid = "";
	/**初始化的标识状态*/
	public boolean titleBoolean = false;
	public boolean wordsBoolean = false;
	public boolean tagBoolean = false;
	public boolean daysBoolean = false;
	public boolean currentStateBoolean = false;
	public boolean successfulDaysBoolean = false;
	public boolean failDaysBoolean = false;
	public boolean withoutrecordDaysBoolean = false;
	public boolean backgroundBoolean = false;
	public boolean latestStateBoolean = false;
	public boolean inTStateBoolean = false;
	public boolean shareToRenrenBoolean = false;
	public boolean shareToWeiboBoolean = false;
	public boolean uuidBoolean = false;
	
	/**
	 * 习惯类的构造器
	 * 有的内容就初始化
	 * 没有的内容只有默认值
	 * */
	public Habit(ContentValues values) {
		if(values.containsKey(TITLE)) {
			title = values.getAsString(TITLE);
			titleBoolean = true;
		}
		if(values.containsKey(WORDS)) {
			words = values.getAsString(WORDS);
			wordsBoolean = true;
		}
		if(values.containsKey(TAG)) {
			tag = values.getAsInteger(TAG);
			tagBoolean = true;
		}
		if(values.containsKey(DAYS)) {
			days = values.getAsInteger(DAYS);
			daysBoolean = true;
		}
		if(values.containsKey(CURRENT_STATE)) {
			currentState = values.getAsInteger(CURRENT_STATE);
			currentStateBoolean = true;
		}
		if(values.containsKey(SUCCESSFUL_TIMES)) {
			successfulDays = values.getAsInteger(SUCCESSFUL_TIMES);
			successfulDaysBoolean = true;
		}
		if(values.containsKey(FAIL_TIMES)) {
			failDays = values.getAsInteger(FAIL_TIMES);
			failDaysBoolean = true;
		}
		if(values.containsKey(WITHOUT_RECORD_TIMES)) {
			withoutrecordDays = values.getAsInteger(WITHOUT_RECORD_TIMES);
			withoutrecordDaysBoolean = true;
		}
		if(values.containsKey(BACKGROUND)) {
			background = values.getAsString(BACKGROUND);
			backgroundBoolean = true;
		}
		if(values.containsKey(LATEST_STATE)) {
			latestState = values.getAsInteger(LATEST_STATE);
			latestStateBoolean = true;
		}
		if(values.containsKey(IN_T_STATE)) {
			inTState = values.getAsInteger(IN_T_STATE);
			inTStateBoolean = true;
		}
		if(values.containsKey(SHARE_TO_RENREN)) {
			shareToRenren = values.getAsInteger(SHARE_TO_RENREN);
			shareToRenrenBoolean = true;
		}
		if(values.containsKey(SHARE_TO_WEIBO)) {
			shareToWeibo = values.getAsInteger(SHARE_TO_WEIBO);
			shareToWeiboBoolean = true;
		}
		if(values.containsKey(UUID)) {
			uuid = values.getAsString(UUID);
			uuidBoolean = true;
		}
	}
}