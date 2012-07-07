package org.dian.kiyan.Constants;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns{
	/* 授权 */
	public static final String AUTHORIZATION = "authorization";
	/**不发布*/
	public static final int STATE_NOT_PUBLISH = 0;
	/**发布*/
	public static final int STATE_PUBLISH = 1;
	/* 微博授权信息 */
	public static final String WEIBO_APP_KEY = "318866733";
	public static final String WEIBO_APP_SECRET = "376309acc370d6b83bd5bc54d7b6317a";
	public static final String WEIBO_REDIRECT_URL = "http://www.sina.com";
	public static final String WEIBO_AUTHORIZE = "weiboauthorize";
	/* 人人授权信息 */
	public static final String RENREN_API_KEY = "a27cd23f525c4d88b9f083df79be4cb9";
	public static final String RENREN_SECRET_KEY = "72fcfde91e434e5388717ffc3c65792e";
	public static final String REREN_APP_ID = "181484";
	public static final String RENREN_AUTHORIZE = "renrenauthorize";
	/* 腾讯授权信息 */
	public static final String QQ_APP_ID = "100627445";
	public static final String QQ_APP_KEY = "59a712d9b64e653f705f528a6ca2d6ee";
	public static final String QQ_APP_NAME = "app100627445";
	public static final String QQ_AUTHORIZE = "qqauthorize";
	/* 自定义的request code */
	/**新增习惯*/
	public static final int REQUEST_ADD_HABIT = 0;
	/**认证微博*/
	public static final int REQUEST_WEIBO_AUTHORIZE = 1;
	/**认证人人*/
	public static final int REQUEST_RENREN_AUTHORIZE = 2;
	/**认证qq*/
	public static final int REQUEST_QQ_AUTHORIZE = 3;
	/**习惯详情*/
	public static final int REQUEST_HABIT_DETAILS = 4;
	/**习惯记录*/
	public static final int REQUEST_RECORD = 5;
	/**分享中心*/
	public static final int REQUEST_SHARE = 6;
	// custom result code
	/**notification的状态*/
	public static final int NOTIFICATION_SHOW = 1;
	public static final int NOTIFICATION_DISAPPEAR = 0;
	public static final String NOTIFICATION = "notificaiton";
	
	/* Relative habit constants database */
	/**数据库的名字*/
	public static final String HABITS = "habits";
	/**数据库的版本*/
	public static final int HABITS_VERSION = 1;
	/**数据表名：所有习惯*/
	public static final String ALL_HABITS = "allhabits";
	/**数据表名：所有习惯的日志*/
	public static final String ALL_HABITS_LOG = "allhabitslog";
	/**数据表名：单个习惯的统计数据*/
	public static final String SINGLE_HABIT_STATISTICS = "singlehabitstatistics";
	/**数据表项：习惯的Title*/
	public static final String TITLE = "title";
	/**数据表项：习惯的标签*/
	public static final String TAG = "tag";
	/**数据表项：习惯的周期*/
	public static final String DAYS = "days";
	/**默认周期*/
	public static final int DEFAULT_TIME = 21;
	/**数据表项：习惯当前周期内的状态*/
	public static final String CURRENT_STATE = "currentstate";
	/**数据表项：单个习惯成功的次数*/
	public static final String SUCCESSFUL_TIMES = "successful";
	/**数据表项：单个习惯失败的次数*/
	public static final String FAIL_TIMES = "fail";
	/**数据表项：单个习惯无记录的次数*/
	public static final String WITHOUT_RECORD_TIMES = "withoutrecordtimes";
	/**数据表项：习惯记录的内容*/
	public static final String WORDS = "words";
	/**数据表项：动作发生的时间戳*/
	public static final String TIMESTAMP = "timestamp";
	/**数据表项：单个习惯背景的uri*/
	public static final String BACKGROUND = "background";
	/**数据表项：习惯的最新状态*/
	public static final String LATEST_STATE = "lateststate";
	/**数据表项：习惯周期内的状态*/
	public static final String IN_T_STATE = "intstate";
	/**数据表项：是否分享到人人*/
	public static final String SHARE_TO_RENREN = "sharetorenren";
	/**数据表项：是否分享到微博*/
	public static final String SHARE_TO_WEIBO = "sharetoweibo";
	/**数据表项：坚持天数*/
	public static final String INSIST_DAYS = "insistdays";
	/**数据表项：习惯的全局唯一ID*/
	public static final String UUID = "uuid";
	
	/* 习惯的各种状态 */
	/**习惯状态：未记录*/
	public static final int HABIT_STATE_NOT_RECORDED = 0;
	/**习惯状态：成功*/
	public static final int HABIT_STATE_SUCCESSFUL = 1;
	/**习惯状态：失败*/
	public static final int HABIT_STATE_FAIL = 2;
	/**习惯状态：创建*/
	public static final int HABIT_STATE_CREATE = 3;
	/**习惯状态：记录*/
	public static final int HABIT_STATE_RECORD = 4;
	
	/* 习惯的各种标签 */
	/**习惯标签：没有*/
	public static final int NO_TAG = 0;
	/**习惯标签：红色*/
	public static final int RED_TAG = 1;
	/**习惯标签：橙色*/
	public static final int ORANGE_TAG = 2;
	/**习惯标签：黄色*/
	public static final int YELLOW_TAG = 3;
	/**习惯标签：绿色*/
	public static final int GREEN_TAG = 4;
	/**习惯标签：深绿色*/
	public static final int DARK_GREEN_TAG = 5;
	/**习惯标签：浅紫色*/
	public static final int LIGHT_PURPLE_TAG = 6;
	/**习惯标签：紫色*/
	public static final int PURPLE_TAG = 7;
	
	/* 操作数据库的不同消息 */
	/**插入数据库的时机：创建习惯时*/
	public static final int WHEN_CREATE = 0;
	/**插入数据库的时机：记录习惯时*/
	public static final int WHEN_RECORD = 1;
	/**更新数据库的时机：新浪微博*/
	public static final int UPDATE_SINA_STATE = 2;
	/**更新数据库的时机：人人*/
	public static final int UPDATE_RENREN_STATE = 3;
	
	/*sharedPreference*/
	/**sharedPreference的名字*/
	public static final String EXIT_INFO = "exitinfo";
	public static final String ADD_HABIT_PREF = "addhabitpref";
	/**退出时间*/
	public static final String EXIT_DATE = "exitdate";
	
	/*INTENT中Extra的name*/
	/**日期差*/
	public static final String DATE_MINUS = "dateminus";
	
	/*输入合法性限制*/
	/**TITLE的字数限制*/
	public static final int TITLE_MAX_LENGTH = 16;
	/**WORDS的字数限制*/
	public static final int WORDS_MAX_LENGTH = 30;
	
	/*Handler的选项*/
	/**刷新人人图标*/
	public static final int HANDLER_MSG_REFRESH_RENREN = 0;
	/**刷新微博图标*/
	public static final int HANDLER_MSG_REFRESH_WEIBO = 1;
}