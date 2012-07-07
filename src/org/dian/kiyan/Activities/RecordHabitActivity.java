package org.dian.kiyan.Activities;

import java.util.Calendar;

import org.dian.kiyan.R;
import org.dian.kiyan.Activities.RecordHabitActivity.MyHandler;
import org.dian.kiyan.Activities.ShareCenterActivity.AuthDialogListener;
import org.dian.kiyan.Apis.Share2RenrenRunnable;
import org.dian.kiyan.Apis.Share2WeiboRunnable;
import org.dian.kiyan.CustomLayout.MyImageButton;
import org.dian.kiyan.Databases.Habit;
import org.dian.kiyan.Databases.HabitDatabase;

import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

import static org.dian.kiyan.Constants.Constants.*;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecordHabitActivity extends Activity{
	
	/**表示最新的习惯状态*/
	private int latestState = HABIT_STATE_RECORD;
	/**记录一句话的控件*/
	private EditText recordWords;
	/**从上一页获取的信息：bundle*/
	private Bundle lastPage;
	/**待分享的文字预览*/
	private TextView prePublish;
	/**待分享的文字字数限制*/
	private TextView textLimit;
	/**从上一页获取的信息：习惯唯一标识*/
	private String uuid;
	/**从控件获取：要留下的一句话*/
	private String words = "";
	/**根据uuid得到的最近的习惯日志内容*/
	private ContentValues unchangeInfo;
	/**根据uuid得到的最近的习惯统计和属性信息*/
	private ContentValues statistics;
	/**记录后分享到人人的状态*/
	private int shareToRenren = STATE_NOT_PUBLISH;
	/**记录后分享到微博的状态*/
	private int shareToWeibo = STATE_NOT_PUBLISH;
	/**从sharedpreference读取的微博授权状态*/
	private boolean weiboAuth;
	/**从sharedpreference读取的人人授权状态*/
	private boolean renrenAuth;
	/**分享到人人的图片按钮*/
	private ImageButton addRenren;
	/**分享到微博的图片按钮*/
	private ImageButton addWeibo;
	/**习惯的内容*/
	private String title;
	/**Acitivity自身的引用*/
	private RecordHabitActivity mRecordHabit = this;
	/**存储授权状态*/
	private SharedPreferences settings;
	/**页面消息处理者*/
	private MyHandler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_habit);
		getDataFromLastPage();
		prepareInfo();
		initView();
	}
	
	/**用UUID从数据库得到一些不会改变的习惯属性*/
	private void prepareInfo() {
		HabitDatabase h = new HabitDatabase(this);
		unchangeInfo = h.queryLatestWithUUID(uuid);
		statistics = h.queryStatistics(uuid);
		title = statistics.getAsString(TITLE);
		settings = getSharedPreferences(AUTHORIZATION, 
				MODE_WORLD_WRITEABLE);
		weiboAuth = settings.getBoolean(WEIBO_AUTHORIZE, false);
		if(weiboAuth)
			shareToWeibo = statistics.getAsInteger(SHARE_TO_WEIBO);
		renrenAuth = settings.getBoolean(RENREN_AUTHORIZE, false);
		if(renrenAuth)
			shareToRenren = statistics.getAsInteger(SHARE_TO_RENREN);
	}

	/**从上一页获取信息*/
	private void getDataFromLastPage() {
		Intent i = getIntent();
		lastPage = i.getExtras();
		uuid = lastPage.getString(UUID);
	}

	/**
	 * 初始化界面
	 * */
	private void initView() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams
				.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 进入Activity不弹输入法
		ImageButton backToWhere = (ImageButton) findViewById(R.id.ib_backToWhere);
		backToWhere.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		ImageButton recordAdmit = (ImageButton) findViewById(R.id.ib_recordAdmit);
		recordAdmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(judgeValidity()) {
					// 操作数据库
					insertIntoDatabase();
					// TODO 后台分享
					if(checkNetworkInfo()) {
						process(words);
						if(shareToWeibo == STATE_PUBLISH) {
							Thread t = new Thread(new Share2WeiboRunnable(words, 
									RecordHabitActivity.this, mRecordHabit, handler));
							t.start();
						}
						if(shareToRenren == STATE_PUBLISH) {
							Thread t = new Thread(new Share2RenrenRunnable(words, 
									RecordHabitActivity.this, mRecordHabit, handler));
							t.start();
						}
					} else {
						Toast.makeText(RecordHabitActivity.this, R.string.noNetworkConnection, 
								Toast.LENGTH_SHORT).show();
					}
					// 返回界面通知刷新
					setResult(RESULT_OK);
					finish();
				} else {
					// 检查输入
					Toast.makeText(RecordHabitActivity.this, R.string.toast_noWords, 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		prePublish = (TextView) findViewById(R.id.tv_prePublish);
		prePublish.setText("即将分享：");
		textLimit = (TextView) findViewById(R.id.tv_textLimit);
		textLimit.setText("0" + "/30");
		recordWords = (EditText) findViewById(R.id.et_recordWords);
		// 判断是否超过字数限制
		recordWords.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
            private int selectionStart ;
            private int selectionEnd ;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				temp = s;
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				selectionStart = recordWords.getSelectionStart();
                selectionEnd = recordWords.getSelectionEnd();
                if (temp.length() > WORDS_MAX_LENGTH) {
                    Toast.makeText(RecordHabitActivity.this,
                            R.string.onTextChanged, Toast.LENGTH_SHORT)
                            .show();
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionStart;
                    recordWords.setText(s);
                    recordWords.setSelection(tempSelection);
                    textLimit.setTextColor(Color.RED);
                } else if(temp.length() == WORDS_MAX_LENGTH) {
                	textLimit.setTextColor(Color.RED);
                } else
                	textLimit.setTextColor(Color.BLACK);
                prePublish.setText("即将分享：" + process(temp.toString()));
                textLimit.setText(s.length() + "/30");
			}
		});
		/* 特殊的ImageButton：记录成功 */
		final MyImageButton recordSuccessful = new MyImageButton(this, 
				R.drawable.record_successful, R.string.record_successful);
		LinearLayout ib1 = (LinearLayout) findViewById(R.id.ll_successful);
		recordSuccessful.setPadding(20, 0, 20, 0);
		ib1.addView(recordSuccessful);
		/* 特殊的ImageButton：记录失败 */
		final MyImageButton recordFail = new MyImageButton(this, 
				R.drawable.record_fail, R.string.record_fail);
		LinearLayout ib2 = (LinearLayout) findViewById(R.id.ll_fail);
		recordFail.setPadding(20, 0, 20, 0);
		ib2.addView(recordFail);
		recordSuccessful.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(latestState == HABIT_STATE_RECORD || 
						latestState == HABIT_STATE_FAIL) {
					latestState = HABIT_STATE_SUCCESSFUL;
					recordSuccessful.setImageResource(R.drawable.record_successful_light);
					recordFail.setImageResource(R.drawable.record_fail);
					prePublish.setText("即将分享：" + process(recordWords.getText().toString()));
				} else if(latestState == HABIT_STATE_SUCCESSFUL) {
					latestState = HABIT_STATE_RECORD;
					recordSuccessful.setImageResource(R.drawable.record_successful);
					prePublish.setText("即将分享：" + process(recordWords.getText().toString()));
				}
				// TODO 图片要有点击状态，还能换成带钩的背景
				//recordSuccessful.setImageResource(resId);
			}
		});
		recordFail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(latestState == HABIT_STATE_RECORD || 
						latestState == HABIT_STATE_SUCCESSFUL) {
					latestState = HABIT_STATE_FAIL;
					recordFail.setImageResource(R.drawable.record_fail_light);
					recordSuccessful.setImageResource(R.drawable.record_successful);
					prePublish.setText("即将分享：" + process(recordWords.getText().toString()));
				} else if(latestState == HABIT_STATE_FAIL) {
					latestState = HABIT_STATE_RECORD;
					recordFail.setImageResource(R.drawable.record_fail);
					prePublish.setText("即将分享：" + process(recordWords.getText().toString()));
				}
				// TODO 图片要有点击状态，还能换成带钩的背景
				//recordSuccessful.setImageResource(resId);
			}
		});
		final MyImageButton recordOnly = new MyImageButton(this, 
				R.drawable.record_recordonly, R.string.ll_record);
		LinearLayout ib3 = (LinearLayout) findViewById(R.id.ll_record);
		ib3.addView(recordOnly);
		recordOnly.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				latestState = HABIT_STATE_RECORD;
			}
		});
		
		/* 如果该周期已经改变过失败和成功的状态，
		 * 就不显示失败和成功的按钮 */
		int temp = statistics.getAsInteger(IN_T_STATE);
		if(temp == HABIT_STATE_SUCCESSFUL || temp == HABIT_STATE_FAIL) {
			ib1.setVisibility(View.GONE);
			ib2.setVisibility(View.GONE);
			ib3.setVisibility(View.VISIBLE);
			latestState = HABIT_STATE_RECORD; // 最新状态为记录
		} else {
			ib3.setVisibility(View.GONE);
		}
		addRenren = (ImageButton) findViewById(R.id.ib_recordToRenren);
		if(shareToRenren == STATE_PUBLISH)
			addRenren.setImageResource(R.drawable.renren_icon_light);
		addRenren.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 是否授权，切换是否分享的状态
				if(renrenAuth) {
					if(shareToRenren == STATE_PUBLISH) {
						shareToRenren = STATE_NOT_PUBLISH;
						onChangeShareState(UPDATE_RENREN_STATE);
						addRenren.setImageResource(R.drawable.renren_icon_dark);
					} else {
						shareToRenren = STATE_PUBLISH;
						onChangeShareState(UPDATE_RENREN_STATE);
						addRenren.setImageResource(R.drawable.renren_icon_light);
					}
				} else {
					// TODO 跳转至授权页面
					Thread t = new Thread(new Share2RenrenRunnable(RecordHabitActivity.this, 
							mRecordHabit, handler));
					t.start();
				}
			}
		});
		addWeibo = (ImageButton) findViewById(R.id.ib_recordToWeibo);
		if(shareToWeibo == STATE_PUBLISH)
			addWeibo.setImageResource(R.drawable.weibo_icon_light);
		addWeibo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 是否授权，切换是否分享的状态
				if(weiboAuth) {
					if(shareToWeibo == STATE_PUBLISH) {
						shareToWeibo = STATE_NOT_PUBLISH;
						onChangeShareState(UPDATE_SINA_STATE);
						addWeibo.setImageResource(R.drawable.weibo_icon_dark);
					} else {
						shareToWeibo = STATE_PUBLISH;
						onChangeShareState(UPDATE_SINA_STATE);
						addWeibo.setImageResource(R.drawable.weibo_icon_light);
					}
				} else {
					// TODO 跳转至授权页面
					Thread t = new Thread(new Share2WeiboRunnable( 
							RecordHabitActivity.this, mRecordHabit, handler));
					t.start();
				}
			}
		});
		handler = new MyHandler(Looper.getMainLooper());
	}
	
	private String process(String words) {
		String temp2 = "";
		switch (latestState) {
		case HABIT_STATE_RECORD:
			temp2 = "就";
			break;
		case HABIT_STATE_SUCCESSFUL:
			temp2 = "成功坚持";
			break;
		case HABIT_STATE_FAIL:
			temp2 = "没能坚持";
			break;
		default:
			break;
		}
		String temp3 = "";
		int T = unchangeInfo.getAsInteger(DAYS);
		switch (T) {
		case 1:
			temp3 = "每天";
			break;
		case 2:
			temp3 = "每两天";
		case 7:
			temp3 = "每周";
		default:
			temp3 = "每" + T + "天";
			break;
		}
		StringBuilder sb = new StringBuilder();
//		sb.append("我").append(temp2).append(temp3).append(title)
//		.append("这个习惯").append("。");
//		sb.append("我").append(temp2).append(temp3).append(title)
//				.append("这个习惯").append("。");
		int successfulDays = statistics.getAsInteger(SUCCESSFUL_TIMES);
		int days = statistics.getAsInteger(INSIST_DAYS) + 1;
		if(latestState == HABIT_STATE_RECORD){
			sb.append(temp2).append(temp3).append(title);
			sb.append("这件事吧，我想说 ").append(words);
		} else if(latestState == HABIT_STATE_SUCCESSFUL){
			sb.append("我").append(temp2).append(temp3)
					.append(title)
					.append(successfulDays + 1)
					.append("天")
					.append("！");
			sb.append(words);
		} else {
			sb.append("我").append(temp2).append(temp3)
					.append(title).append("！");
			sb.append(words).append(" 不行！")
			.append("已经持续")
			.append(days)
			.append("天成功")
			.append(successfulDays)
			.append("天了！")
			.append("下次再来！");
		}
		this.words = sb.toString();
		return sb.toString();
	}
	
	/**
	 * 处理日期和星期
	 * */
	private String processDate() {
		final Calendar c = Calendar.getInstance(); 
        int mYear = c.get(Calendar.YEAR); //获取当前年份 
        int mMonth = c.get(Calendar.MONTH) + 1;//获取当前月份 
        int mDay = c.get(Calendar.DAY_OF_MONTH);//获取当前月份的日期号码
        int mDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String dayOfWeek = "";
        switch (mDayOfWeek) {
		case Calendar.SUNDAY:
			dayOfWeek = getResources().getString(R.string.sunday);
			break;
		case Calendar.MONDAY:
			dayOfWeek = getResources().getString(R.string.monday);
			break;
		case Calendar.TUESDAY:
			dayOfWeek = getResources().getString(R.string.tuesday);
			break;
		case Calendar.WEDNESDAY:
			dayOfWeek = getResources().getString(R.string.wednesday);
			break;
		case Calendar.THURSDAY:
			dayOfWeek = getResources().getString(R.string.thursday);
			break;
		case Calendar.FRIDAY:
			dayOfWeek = getResources().getString(R.string.friday);
			break;
		case Calendar.SATURDAY:
			dayOfWeek = getResources().getString(R.string.saturday);
			break;
		default:
			dayOfWeek = "星期几啊？";
			break;
		}
	    StringBuilder sb = new StringBuilder();
	    sb.append(mYear).append("/").append(mMonth).append("/")
	    		.append(mDay).append(" ").append(dayOfWeek);
	    String temp = sb.toString();
		return temp;
	}
	
	/**
	 * 向数据库插入信息
	 * 包括：向全部习惯信息插入title、tag、days、currentstate、words
	 * 向单个习惯信息更新latest_state、intstate、successfultimes、
	 * failtimes
	 * */
	protected void insertIntoDatabase() {
		HabitDatabase d = new HabitDatabase(this);
		// 准备全部所需的信息
		Message when = new Message();
		when.what = WHEN_RECORD;
		switch (latestState) {
		case HABIT_STATE_RECORD:
			ContentValues values1 = new ContentValues();
			values1.put(TITLE, title);
			values1.put(TAG, unchangeInfo.getAsInteger(TAG));
			values1.put(DAYS, unchangeInfo.getAsInteger(DAYS));
			values1.put(CURRENT_STATE, HABIT_STATE_RECORD);
			values1.put(WORDS, words);
			values1.put(LATEST_STATE, latestState);
			values1.put(UUID, uuid);
			Habit h1 = new Habit(values1);
			d.insert(h1, when, this);
			break;
		case HABIT_STATE_SUCCESSFUL:
			ContentValues values2 = new ContentValues();
			values2.put(TITLE, title);
			values2.put(TAG, unchangeInfo.getAsInteger(TAG));
			values2.put(DAYS, unchangeInfo.getAsInteger(DAYS));
			values2.put(CURRENT_STATE, HABIT_STATE_SUCCESSFUL);
			values2.put(WORDS, words);
			values2.put(LATEST_STATE, latestState);
			values2.put(IN_T_STATE, HABIT_STATE_SUCCESSFUL);
			values2.put(SUCCESSFUL_TIMES, 
					statistics.getAsInteger(SUCCESSFUL_TIMES) + 1);
			values2.put(UUID, uuid);
			Habit h2 = new Habit(values2);
			d.insert(h2, when, this);		
			break;
		case HABIT_STATE_FAIL:
			ContentValues values3 = new ContentValues();
			values3.put(TITLE, title);
			values3.put(TAG, unchangeInfo.getAsInteger(TAG));
			values3.put(DAYS, unchangeInfo.getAsInteger(DAYS));
			values3.put(CURRENT_STATE, HABIT_STATE_FAIL);
			values3.put(WORDS, words);
			values3.put(LATEST_STATE, latestState);
			values3.put(IN_T_STATE, HABIT_STATE_FAIL);
			values3.put(FAIL_TIMES, 
					statistics.getAsInteger(FAIL_TIMES) + 1);
			values3.put(UUID, uuid);
			Habit h3 = new Habit(values3);
			d.insert(h3, when, this);
			break;

		default:
			break;
		}
	}

	/**
	 * 判断输入的合法性
	 * 包括：是否写了一句话、是否超过字数限制、
	 * 是否选择了习惯状态、是否勾选了分享
	 * */
	protected boolean judgeValidity() {
		boolean temp = false;
		words = recordWords.getText().toString().trim();
		if(words.equals(""))
			temp = false;
		// 判断是否超过字数限制
		// TODO 判断是否选择了习惯状态
		// TODO 判断是否勾选了分享
		else {
			temp = true;
		}
		return temp;
	}
	
	/**每次改变分享状态的时候改变数据库数据状态*/
	private void onChangeShareState(int when) {
		HabitDatabase h = new HabitDatabase(RecordHabitActivity.this);
		ContentValues tempValue = new ContentValues();
		tempValue.put(UUID, uuid);
		switch (when) {
		case UPDATE_RENREN_STATE:
			tempValue.put(SHARE_TO_RENREN, shareToRenren);
			Habit habit1 = new Habit(tempValue);
			Message msg1 = new Message();
			msg1.what = UPDATE_RENREN_STATE;
			h.update(habit1, msg1);
			break;
		case UPDATE_SINA_STATE:
			tempValue.put(SHARE_TO_WEIBO, shareToWeibo);
			Habit habit2 = new Habit(tempValue);
			Message msg2 = new Message();
			msg2.what = UPDATE_SINA_STATE; 
			h.update(habit2, msg2);
			break;
		default:
			break;
		}
	}
	
	private boolean checkNetworkInfo() {
        /*ConnectivityManager conMan = (ConnectivityManager) 
        		getSystemService(Context.CONNECTIVITY_SERVICE);
        // mobile 3G Data Network
        State mobile = conMan.getNetworkInfo(
        		ConnectivityManager.TYPE_MOBILE).getState();
        // wifi
        State wifi = conMan.getNetworkInfo(
        		ConnectivityManager.TYPE_WIFI).getState();
        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
            return true;
        if(wifi==State.CONNECTED||wifi==State.CONNECTING)
            return true;
        // computer
        return false;
        // startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面*/
		ConnectivityManager cm = (ConnectivityManager) mRecordHabit.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm == null)
			return false;
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if(netinfo == null) {
			return false;
		}
		if(netinfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_MSG_REFRESH_RENREN:
				renrenAuth = settings.getBoolean(RENREN_AUTHORIZE, false);
				if(renrenAuth) {
					addRenren.setImageResource(R.drawable.renren_icon_light);
					shareToRenren = STATE_PUBLISH;
					System.out.println("人人认证成功");
				}
				break;
			case HANDLER_MSG_REFRESH_WEIBO:
				weiboAuth = settings.getBoolean(WEIBO_AUTHORIZE, false);
				if(weiboAuth) {
					addWeibo.setImageResource(R.drawable.weibo_icon_light);
					shareToWeibo = STATE_PUBLISH;
					System.out.println("新浪认证成功");
				}
				break;
			default:
				break;
			}
		}
	}
}