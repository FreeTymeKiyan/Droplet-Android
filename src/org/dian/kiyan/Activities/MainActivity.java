package org.dian.kiyan.Activities;

import static org.dian.kiyan.Constants.Constants.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dian.kiyan.R;
import org.dian.kiyan.CustomLayout.MyScrollLayout;
import org.dian.kiyan.Databases.HabitDatabase;
import org.dian.kiyan.Databases.MySimpleAdapter;
import org.dian.kiyan.Databases.PopupListAdapter;
import org.dian.kiyan.Databases.SidelistAdapter;
import org.dian.kiyan.R.anim;
import org.dian.kiyan.R.drawable;
import org.dian.kiyan.R.id;
import org.dian.kiyan.R.layout;
import org.dian.kiyan.R.string;
import org.dian.kiyan.R.style;
import org.dian.kiyan.ReceiveBroadcast.AlarmReceiver;
import org.dian.kiyan.ReceiveBroadcast.TimeListenerSvc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;

public class MainActivity extends Activity {
	
	/**导航栏界面的ListView*/
	private ListView sideListView;
	/**导航栏界面的适配器*/
	private SidelistAdapter sideListAdapter; 
	/**导航栏ListView的数据源*/
	private List<Map<String, Object>> sideListData 
			= new ArrayList<Map<String, Object>>();
	/**用于判断侧边栏是否弹出*/
	public static boolean moved;
	/**侧边栏*/
	private MyScrollLayout mScrollLayout;	
	private int mViewCount;	
	/**屏幕宽度*/
	public static int screenWidth;
	/**屏幕高度*/
	public static int screenHeight;
	/***/
	private ImageButton add;
	/***/
	private ImageButton share;
	/**自定义ListView的数据源*/
	private List<Map<String, Object>> listData 
			= new ArrayList<Map<String, Object>>();// 声明列表容器
	SimpleAdapter listItemAdapter;
	/**每一个习惯的Tag对应的图片*/
	private Drawable certainTagBackground;
	/**习惯总数，需要刷新*/
	private int habitCount;
	/**成功坚持的习惯数量*/
	private int successCount;
	/**失败了的习惯数量 */
	private int failCount;
	/**等待记录的习惯数量 */
	private int waitCount;
	/**自定义ListView的数据适配器*/
	private MySimpleAdapter adapter;
	/**总成功率*/
	private float successRate;
	/**自定义的ListView*/
	private ListView listView;
	/**习惯总数的TextView*/
	private TextView statistic1;
	/**成功数量的TextView*/
	private TextView statistic2;
	/**失败的TextView*/
	private TextView statistic3;
	/**当天未记录的TextView*/
	private TextView statistic4;
	/**导航栏的listViewheader*/
	private TextView statistic5;
	/**所有习惯的成功率TextView*/
	private TextView monthlyRateNum;
	/**从系统获取的当前时间*/
	private TextView time;
	/**弹出的窗口*/
	public static PopupWindow pw;
	/**弹出窗口的ListView*/
	private ListView popupWindowList;
	/**弹出窗口ListView的数据源*/
	private List<Map<String, Object>> popuplistData 
			= new ArrayList<Map<String, Object>>();
	/**弹出窗口ListView的适配器*/
	private PopupListAdapter popupAdapter;
	/**快速记录按键*/
	private ImageButton mainFastRecord;
	/**快速记录按键上面的加号*/
	private ImageView mainFastRecordPlus;
	/**暂存从数据库获取的所有日志数据*/
	private Bundle allHabitsLog;
	/**用于控制后台Service*/
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*开始后台服务*/
        askForDateMinus();
        checkIntent();
        /*准备数据*/
        prepareData();
        /*初始化界面*/
        init(); 
        initViews();
        /*refreshData rd = new refreshData();
        rd.execute(0);*/
    }
    
    /**查询是否由Notification进入
     * */
    private void checkIntent(){
    	Intent intent = getIntent();
    	if(intent != null && intent.hasExtra(NOTIFICATION)){
    		AlarmReceiver.NOTIFICATION_STATE = NOTIFICATION_DISAPPEAR;
    	}
    }
    
    /**
	 * 通过后台获取上一次的日期和现在的日期比较
	 * 得有处理的进度框
	 * 或者在启动界面之前读取
	 * */
	private void askForDateMinus() {
		Intent service = new Intent(this, TimeListenerSvc.class);
        bindService(service, conn, BIND_AUTO_CREATE);
	}
    
	/**
	 * 准备后台数据 
	 * */
	private void prepareData() {
		HabitDatabase h = new HabitDatabase(this);
		/* 正在尝试养成的习惯总数 */
		habitCount = h.queryTotalCount();
		if(allHabitsLog == null) {
			allHabitsLog = new Bundle(h.queryAll());
		}
		float[] count = h.queryStatistics();
		/* 成功坚持的习惯数量 */
		successCount = (int)count[0];
		/* 失败了的习惯数量 */
		failCount = (int)count[1];
		/* 等待记录的习惯数量 */
		waitCount = (int)count[2]; 
		/*成功率*/
		successRate = count[3];
		/* 获取所有ListView的内容 */
		for(int i = 0; i < allHabitsLog.size(); i++) {
         	Map<String, Object> map = new HashMap<String, Object>();
        	String key = "temp" + i;
        	Bundle b = new Bundle(allHabitsLog.getBundle(key));
        	String words = b.getString(WORDS);
        	map.put("tv_itemWords", words /*记录的一句话*/ );
        	// 得到图片tag并转成图片
        	int tag = b.getInt(TAG);
        	int currentState = b.getInt(CURRENT_STATE);
        	int d = getResIDFromTagAndState(tag, currentState);
            map.put("iv_itemState", d /*习惯的图片状态*/ );
            String stateTitle = processDetailState(currentState, 
            		b.getString(TITLE));
            map.put("tv_itemStateText", stateTitle /*习惯的文字状态、内容、这个习惯*/ );
            String timestamp = b.getString(TIMESTAMP);
            timestamp = processTimestamp(timestamp);
            map.put("tv_itemTimestamp",  /*处理过的习惯时间戳*/ timestamp);
            map.put(TITLE, b.getString(TITLE));
            map.put("tv_itemTitle", b.getString(TITLE));
            map.put(UUID, b.getString(UUID));
            map.put("titleColor", processColorWithTag(tag));
        	listData.add(map);
        }
		/*设置主页自定义ListView的适配器*/
		String[] from = new String[]{"tv_itemTitle", "tv_itemWords", "iv_itemState", 
        		"tv_itemTimestamp", "tv_itemStateText"};
        int[] to = new int[]{R.id.tv_itemTitle, R.id.tv_itemWords, R.id.iv_itemState, 
        		R.id.tv_itemTimestamp, R.id.tv_itemStateText};
        adapter = new MySimpleAdapter(this, listData, 
        		R.layout.main_list_item, from, to);
        
    	SQLiteDatabase db = h.getWritableDatabase();
    	Cursor sideListCursor = db.rawQuery("select * from " + ALL_HABITS, null);
    	startManagingCursor(sideListCursor);
    	while(sideListCursor.moveToNext()) {
    		HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("list_Image", R.drawable.triangle);
			// get from database
			map.put("list_Words", sideListCursor.getString(1));
			map.put(UUID, sideListCursor.getString(2));
			sideListData.add(map);
    	}     
    	/*得到sideListData的末尾内容*/
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
    	h.close();
    	
        String[] from2 = new String[]{"list_Words", "list_Image"};
        int[] to2 = new int[]{R.id.list_Words, R.id.list_Image};
        sideListAdapter= new SidelistAdapter(this, sideListData, 
        		R.layout.habit_list_display, from2, to2);
        
        // 写在做菜单的数据适配源getAllHabits();
        String[] from1 = new String[]{"iv_mainFastRecordTag", 
				"tv_mainFastRecordTitle", "iv_mainFastRecordState"};
        int[] to1 = new int[]{R.id.iv_mainFastRecordTag, 
        			R.id.tv_mainFastRecordTitle, 
        			R.id.iv_mainFastRecordState};
		popupAdapter = new PopupListAdapter(this, popuplistData, 
				R.layout.popup_window_list_item, from1, to1);
	}
	
	/**
	 * 根据每个习惯对应的标签变换title的颜色
	 * */
	private int processColorWithTag(int habitTag) {
		int color = 0;
		// TODO 根据Tag获取对应颜色（写成常量）
		switch (habitTag) {
		case RED_TAG:
			color = Color.parseColor("#f2572a");
			break;
		case ORANGE_TAG:
			color = Color.parseColor("#faac38");
			break;
		case YELLOW_TAG:
			color = Color.parseColor("#f9e555");
			break;
		case GREEN_TAG:
			color = Color.parseColor("#b0de47");
			break;
		case DARK_GREEN_TAG:
			color = Color.parseColor("#248a8a");
			break;
		case LIGHT_PURPLE_TAG:
			color = Color.parseColor("#827de3");
			break;
		case PURPLE_TAG:
			color = Color.parseColor("#a962bd");
			break;
		default:
			break;
		}
		return color;
	}


	/**
	 * 用返回的tag得到对应的标签R值
	 * */
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
		certainTagBackground = getResources().getDrawable(d);
		return d;
	}
	
	/**根据返回的状态值处理显示内容的方法*/
	private String processDetailState(int asInteger, String title) {
		String temp = "";
		switch(asInteger) {
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
	
	/**根据返回的时间戳处理出时间的方法*/
	private String processTimestamp(String timestamp) {
		final int jumpFrom = 11;
		final int jumpTo = 16;
		timestamp = timestamp.substring(jumpFrom, jumpTo);
		timestamp = getResources().getString(R.string.odds2) + " "
				+ timestamp;
		return timestamp;
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
    	sideListView = (ListView) this.findViewById(R.id.List_Guide_Item);
    	sideListView.addHeaderView(layout_one, null, false);
    	sideListView.setAdapter(sideListAdapter);
    	//Habit_ListView.setAdapter(adapter);
    	sideListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 监听快速记录菜单的点击事件
				if(arg2 - 1 == sideListData.size() - 2) {
					addNewHabit();
				} else if(arg2 - 1 == sideListData.size() - 1) {
					Intent intent = new Intent(MainActivity.this, 
							MainPrefActivity.class);
					startActivity(intent);
				} else {
					String uuid = sideListData.get(arg2 - 1).get(UUID).toString();
					Intent i = new Intent();
					i.putExtra(UUID, uuid);
					i.setClass(MainActivity.this, 
							HabitDetailsActivity.class);
					startActivityForResult(i, REQUEST_HABIT_DETAILS);
					overridePendingTransition(R.anim.enter_habit_details, 
							R.anim.exit_habit_details);
				}
			}
		});
    	//Haibt_adapter = new MySimpleAdapter(this, listData); 
    	mViewCount = mScrollLayout.getChildCount();  	  	
    	//mScrollLayout.SetOnViewChangeListener(this);
    }
	
	private void initViews() {
		/*自定义ListView的头部*/
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.main_list_view_header,
				null);
		Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.test_bg);
		if(b != null){
			b = Bitmap.createScaledBitmap(b, 320, 173, true);
		}
		Drawable d = new BitmapDrawable(b); 
		layout.setBackgroundDrawable(d); // 设置背景
		statistic1 = (TextView) layout.findViewById(R.id.tv_statistic1);
		statistic1.setText(habitCount + "");
		statistic2 = (TextView) layout.findViewById(R.id.tv_statistic2);
		statistic2.setText(successCount + "");
		statistic3 = (TextView) layout.findViewById(R.id.tv_statistic3);
		statistic3.setText(failCount + "");
		statistic4 = (TextView) layout.findViewById(R.id.tv_statistic4);
		statistic4.setText(waitCount + "");
		monthlyRateNum = (TextView) layout.findViewById(R.id.tv_successRateNum);
		String processedmonthlyRateNum = processAllMonthlyRateNum();
		monthlyRateNum.setText(processedmonthlyRateNum);
		time = (TextView) layout.findViewById(R.id.tv_mainTime);
		String processedTime = processTime();
		time.setText(processedTime);
		TextView date = (TextView) layout.findViewById(R.id.tv_mainDate);
		String processedDate = processDate();
		date.setText(processedDate);
		listView = (ListView) findViewById(R.id.lv_allHabitsLogList);
		TextView exit = (TextView) layout.findViewById(R.id.exit);
		ImageView exit_image = (ImageView) layout.findViewById(R.id.exit_image);
		// 添加ListView的头部分
        listView.addHeaderView(layout, null, false);
        listView.setAdapter(adapter);
        //mScrollLayout.requestFocus();
        mScrollLayout.bringToFront();
        //listView.requestFocus();
        //该方法非常不好，应放弃
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
            		Log.e(TAG, "1"); 
            		return true;
            	}
//            	Log.e(TAG, "2"); 
            	return false;
            	//return listView.onTouchEvent(event);
            }    
        }); 
        
        //listView.setItemsCanFocus(true);
        
        /*快速记录按键*/
        mainFastRecord = (ImageButton) findViewById(R.id.ib_mainFastRecord);
        mainFastRecord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 显示一个快速列表
				if(pw.isShowing()) {
					System.out.println("on click this");
					showMainFastRecordExitAnimation();					
					pw.dismiss();
				}else if(moved == false) {
					if(habitCount == 0){
						showMainFastRecordEnterAnimation();
						addNewHabit();
					}else
						makePopupWindow(MainActivity.this);
				}
			}
		});
        mainFastRecordPlus = (ImageView) findViewById(R.id.ib_mainFastRecordPlus);
        ImageButton mainBackToTop = (ImageButton) findViewById(R.id.ib_mainBackToTop);
        mainBackToTop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listView.setSelectionFromTop(0, 0); // 回到ListView顶部
			}
		});
		add = (ImageButton) findViewById(R.id.ib_addNew);
		add.setOnClickListener(onButtonClicked);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, 
					int arg2, long rowId) {
//					if(moved == true && flag % 2 == 0){
//						flag++;
//					}
				if(moved == true){
					
				}
				if(moved == false){
					//flag++;
					Intent i = new Intent();
					// 把对应的title传到习惯详情去
					System.out.println(rowId + "---" + arg2);
					String uuid = listData.get(arg2 - 1).get(UUID).toString(); // arg2 - 1这个值有待考虑
					System.out.println(uuid);
					i.putExtra(UUID, uuid);
					i.setClass(MainActivity.this, 
							HabitDetailsActivity.class);
					startActivityForResult(i, REQUEST_HABIT_DETAILS);
				}
				overridePendingTransition(R.anim.enter_habit_details,
							R.anim.exit_habit_details);
			}
		});
		share = (ImageButton) findViewById(R.id.ib_mainShare);
		share.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(listData.size() != 0) {
					Intent i = new Intent();
					// TODO 把对应的title传到分享去，仅测试用
					
					String value = listData.get(0).get(UUID).toString();
					i.putExtra(UUID, value);
					i.setClass(MainActivity.this, 
							ShareCenterActivity.class);
					startActivityForResult(i, REQUEST_SHARE);
				}
			}
		});
		/*弹出窗口*/
		View popupWindowView = inflater.inflate(
				R.layout.popup_window, null);
		popupWindowList = (ListView) popupWindowView.findViewById(R.id.lv_mainFastRecord);
		popupWindowList.setAdapter(popupAdapter);
		pw = new PopupWindow(popupWindowView, LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		pw.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				showMainFastRecordExitAnimation();
			}
		});
		pw.setAnimationStyle(R.style.PopupAnimation);
		pw.setWidth(213);
		pw.setOutsideTouchable(true);
		pw.setFocusable(true);
		pw.setTouchable(true);
		ColorDrawable dw = new ColorDrawable(-000000);
		pw.setBackgroundDrawable(dw);
		popupWindowView.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO 截取菜单和返回事件
				switch (keyCode) {
				case KeyEvent.KEYCODE_MENU:
					if(pw.isShowing()) {
						showMainFastRecordExitAnimation();
						pw.dismiss();
					}
					break;
				case KeyEvent.KEYCODE_BACK:
					System.out.println("back pressed");
					if(pw.isShowing()) {
						showMainFastRecordExitAnimation();
						pw.dismiss();
					}
				default:
					break;
				}
				return false;
			}
		});
		popupWindowList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 监听快速记录菜单的点击事件
				if(arg2 == popuplistData.size() - 1) {
					addNewHabit();
				} else {
					String uuid = popuplistData.get(arg2).get(UUID).toString();
					System.out.println(uuid);
					Intent i = new Intent();
					i.putExtra(UUID, uuid);
					i.setClass(MainActivity.this, 
							RecordHabitActivity.class);
					startActivityForResult(i, REQUEST_RECORD);
					overridePendingTransition(R.anim.enter_habit_details, R.anim.exit_habit_details);
				}
			}
		});
	}
	
//	 @Override
//	    public boolean dispatchTouchEvent(MotionEvent ev){
//	        super.dispatchTouchEvent(ev);
//	        return true;
//	    }

//	public boolean dispatchTouchEvent(MotionEvent ev){    
//		 if(moved == true){
//			 this.mScrollLayout.onTouchEvent(ev);//在这里先处理下你的手势左右滑动事件     
////			 Habit_ListView.onTouchEvent(ev);//return super.dispatchTouchEvent(ev); 
//			 //Habit_ListView.setItemChecked(position, value)
//			 //Habit_ListView.setFocusable(true);Habit_ListView.setFocusableInTouchMode(true);
//			//	 return listView.onTouchEvent(ev);
////			 mScrollLayout.snapToScreen(1);
//			 return true;
//		 }
////		 if(onTouchEvent(ev) == true){
////			 return true;
////		 }
//		 //Habit_ListView.onTouchEvent(ev);
//		 Log.e(TAG, "qusiquiqusi"); 
//		 //listView.onTouchEvent(ev);
//		 mScrollLayout.onTouchEvent(ev);
//		 //return false;
//		 return super.dispatchTouchEvent(ev);
//	 }
	
	/**响应屏幕的触摸事件*/
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		  // Log.e(TAG, "test"); 
		  // mScrollLayout.snapToScreen(0);
		if(pw != null && pw.isShowing()) {
			System.out.println("touch event invoked");
			showMainFastRecordExitAnimation();
		}
		// return mGestureDetector.onTouchEvent(event); 
		return super.onTouchEvent(event);
	}
	
	
//	public boolean onInterceptTouchEvent(MotionEvent event){
//		return false;
//		
//	}
	/**
	 * 弹出窗口的方法
	 * @param Context context
	 * */
	protected void makePopupWindow(Context context) {
		if(habitCount == 0 && popuplistData.size() == 0) {
			HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("iv_mainFastRecordTag", R.drawable.tag_special_rect);
    		map.put("tv_mainFastRecordTitle", "+添加习惯");
    		map.put("iv_mainFastRecordState", R.drawable.main_fast_record_add);
    		popuplistData.add(map);
    		popupAdapter.notifyDataSetChanged();
		} else if(habitCount == 0 && popuplistData.size() == 1) {
			
		} else {
			getAllHabits();
		}
		pw.showAtLocation(findViewById(R.id.main), 
				Gravity.LEFT|Gravity.BOTTOM, 33, 33);
		// 左下角的动画
		showMainFastRecordEnterAnimation();
	}

	/**显示左下角button的进入动画*/
	private void showMainFastRecordEnterAnimation() {
		final Animation plusAnim = AnimationUtils.loadAnimation(
				MainActivity.this, 
				R.anim.main_fast_record_enter);
		mainFastRecordPlus.startAnimation(plusAnim);
	}
	
	/**显示左下角button的退出动画*/
	private void showMainFastRecordExitAnimation() {
		final Animation plusAnim = AnimationUtils.loadAnimation(
				MainActivity.this, 
				R.anim.main_fast_record_exit);
		mainFastRecordPlus.clearAnimation();
		mainFastRecordPlus.startAnimation(plusAnim);
	}

	/**
	 * 处理时间
	 * */
	private String processTime() {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		String processedTime = "";
		if(minute < 10 )
			processedTime = hour + ":" + "0" + minute;
		else {
			processedTime = hour + ":" + minute;
		}
		return processedTime;
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
	 * 根据数据处理右上角显示的成功率
	 * */
	private String processAllMonthlyRateNum() {
		// 修改格式为百分比
		DecimalFormat df = new DecimalFormat("##%");
		String temp = df.format(successRate);
		return temp;
	}

	/**
	 * 获得所有的习惯
	 * */
	private void getAllHabits() {
		HabitDatabase h = new HabitDatabase(this);
    	if(habitCount != popuplistData.size() - 1) {
    		SQLiteDatabase db = h.getWritableDatabase();
    		Cursor c = db.rawQuery("select * from " + SINGLE_HABIT_STATISTICS, null);
    		startManagingCursor(c);
    		popuplistData.removeAll(popuplistData);
        	for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
        		HashMap<String, Object> map = new HashMap<String, Object>();
        		String uuid = c.getString(13);
        		map.put(UUID, uuid);
        		String title = c.getString(1);
    			map.put("tv_mainFastRecordTitle", title);
    			int tag = c.getInt(12);
    			map.put("iv_mainFastRecordTag", getTagRectIDFromTag(tag));
    			int inTState = c.getInt(3);
    			map.put("iv_mainFastRecordState", getIDFromState(inTState));
        		popuplistData.add(map);
        	}
        	/*额外加一行添加习惯*/
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("iv_mainFastRecordTag", R.drawable.tag_special_rect);
    		map.put("tv_mainFastRecordTitle", "+添加习惯");
    		map.put("iv_mainFastRecordState", R.drawable.main_fast_record_add);
        	popuplistData.add(map);
    	}
    	h.close();
	}

	/**根据当前记录状态显示铅笔图片的颜色*/
	private int getIDFromState(int inTState) {
		int statePicID = 0;
		switch (inTState) {
		case HABIT_STATE_NOT_RECORDED:
			statePicID = R.drawable.wait_4_record;
			break;
		default:
			statePicID = R.drawable.recorded;
			break;
		}
		return statePicID;
	}

	private int getTagRectIDFromTag(int tag) {
		int id = 0;
		switch (tag) {
		case RED_TAG:
			id = R.drawable.tag1_rect;
			break;
		case ORANGE_TAG:
			id = R.drawable.tag2_rect;		
			break;
		case YELLOW_TAG:
			id = R.drawable.tag3_rect;
			break;
		case GREEN_TAG:
			id = R.drawable.tag4_rect;
			break;
		case DARK_GREEN_TAG:
			id = R.drawable.tag5_rect;
			break;
		case PURPLE_TAG:
			id = R.drawable.tag6_rect;
			break;
		case LIGHT_PURPLE_TAG:
			id = R.drawable.tag7_rect;
			break;
		default:
			id = R.drawable.tag_special_rect;
			break;
		}
		return id;
	}

	private OnClickListener onButtonClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ib_addNew:
				//addNewHabit();
				if(moved == false){
					moved = true;
					mScrollLayout.snapToScreen(0);
				}
				else{
					moved = false;
					mScrollLayout.snapToScreen(1);
				}
				break;
			default:
				break;
			}
		}
	};

	protected void addNewHabit() {
		Intent i = new Intent(MainActivity.this, 
				AddHabitActivity.class);
		startActivityForResult(i, REQUEST_ADD_HABIT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_ADD_HABIT:
			switch (resultCode) {
			case RESULT_OK:
				refreshView(); // 刷新界面
				break;
			case RESULT_CANCELED:
				// nothing
				break;
			default:
				break;
			}
			break;
		case REQUEST_HABIT_DETAILS:
			switch (resultCode) {
			case RESULT_OK:
				refreshView(); // 刷新界面
				break;
			case RESULT_CANCELED:
				refreshView();
				break;
			default:
				break;
			}
			break;
		case REQUEST_RECORD:
			switch (resultCode) {
			case RESULT_OK:
				refreshView();
				break;
			case RESULT_CANCELED:
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 所有刷新界面的代码
	 * */
	private void refreshView() {
		Intent intent = new Intent(this, MainActivity.class);
		finish();
		startActivity(intent);
	}
	
	/**@deprecated*/
	private void refreshView(int temp) {
		/*隐藏正在显示的popupWindw*/
		if(pw.isShowing()) {
			getAllHabits();
			popupAdapter.notifyDataSetChanged();
			pw.update();
		}
		/* listView部分的刷新 */	
		HabitDatabase h = new HabitDatabase(this);
		Bundle allHabitsLog = new Bundle(h.queryAll());
		if(allHabitsLog.size() != listData.size()) {
			// TODO 更新不相同的那一部分就行了
			System.out.println("system.out:not equal");
			for(int i = allHabitsLog.size() - listData.size() - 1; i > -1; 
					i--) {
				System.out.println("system.out:listData");
	         	Map<String, Object> map = new HashMap<String, Object>();
	        	String key = "temp"  + i;
	        	Bundle b = new Bundle(allHabitsLog.getBundle(key));
	        	String words = b.getString(WORDS);
	        	map.put("tv_detailWords", words /*记录的一句话*/ );
	        	// 得到图片tag并转成图片
	        	int tag = b.getInt(TAG);
	        	int currentState = b.getInt(CURRENT_STATE);
	        	int d = getResIDFromTagAndState(tag, currentState);
	            map.put("iv_detailState", d /*习惯的图片状态*/ );
	            String stateTitle = processDetailState(currentState, 
	            		b.getString(TITLE));
	            map.put("tv_detailStateTitle", stateTitle /*习惯的文字状态、内容、这个习惯*/ );
	            String timestamp = b.getString(TIMESTAMP);
	            timestamp = processTimestamp(timestamp);
	            map.put("tv_timestamp",  /*处理过的习惯时间戳*/ timestamp);
	            map.put(TITLE, b.getString(TITLE));
	            map.put(UUID, b.getString(UUID));
	        	listData.add(0, map);
	        }
		}
		
    	SQLiteDatabase db = h.getWritableDatabase();
    	Cursor sideListCursor = db.rawQuery("select * from " + ALL_HABITS, null);
    	startManagingCursor(sideListCursor);
    	if(sideListCursor.getCount() != sideListData.size()) {
    		sideListCursor.moveToFirst();
    		sideListData.remove(sideListData.size() - 1); // 除去最后两条内容
    		sideListData.remove(sideListData.size() - 1);
    		sideListCursor.moveToPosition(sideListData.size() - 1);
    		while(sideListCursor.moveToNext()) {
        		HashMap<String, Object> map = new HashMap<String, Object>();
    			// get from database
    			map.put("list_Image", R.drawable.triangle);
    			map.put("list_Words", sideListCursor.getString(1));
    			map.put(UUID, sideListCursor.getString(2));
    			sideListData.add(map);    		
        	}
    		/*得到sideListItemData的末尾内容*/
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
    	}
		
		/* listView部分的刷新 */
		/*HabitDatabase h = new HabitDatabase(this);
		SQLiteDatabase db = h.getReadableDatabase();
		Cursor c = db.query(ALL_HABITS_LOG, null, null, 
				null, null, null, "_id DESC");
		c.moveToFirst();
		Map<String, Object> map = new HashMap<String, Object>();
    	String words = c.getString(5);
    	map.put("tv_detailWords", words);
    	int tag = c.getInt(2);
    	int d = getResIDFromTag(tag);// 得到图片tag并转成图片
        map.put("iv_detailState", d);
        String stateTitle = processDetailState(c.getInt(4), 
        		c.getString(1));
        map.put("tv_detailStateTitle", stateTitle);
        String timestamp = c.getString(6);
        timestamp = processTimestamp(timestamp);
        map.put("tv_timestamp",timestamp);
        c.close();
        int addToFirst = 0;
    	listData.add(addToFirst, map);*/
		adapter.notifyDataSetChanged();
		sideListAdapter.notifyDataSetChanged();
		/* 头部分的内容刷新 */
		habitCount = h.queryTotalCount();
		float[] count = h.queryStatistics();
		successCount = (int)count[0];
		failCount = (int)count[1];
		waitCount = (int)count[2];
		successRate = count[3];
		statistic1.setText(habitCount + "");
		statistic2.setText(successCount + "");
		statistic3.setText(failCount + "");
		statistic4.setText(waitCount + "");
		statistic5.setText(habitCount + "");
		monthlyRateNum.setText(processAllMonthlyRateNum());
		time.setText(processTime());
		h.close();
	}
	
	@Override
	public void onBackPressed() {
		// show a dialog to inform that everything will lose without saving
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit")
				.setMessage("Are you sure you want to exit?\nUnsaved data will lose")// add a message for the dialog
				.setCancelable(true)// the user cannot close the dialog with the back button
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {//set...Button()
		           public void onClick(DialogInterface dialog, int id) {
		                finish(); // exit the activity
		           }
		       })	
		       	.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();// return to the activity
		           }
		       });
		AlertDialog alert = builder.create();// create the dialog
		alert.show();
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
				//moved = true;
				showExit(this);
			} else if(moved == true){
				moved = false;
				mScrollLayout.snapToScreen(1);
				//listView.onInterceptTouchEvent(event);
				//listView.setItemsCanFocus(false);
				//listView.setFocusableInTouchMode(false);
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
	
	//提示退出
	public static void showExit(final Activity a){
		Dialog alertDialog = new AlertDialog.Builder(a)
		.setTitle("退出程序")
		.setMessage("是否退出程序")
		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				System.exit(0);
			}
		}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				//listView.setFocusableInTouchMode(true);
			}
		}).create();  //创建对话框
		alertDialog.show(); // 显示对话框
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		//refreshView();
		if(pw.isShowing()) {
			pw.dismiss();
		}
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		refreshView();
	}
	
	@Override
	protected void onDestroy() {
		this.unbindService(conn);
        super.onDestroy();
	}
	
	/*private class refreshData extends AsyncTask<Integer, Integer, String> {

		@Override
		protected void onPreExecute() {
			CharSequence text = "刷新习惯状态中...";
			Toast.makeText(getApplicationContext(), text , Toast.LENGTH_SHORT);
			final Animation anim = AnimationUtils.loadAnimation(AGoodHabitPerMonthActivity.this, R.anim.rotation);
			mainFastRecordPlus.startAnimation(anim);
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Integer... params) {
			HabitDatabase h = new HabitDatabase(AGoodHabitPerMonthActivity.this);
			h.updateFromService();
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			CharSequence text = "习惯状态更新完毕！";
			Toast.makeText(getApplicationContext(), text , Toast.LENGTH_SHORT);
			mainFastRecordPlus.clearAnimation();
			super.onPostExecute(result);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onCancelled() {
			//super.onCancelled();无法取消
		}
	}*/
}