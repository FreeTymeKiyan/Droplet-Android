package org.dian.kiyan.Activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dian.kiyan.R;
import org.dian.kiyan.Constants.Constants;
import org.dian.kiyan.CustomLayout.MyScrollLayout;
import org.dian.kiyan.Databases.DataRefreshSvc;
import org.dian.kiyan.Databases.HabitDatabase;
import org.dian.kiyan.Databases.MySimpleAdapter;
import org.dian.kiyan.Databases.SidelistAdapter;
import org.dian.kiyan.ReceiveBroadcast.AlarmReceiver;
import org.dian.kiyan.ReceiveBroadcast.TimeListenerSvc;
import org.dian.kiyan.Utils.Utils;

import com.mobclick.android.a;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import static org.dian.kiyan.Constants.Constants.*;

/**
 * 根据Title查询数据库获取对应习惯的信息
 * 组织并显示到习惯详情页面
 * */
public class HabitDetailsActivity extends Activity {
	/**导航栏界面的ListView*/
	private ListView sideList;
	/**导航栏的Item适配器*/
	private SidelistAdapter sideListdapter; 
	/**导航界面的数据源*/
	private List<Map<String, Object>> sideListData 
			= new ArrayList<Map<String, Object>>();// 声明列表容器
	private int flag = 0;
	public static boolean moved;
	private MyScrollLayout mScrollLayout;	
	private int mViewCount;	
	/**习惯的总数*/
	private int habitCount;
	/**习惯的全局唯一标识*/
	private String uuid = "";
	/**习惯的title*/
	private String title = "";
	/**ListView的适配器*/
	private MySimpleAdapter adapter;
	private ListView listView; // 声明列表视图对象
    private List<Map<String, Object>> listData 
    		= new ArrayList<Map<String, Object>>();// 声明列表容器
	private Drawable detailTagBackground;
	/**坚持习惯的总天数*/
	private int insistDays;
	/**习惯成功的天数*/
	private int successfulDays;
	/**当前周期内的习惯状态*/
	private int inTState;
	/* 两个要即时刷新的TextView */
	/**关于习惯的简要信息*/
	private TextView briefState;
	/**习惯成功率，需格式化*/
	private TextView monthlyRateNum;
	/**背景图的路径*/
	private String background = "";
	/**头部的view*/
	View layout;
	/**显示tag的ImageView*/
	ImageButton detailTag;
	/**导航栏的listViewheader*/
	private TextView statistic5;
	/**习惯的所有个性数据*/
	private ContentValues statistics = new ContentValues();
	/**习惯对应的标签*/
	private int tag;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.habit_details);
		getInfo();
        // compareDate();/*比较当前日期和退出日期*/
		prepareData();
		initView();
		init();
		// TODO 界面布局和宽高限定问题
		// TODO 按menu内设置键跳转到设置页面
		// TODO 加上友盟的SDK内容
		// TODO 导航栏的菜单
		// TODO 点击导航栏右侧的分享键跳转至分享界面
	}

	/**
	 * 获取首页的Intent
	 * 得到里面的UUID
	 * */
	private void getInfo() {
		Intent i = getIntent();
		if(i.hasExtra(UUID))
			uuid = i.getStringExtra(UUID);
		else {
			System.out.println("no such extra");
		}
		if(i.hasExtra(NOTIFICATION)){
			AlarmReceiver.NOTIFICATION_STATE = NOTIFICATION_DISAPPEAR;
		}
	}
	
	/**
	 * 根据UUID查询数据库
	 * 获取需要显示的信息
	 * */
	private void prepareData() {
		// 查询数据库
		HabitDatabase h = new HabitDatabase(this);
		Bundle bundle = h.query(uuid);
		habitCount = h.queryTotalCount();
		statistics = h.queryStatistics(uuid);
		title = statistics.getAsString(TITLE);
		background = statistics.getAsString(BACKGROUND);
		inTState = statistics.getAsInteger(IN_T_STATE);
		tag = statistics.getAsInteger(TAG);
        for(int i = 0; i < bundle.size(); i++) {
         	Map<String, Object> map = new HashMap<String, Object>();
        	String key = "temp" + i;
        	Bundle b = new Bundle(bundle.getBundle(key));
        	String words = b.getString(WORDS);
        	map.put("tv_detailWords", words /*记录的一句话*/ );
        	// 得到图片tag并转成图片
        	int tag = b.getInt(TAG);
        	int state = b.getInt(CURRENT_STATE);
        	int d = getResIDFromTagAndState(tag, state);
            map.put("iv_detailState", d /*习惯的图片状态*/ );
            String stateTitle = processDetailState(bundle.
            		getBundle(key).getInt(CURRENT_STATE));
            map.put("tv_detailStateTitle", stateTitle /*习惯的文字状态、内容、这个习惯*/ );
            String timestamp = b.getString(TIMESTAMP);
            timestamp = processTimestamp(timestamp);
            map.put("tv_timestamp",  /*处理过的习惯时间戳*/ timestamp);
        	listData.add(map);
        }
        // 获取坚持天数，成功天数，
        ContentValues values = h.queryStatistics(uuid);
        successfulDays = values.getAsInteger(SUCCESSFUL_TIMES);
        int failDays = values.getAsInteger(FAIL_TIMES);
        int withoutRecordDays = values.getAsInteger(WITHOUT_RECORD_TIMES);
        insistDays = successfulDays + failDays + withoutRecordDays;
        
        // 设置适配器
        String[] from = new String[]{"tv_detailWords", "iv_detailState", 
        		"tv_timestamp", "tv_detailStateTitle"}; 
        int[] to = new int[]{R.id.tv_detailWords, R.id.iv_detailState, 
        		R.id.tv_timestamp, R.id.tv_detailStateTitle,};
        adapter = new MySimpleAdapter(this, listData, 
        		R.layout.custom_list_item, from, to);
        
        //add it/////////////////////////////////////////////////////////
        //得到list_Item_Deta的内容
        /*得到listItem_Data标题*/
    	SQLiteDatabase db = h.getWritableDatabase();
    	Cursor sideListCursor = db.rawQuery("select * from " + ALL_HABITS, null);
    	startManagingCursor(sideListCursor);
    	while(sideListCursor.moveToNext()) {
    		HashMap<String, Object> map = new HashMap<String, Object>();
    		// get from database
			map.put("list_Image", R.drawable.triangle);
			map.put("list_Words", sideListCursor.getString(1));
			map.put(UUID, sideListCursor.getString(2));
			sideListData.add(map);
    		
    	}     
    	/*得到list_Item_Data的末尾内容*/
    	for(int i = 0; i < 2; i++){
			HashMap<String, Object> map = new HashMap<String, Object>();
			if(i == 0){
				map.put("list_Image", R.drawable.add);
				map.put("list_Words", "添加新习惯");
				sideListData.add(map);
			}
			if(i == 1){
				map.put("list_Image", R.drawable.settings);
				map.put("list_Words", "设置");
				sideListData.add(map);
			}
    	}
    	db.close();
        String[] from2 = new String[]{"list_Words", "list_Image"};
        int[] to2 = new int[]{R.id.list_Words, R.id.list_Image};
        sideListdapter= new SidelistAdapter(this, sideListData, 
        		R.layout.habit_list_display, from2, to2);
        //add it////////////////////////////////////////////////////
	}
	
	
	
	/**用返回的tag得到对应的标签R值*/
	private int getResIDFromTagAndState(int tag, int inTState) {
		int d = 0;
		switch (tag) {
		case NO_TAG:
			// TODO 需要实现HABIT_21天的状态
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag1_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag1_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag1_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag1_success;
				break;
			default:
				break;
			}
			break;
		case RED_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag1_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag1_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag1_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag1_success;
				break;
			default:
				break;
			}
			break;
		case ORANGE_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag2_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag2_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag2_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag2_success;
				break;
			default:
				break;
			}
			break;
		case YELLOW_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag3_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag3_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag3_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag3_success;
				break;
			default:
				break;
			}
			break;
		case GREEN_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag4_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag4_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag4_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag4_success;
				break;
			default:
				break;
			}
			break;
		case DARK_GREEN_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag5_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag5_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag5_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag5_success;
				break;
			default:
				break;
			}
			break;
		case LIGHT_PURPLE_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag6_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag6_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag6_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag6_success;
				break;
			default:
				break;
			}
			break;
		case PURPLE_TAG:
			switch (inTState) {
			case HABIT_STATE_CREATE:
				d = R.drawable.tag7_create;
				break;
			case HABIT_STATE_FAIL:
				d = R.drawable.tag7_fail;
				break;
			case HABIT_STATE_RECORD:
				d = R.drawable.tag7_record;
				break;	
			case HABIT_STATE_SUCCESSFUL:
				d = R.drawable.tag7_success;
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		detailTagBackground = getResources().getDrawable(d);
		return d;
	}

	/**根据返回的时间戳处理出时间的方法*/
	private String processTimestamp(String timestamp) {
		final int jumpFrom = 11;
		final int jumpTo = 16;
		timestamp = timestamp.substring(jumpFrom, jumpTo);
		timestamp = getResources().getString(R.string.odds2) + " "
				+ timestamp;
		return timestamp;
	}

	/**根据返回的状态值处理显示内容的方法*/
	private String processDetailState(int asInteger) {
		String temp = "";
		switch (asInteger) {
		case HABIT_STATE_SUCCESSFUL:
			temp = getResources().getString(R.string.state_successful); 
			break;
		case HABIT_STATE_FAIL:
			temp = getResources().getString(R.string.state_fail); 
			break;
		case HABIT_STATE_CREATE:
			temp = getResources().getString(R.string.state_create);
			break;
		case HABIT_STATE_RECORD:
			temp = getResources().getString(R.string.state_record);
			break;
		default:
			temp = "我也不知道发生了什么~";
			break;
		}
		return temp;
	}

    private void init() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout_one = inflater.inflate(R.layout.list_item_header,
				null);
		statistic5 = (TextView) layout_one.findViewById(R.id.habit_count_statistic1);
		statistic5.setText(habitCount + "");
//    	TextView exit_text= (TextView) this.findViewById(R.id.ib_exit);
//    	ImageView exit_Image= (ImageView) this.findViewById(R.id.ib_exit_image);
    	moved = false;
    	mScrollLayout = (MyScrollLayout) findViewById(R.id.ScrollLayout); 	
    	sideList = (ListView) this.findViewById(R.id.List_Guide_Item);
    	sideList.addHeaderView(layout_one, null, false);
    	sideList.setAdapter(sideListdapter);
    	//Habit_ListView.setAdapter(adapter);
    	/*导航栏的消息响应函数*/
    	sideList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 监听快速记录菜单的点击事件
				if(arg2 - 1 == sideListData.size() - 2) {
					addNewHabit();
				} else if(arg2 - 1 == sideListData.size() - 1) {
					Intent intent = new Intent(HabitDetailsActivity.this, 
							MainPrefActivity.class);
					startActivity(intent);
					HabitDetailsActivity.this.finish();
				} else {
					String uuid = sideListData.get(arg2 - 1).get(UUID).toString();
					Intent i = new Intent();
					i.putExtra(UUID, uuid);
					i.setClass(HabitDetailsActivity.this, HabitDetailsActivity.class);
					startActivityForResult(i, REQUEST_HABIT_DETAILS);
					overridePendingTransition(R.anim.enter_habit_details, R.anim.exit_habit_details);
					HabitDetailsActivity.this.finish();
				}
			}
		});
    	mViewCount = mScrollLayout.getChildCount();  	  	
    }
    
	protected void addNewHabit() {
		Intent i = new Intent(HabitDetailsActivity.this, 
				AddHabitActivity.class);
		startActivityForResult(i, REQUEST_ADD_HABIT);
	}
	
	/**设置习惯详情的界面*/
	private void initView() {
		// ListView的头部
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.list_view_header, 
				null);
		TextView habitTitle = (TextView) findViewById(R.id.tv_habitDetails);
		habitTitle.setText(title);
		Uri uri = Uri.parse(background);
		Bitmap test = null;
		try {
			test = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			test = BitmapFactory.decodeResource(getResources(), R.drawable.test_bg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO 改进图片缩放效果
		// TODO 从屏幕读取大小来进行缩放
		test = resizeBitmap(test, 320, 173);
		Drawable d = new BitmapDrawable(test); 
		layout.setBackgroundDrawable(d);
		/* 标签 */
		detailTag = (ImageButton) layout.findViewById(
				R.id.ib_detailTag);
		detailTag.setBackgroundResource(processTag());
		// TODO 选择标签功能
		TextView detailTitle = (TextView) layout.findViewById(R.id.tv_detailTitle);
		detailTitle.setText(title);
		briefState = (TextView) layout.findViewById(R.id.tv_briefState);
		String processedBriefState = processBriefState();
		briefState.setText(processedBriefState);
		TextView detailToday = (TextView) layout.findViewById(R.id.tv_detailToday);
		detailToday.setText(processDetailToday());
		detailToday.setTextColor(processDetailTodayColor());
		TextView monthlyRate = (TextView) layout.findViewById(R.id.tv_monthlyRate);
		String processedMonthlyRate = processMonthlyRate();
		monthlyRate.setText(processedMonthlyRate);
		monthlyRateNum = (TextView) layout.findViewById(R.id.tv_monthlyRateNum);
		String processedmonthlyRateNum = processSingleMonthlyRateNum();
		monthlyRateNum.setText(processedmonthlyRateNum);
		// TODO 时间要1分钟更新一次，用handler
		TextView time = (TextView) layout.findViewById(R.id.tv_currentTime);
		String processedTime = processTime();
		time.setText(processedTime);
		TextView date = (TextView) layout.findViewById(R.id.tv_date);
		String processedDate = processDate();
		date.setText(processedDate);
		listView = (ListView) findViewById(R.id.lv_singleHabitLog);
		layout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HabitDetailsActivity.this, HabitPrefActivity.class);
				intent.putExtra(UUID, uuid);
				startActivityForResult(intent, REQUEST_ADD_HABIT);
			}
		});
		// 添加ListView的头部分
        listView.addHeaderView(layout, null, false);
//        listView.addHeaderView(layout);
        listView.setAdapter(adapter);
        listView.setDrawSelectorOnTop(false);
        listView.setOnTouchListener(new OnTouchListener() {               
            @Override    
            public boolean onTouch(View v, MotionEvent event) { 
            	if(moved == true){
//            		mScrollLayout.snapToScreen(1);
//            		flag++;
//            		moved = true;
//            		if(flag % 2 == 0){
//            			moved = false;
//            		}
//            		Log.e(TAG, "1"); 
            		return true;
            	}
//            	Log.e(TAG, "2"); 
            	return false;
            	//return listView.onTouchEvent(event);
            }    
        }); 
        
        //add it//////////////////////////////////////////////////////////////////////
        ImageButton guideMenu = (ImageButton) findViewById(R.id.ib_guideMenu);
        guideMenu.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						// TODO 读取Title跳转到记录页面
						if(moved == false){
							moved = true;
							mScrollLayout.snapToScreen(0);
						}
						else{
							moved = false;
							mScrollLayout.snapToScreen(1);
						}
					}
				});
        //add it/////////////////////////////////////////////////////////////////////
        
        /** 记录按钮 */
        ImageButton detailRecord = (ImageButton) findViewById(R.id.ib_detailRecord);
        detailRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 读取UUID跳转到记录页面
				Intent i = new Intent();
				Bundle b = new Bundle();
				b.putString(UUID, uuid);
				i.putExtras(b);
				i.setClass(HabitDetailsActivity.this, RecordHabitActivity.class);
				startActivityForResult(i, REQUEST_RECORD);
			}
		});
        
        /* 回到顶部*/
        ImageButton backToTop = (ImageButton) findViewById(R.id.ib_detailBackToTop);
        backToTop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listView.setSelectionFromTop(0, 0); // 回到页面的顶端
			}
		});
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    if (event.getRepeatCount() > 0 && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
	        return true;
	    }
	    return super.dispatchKeyEvent(event);
	}
	//这样就可以了屏蔽menu键长按了。
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//按返回键时，创建退出提示对话框
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(moved == false){
				HabitDetailsActivity.this.setResult(RESULT_OK, getIntent());
				/* 结束这个activity */
				HabitDetailsActivity.this.finish();
			}	
			else if(moved == true){
				moved = false;
				mScrollLayout.snapToScreen(1);
			}
		} else if(keyCode == KeyEvent.KEYCODE_MENU){
			if(moved == false){
				moved = true;
				mScrollLayout.snapToScreen(0);
				//listView.setItemsCanFocus(false);
				//listView.setFocusableInTouchMode(false);
			} else if(moved == true){
				mScrollLayout.snapToScreen(1);
				//listView.setItemsCanFocus(true);
				//listView.setFocusableInTouchMode(false);
				moved = false;
			}
		}
		//return super.onKeyDown(keyCode, event);
		return true;
	}
	
	private String processTime() {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		String processedTime = "";
		if(minute < 10)
			processedTime = hour + ":" + "0" + minute;
		else {
			processedTime = hour + ":" + minute;
		}
		return processedTime;
	}

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

	private String processSingleMonthlyRateNum() {
		String temp = "";
		if(insistDays == 0) {
			temp = "0%";
		} else {
			float temp1 = insistDays;
			float temp2 = successfulDays / temp1;
			DecimalFormat df = new DecimalFormat("##%");
			temp = df.format(temp2); 
		}
		
		return temp;
	}

	private String processMonthlyRate() {
		StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.monthlyRate));
		String temp = sb.toString();
		return temp;
	}
	
	private int processTag() {
		int id = 0;
		switch (tag) {
		case RED_TAG:
			id = R.drawable.habit_tag_1;
			break;
		case ORANGE_TAG:
			id = R.drawable.habit_tag_2;
			break;
		case YELLOW_TAG:
			id = R.drawable.habit_tag_3;
			break;
		case GREEN_TAG:
			id = R.drawable.habit_tag_4;
			break;
		case DARK_GREEN_TAG:
			id = R.drawable.habit_tag_5;
			break;
		case LIGHT_PURPLE_TAG:
			id = R.drawable.habit_tag_6;
			break;
		case PURPLE_TAG:
			id = R.drawable.habit_tag_7;
			break;
		default:
			break;
		}
		return id;
	}

	private String processBriefState() {
		StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.briefState1))
				.append(insistDays)
				.append(getResources().getString(R.string.briefState2))
				.append("\n")
				.append(getResources().getString(R.string.briefState3))
				.append(successfulDays)
				.append(getResources().getString(R.string.briefState2));
		String temp = sb.toString();
		return temp;
	}
	
	private String processDetailToday() {
		StringBuilder sb = new StringBuilder();
		switch (inTState) {
		case HABIT_STATE_NOT_RECORDED:
			sb.append(getResources().getString(R.string.briefState4));
			break;
		case HABIT_STATE_SUCCESSFUL:
			sb.append(getResources().getString(R.string.briefState5));		
			break;
		case HABIT_STATE_FAIL:
			sb.append(getResources().getString(R.string.briefState6));
			break;
		case HABIT_STATE_CREATE:
			sb.append(getResources().getString(R.string.briefState7));
			break;
		case HABIT_STATE_RECORD:
			sb.append(getResources().getString(R.string.briefState8));
			break;
		default:
			break;
		}
		return sb.toString();
	}
	
	private int processDetailTodayColor() {
		int color = 0;
		switch (inTState) {
		case HABIT_STATE_NOT_RECORDED:
			color = Color.RED;
			break;
		default:
			color = Color.WHITE;
			break;
		}
		return color;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ADD_HABIT:
			switch (resultCode) {
			case RESULT_CANCELED:
				break;
			case RESULT_OK:
				refreshView();
				break;
			}
			break;
		case REQUEST_HABIT_DETAILS:
			break;
		case REQUEST_RECORD:
			switch (resultCode) {
			case RESULT_CANCELED:
				break;
			case RESULT_OK:
				refreshView();
				break;
			}
			
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 所有刷新界面的代码
	 * */
	private void refreshView() {
		/* listView部分的刷新 */
		HabitDatabase h = new HabitDatabase(this);
		SQLiteDatabase db = h.getReadableDatabase();
		Cursor c = db.query(ALL_HABITS_LOG, null, "uuid=?", 
				new String[]{uuid}, null, null, "_id DESC");
		c.moveToFirst();
		if(c.getCount() != listData.size()) {
			Map<String, Object> map = new HashMap<String, Object>();
	    	String words = c.getString(5);
	    	map.put("tv_detailWords", words);
	    	int tag = c.getInt(2);
	    	inTState = c.getInt(4);
	    	int d = getResIDFromTagAndState(tag, inTState); // 得到图片tag并转成图片
	        map.put("iv_detailState", d);
	        String stateTitle = processDetailState(c.getInt(4));
	        map.put("tv_detailStateTitle", stateTitle);
	        String timestamp = c.getString(6);
	        timestamp = processTimestamp(timestamp);
	        map.put("tv_timestamp",timestamp);
	        int addToFirst = 0;
	    	listData.add(addToFirst, map);
	    	adapter.notifyDataSetChanged();
		}
        c.close();
		/* 头部分的内容刷新 */
		ContentValues values = h.queryStatistics(uuid);
        successfulDays = values.getAsInteger(SUCCESSFUL_TIMES);
        insistDays = successfulDays + values.getAsInteger(FAIL_TIMES) 
				+ values.getAsInteger(WITHOUT_RECORD_TIMES);
        
        tag = values.getAsInteger(TAG);
        detailTag.setBackgroundResource(processTag());
        background = values.getAsString(BACKGROUND);
        Uri uri = Uri.parse(background);
		Bitmap test = null;
		try {
			test = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			test = BitmapFactory.decodeResource(getResources(), R.drawable.test_bg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO 改进图片缩放效果
		// TODO 从屏幕读取大小来进行缩放
		test = resizeBitmap(test, 320, 173);
		Drawable d = new BitmapDrawable(test); 
		layout.setBackgroundDrawable(d);
		
        String processedBriefState = processBriefState();
		briefState.setText(processedBriefState);
		String processedMonthlyRateNum = processSingleMonthlyRateNum();
		monthlyRateNum.setText(processedMonthlyRateNum);
		habitCount = h.queryTotalCount();
		if(sideListData.size() != habitCount) {
			/*得到listItem_Data标题*/
			db = h.getReadableDatabase();
	    	Cursor sideListCursor = db.rawQuery("select * from " + ALL_HABITS, null);
	    	startManagingCursor(sideListCursor);
	    	sideListData.remove(sideListData.size() - 1); // 除去最后两条内容
	    	sideListData.remove(sideListData.size() - 1);
	    	for(sideListCursor.moveToPosition(sideListData.size() + 1); 
	    			!sideListCursor.isAfterLast(); sideListCursor.moveToNext()) {
	    		HashMap<String, Object> map = new HashMap<String, Object>();
    			// get from database
	    		map.put("list_Image", R.drawable.triangle);
	    		map.put("list_Words", sideListCursor.getString(1));
	    		map.put(UUID, sideListCursor.getString(2));
				sideListData.add(map);
	    	}
	    	/*得到list_Item_Data的末尾内容*/
	    	for(int i = 0; i < 2; i++){
				HashMap<String, Object> map = new HashMap<String, Object>();
				if(i == 0){
					map.put("list_Image", R.drawable.add);
					map.put("list_Words", "添加新习惯");
					sideListData.add(map);
				}
				if(i == 1){
					map.put("list_Image", R.drawable.settings);
					map.put("list_Words", "设置");
					sideListData.add(map);
				}
	    	}
	    	sideListdapter.notifyDataSetChanged();
		}
		db.close();
		h.close();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onDestroy();
	}
	
	/**
     * 保持长宽比缩小Bitmap
     *
     * @param bitmap
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {

        int originWidth  = bitmap.getWidth();
        int originHeight = bitmap.getHeight();

        // no need to resize
        if (originWidth < maxWidth && originHeight < maxHeight) {
            return bitmap;
        }

        int width  = originWidth;
        int height = originHeight;

        // 若图片过宽, 则保持长宽比缩放图片
        if (originWidth > maxWidth) {
            width = maxWidth;

            double i = originWidth * 1.0 / maxWidth;
            height = (int) Math.floor(originHeight / i);

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }

        // 若图片过长, 则从上端截取
        if (height > maxHeight) {
            height = maxHeight;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        return bitmap;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		refreshView();
	}
}