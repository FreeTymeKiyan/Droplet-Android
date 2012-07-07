package org.dian.kiyan.Activities;

import static org.dian.kiyan.Constants.Constants.ALL_HABITS;
import static org.dian.kiyan.Constants.Constants.ALL_HABITS_LOG;
import static org.dian.kiyan.Constants.Constants.AUTHORIZATION;
import static org.dian.kiyan.Constants.Constants.RENREN_API_KEY;
import static org.dian.kiyan.Constants.Constants.RENREN_SECRET_KEY;
import static org.dian.kiyan.Constants.Constants.REREN_APP_ID;
import static org.dian.kiyan.Constants.Constants.UPDATE_SINA_STATE;
import static org.dian.kiyan.Constants.Constants.UUID;
import static org.dian.kiyan.Constants.Constants.WEIBO_APP_SECRET;
import static org.dian.kiyan.Constants.Constants.WEIBO_AUTHORIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dian.kiyan.R;
import org.dian.kiyan.Databases.HabitDatabase;
import org.dian.kiyan.Databases.MySimpleAdapter;
import org.dian.kiyan.ReceiveBroadcast.AlarmReceiver;

import com.renren.api.connect.android.Renren;
import com.weibo.net.AccessToken;
import com.weibo.net.Weibo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.renren.api.connect.android.Renren;

public class MainPrefActivity extends Activity{
	/**标记目前已有几个习惯*/
	private int flag = 0;
	/**习惯管理中的文字和按钮*/
	private TextView habit_0;
	private TextView habit_1;
	private TextView habit_2;
	private TextView habit_3;
	private TextView habit_4;
	private TextView habit_5;
	private TextView habit_6;
	private Button habit_button_0;
	private Button habit_button_1;
	private Button habit_button_2;
	private Button habit_button_3;
	private Button habit_button_4;
	private Button habit_button_5;
	private Button habit_button_6;
	
	private String User_Id[];
	/**得到习惯管理相关的内容*/
	private RelativeLayout relative;
	/**设置界面中的list列表*/
	private ListView reference_habit;
	/**设置界面中的list的适配器*/
	private MySimpleAdapter reference_Adapter; 
	/**设置界面中的list的数据源*/
	private List<Map<String, Object>> reference_ListData 
			= new ArrayList<Map<String, Object>>();
	
	/**返回上一菜单的按钮*/
	private ImageButton reference_return;
	/**清除微博数据的按钮*/
	private Button weibo_clean;
	/**清除人人数据的按钮*/
	private Button renren_clean;
	/**关于*/
	private TextView about;
	/**版本检测*/
	private TextView Version_Detect;
	/**新浪的授权状态*/
	private boolean sinaAuthorization = false;
	/**微博的SDK封装类*/
	private Weibo weibo= Weibo.getInstance();
	/**人人SDK的封装类*/
	private Renren mRenren;
	//private String Accesstoken = null;
	//private AccessTokenManager avc;
//	/**用于修改sharedpreference中人人和微博中的数据*/
//	SharedPreferences settings = getSharedPreferences(AUTHORIZATION, 
//			MODE_WORLD_WRITEABLE);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference);
        User_Id = new String[7];
        initView();
        prepareData();
        removeView();
        mRenren = new Renren(RENREN_API_KEY, RENREN_SECRET_KEY, 
				REREN_APP_ID, this);
    }    
    /**初始化界面*/
    private void initView(){
    	//找到习惯管理中各部分的标签
    	habit_0 = (TextView) this.findViewById(R.id.habit_text_0);
    	habit_button_0 = (Button) this.findViewById(R.id.habit_button_0);
    	habit_button_0.setText("放弃习惯");
    	habit_1 = (TextView) this.findViewById(R.id.habit_text_1);
    	habit_button_1 = (Button) this.findViewById(R.id.habit_button_1);
    	habit_button_1.setText("放弃习惯");
    	habit_2 = (TextView) this.findViewById(R.id.habit_text_2);
    	habit_button_2 = (Button) this.findViewById(R.id.habit_button_2);
    	habit_button_2.setText("放弃习惯");
    	habit_3 = (TextView) this.findViewById(R.id.habit_text_3);
    	habit_button_3 = (Button) this.findViewById(R.id.habit_button_3);
    	habit_button_3.setText("放弃习惯");
    	habit_4 = (TextView) this.findViewById(R.id.habit_text_4);
    	habit_button_4 = (Button) this.findViewById(R.id.habit_button_4);
    	habit_button_4.setText("放弃习惯");
    	habit_5 = (TextView) this.findViewById(R.id.habit_text_5);
    	habit_button_5 = (Button) this.findViewById(R.id.habit_button_5);
    	habit_button_5.setText("放弃习惯");
    	habit_6 = (TextView) this.findViewById(R.id.habit_text_6);
    	habit_button_6 = (Button) this.findViewById(R.id.habit_button_6);
    	habit_button_6.setText("放弃习惯");
    	
    	habit_button_0.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[0]);
				    	h.close();
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[0]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			}
		});
    	
    	habit_button_1.setOnClickListener(new OnClickListener() {			
    		@Override
			public void onClick(View v) {
				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[1]);
				    	h.close();
				    	//得到可写的SQLiteDatabase对象
						//h.delete(User_Id[0]);
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[1]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			
			}
		});
    	
    	habit_button_2.setOnClickListener(new OnClickListener() {			
    		@Override
			public void onClick(View v) {
				//清除对应习惯的数据

				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[2]);
				    	h.close();
				    	//得到可写的SQLiteDatabase对象
						//h.delete(User_Id[0]);
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[2]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			
			}
		});
    	
    	habit_button_3.setOnClickListener(new OnClickListener() {			
    		@Override
			public void onClick(View v) {
				//清除对应习惯的数据

				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[3]);
				    	h.close();
				    	//得到可写的SQLiteDatabase对象
						//h.delete(User_Id[0]);
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[3]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			
			}
		});
    	
    	habit_button_4.setOnClickListener(new OnClickListener() {			
    		@Override
			public void onClick(View v) {
				//清除对应习惯的数据

				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[4]);
				    	h.close();
				    	//得到可写的SQLiteDatabase对象
						//h.delete(User_Id[0]);
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[4]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			
			}
		});
    	
    	habit_button_5.setOnClickListener(new OnClickListener() {			
    		@Override
			public void onClick(View v) {
				//清除对应习惯的数据

				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[5]);
				    	h.close();
				    	//得到可写的SQLiteDatabase对象
						//h.delete(User_Id[0]);
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[5]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			
			}
		});
    	
    	habit_button_6.setOnClickListener(new OnClickListener() {			
    		@Override
			public void onClick(View v) {
				//清除对应习惯的数据

				//清除对应习惯的数据
				Dialog alertDialog = new AlertDialog.Builder(MainPrefActivity.this)
				.setTitle("是否删除该习惯")
				.setMessage("是否删除")
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						HabitDatabase h = new HabitDatabase(MainPrefActivity.this);
				    	h.delete(User_Id[6]);
				    	h.close();
				    	//得到可写的SQLiteDatabase对象
						//h.delete(User_Id[0]);
				    	MainPrefActivity.this.removeView();
				    	MainPrefActivity.this.setContentView(R.layout.preference);
				    	initView();
				        prepareData();
				        removeView();
				        
				        removeClock(User_Id[6]);
					}
				}).setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						//listView.setFocusableInTouchMode(true);
					}
				}).create();  //创建对话框
				alertDialog.show(); // 显示对话框
			
			}
		});
    	
    	//得到习惯管理的relativeLayout从而去掉其内部的空布局
    	relative = (RelativeLayout) this.findViewById(R.id.reference__tab_habit_manager);

    	reference_return = (ImageButton) this.findViewById(R.id.reference_return);
    	reference_return.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//退回上一个界面
				reference_return.setBackgroundResource(R.drawable.preference_back);
				MainPrefActivity.this.finish();
			}
		});
    	/**更换微博的消息响应*/
    	weibo_clean = (Button) this.findViewById(R.id.reference_weibo_Words);
    	weibo_clean.setText("更换账号");
    	weibo_clean.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 清除微博中的相关记录 
				SharedPreferences settings = getSharedPreferences(AUTHORIZATION, 
						MODE_WORLD_WRITEABLE);
//				String token = "";
//				AccessToken at = new AccessToken(null);
//				weibo.setAccessToken(at);
				Editor editor = settings.edit(); // 改变全局偏好设置的状态
				editor.remove(WEIBO_AUTHORIZE);
				editor.remove(WEIBO_APP_SECRET);
				editor.remove(AUTHORIZATION);
//				editor.putBoolean(WEIBO_AUTHORIZE, sinaAuthorization);
//				editor.putString("weibo access token", token);
				//editor.clear();
				editor.commit(); // 提交修改
				
				Toast.makeText(MainPrefActivity.this, "新浪微博登录信息已清除", 
						Toast.LENGTH_SHORT).show();
			}
		});
    	/**更换人人账号的消息响应*/
    	renren_clean = (Button) this.findViewById(R.id.reference_renren_Words);
    	renren_clean.setText("更换账号");
    	renren_clean.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				// 清除renren中的相关记录 
				clean_Renren(MainPrefActivity.this);
				Toast.makeText(MainPrefActivity.this, "人人登录信息已清除", 
						Toast.LENGTH_SHORT).show();
			}
		});
    	/**关于信息的消息响应*/
    	about = (TextView) this.findViewById(R.id.reference_about);
    	about.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 调出关于信息 
				new AlertDialog.Builder(MainPrefActivity.this)
	            .setMessage("卓普，让我们卓越每个普通人！")
	            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            }).show();
			}
		});
    	/**版本检测的消息响应*/
    	Version_Detect = (TextView) this.findViewById(R.id.version_detect);
    	Version_Detect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 检测相应程序的版本
				Toast.makeText(MainPrefActivity.this, "目前是1.0版本", 
						Toast.LENGTH_SHORT).show();
			}
		});   	
    }
    
    private void prepareData() {
    	/**获得认证相关的数据，在消息响应中进行修改*/
    	//settings = getSharedPreferences(AUTHORIZATION, MODE_WORLD_WRITEABLE);
    	//settings;
    	HabitDatabase h = new HabitDatabase(this);
    	SQLiteDatabase db = h.getWritableDatabase();
    	Cursor sideListCursor = db.rawQuery("select * from " + ALL_HABITS, null);
    	startManagingCursor(sideListCursor);
    	flag = 0;
    	while(sideListCursor.moveToNext()) {
			String words = sideListCursor.getString(1);
			//User_Id[flag] = sideListCursor.getString(2);
			switch(flag){
				case 0: habit_0.setText(words);
						User_Id[0] = sideListCursor.getString(2);
						break;
				case 1: habit_1.setText(words);
						User_Id[1] = sideListCursor.getString(2);
						break;
				case 2: habit_2.setText(words);
						User_Id[2] = sideListCursor.getString(2);
						break;
				case 3: habit_3.setText(words);
						User_Id[3] = sideListCursor.getString(2);
						break;
				case 4: habit_4.setText(words);
						User_Id[4] = sideListCursor.getString(2);
						break;
				case 5: habit_5.setText(words);
						User_Id[5] = sideListCursor.getString(2);
						break;
				case 6: habit_6.setText(words);
						User_Id[6] = sideListCursor.getString(2);
						break;
			}
			flag++;
    	}     
    	h.close();
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
    
    private void removeClock(String uuid){
    	SharedPreferences clockData = getSharedPreferences("User_Clock", 0);
        clockData.edit().remove(uuid).commit();
        Intent intent = new Intent(MainPrefActivity.this, AlarmReceiver.class);
        intent.addCategory(uuid);
        intent.setAction("org.dian.kiyan");
        PendingIntent cancelIntent = PendingIntent
        .getBroadcast(MainPrefActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(cancelIntent);
    }
    
    /**用于删去习惯内容中没有内容的文字和按钮*/
    private void removeView(){
    	int count = relative.getChildCount();
    	for(int i = count - 1;i > 3 * flag - 1; i--){
    		relative.removeViewAt(i);
    	}
    }
    
    public void clean_Renren(Context context){
    	mRenren.logout(context);
    }
}
