package org.dian.kiyan.Activities;

import static org.dian.kiyan.Constants.Constants.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;

import org.dian.kiyan.R;
import org.dian.kiyan.Databases.Habit;
import org.dian.kiyan.Databases.HabitDatabase;
import org.dian.kiyan.ReceiveBroadcast.AlarmReceiver;
import org.dian.kiyan.Utils.Utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;

/**
 * 修改习惯信息的Activity
 * */
public class HabitPrefActivity extends Activity {
	/* 私有常量 */
	/**从相册选择图片*/
	private static final int CHOOSE_PIC_FROM_ALBUM = 0;
	/**从相机选择图片*/
	private static final int CHOOSE_PIC_FROM_CAMERA = 1;
	/* 控件 */
	/**确认创建习惯*/
	private ImageButton addHabit;
	/**取消创建习惯*/
	private ImageButton cancel;
	/**选择单个习惯页背景图片的Button*/
	private Button chooseBackground;
	/**习惯的标签之一*/
	private ImageView habitTag1;
	/**习惯的标签之二*/
	private ImageView habitTag2;
	/**习惯的标签之三*/
	private ImageView habitTag3;
	/**习惯的标签之四*/
	private ImageView habitTag4;
	/**习惯的标签之五*/
	private ImageView habitTag5;
	/**习惯的标签之六*/
	private ImageView habitTag6;
	/**习惯的标签之七*/
	private ImageView habitTag7;
	/* 变量 */
	/**返回到主界面的Intent*/
	private Intent i = new Intent();
	/**用来暂存习惯的背景图，默认为白色*/
	private Uri background = Uri.parse("android.resource://" +
			"org.dian.kiyan/drawable-hdpi/test_bg.png"); 
	/**用来暂存习惯的标签，默认为白色*/
	private int tag = RED_TAG;
	
	/**创建习惯待分享的内容*/
	private String words = "";
	/**Activity自身的引用*/
	private HabitPrefActivity mAddHabit = this;
	
	/**时钟相关的参数*/
	Calendar calendar;
	/**控制闹钟是否开启*/
	private Button timeSwitch;
	/**控制闹钟播放的时间*/
	private Button timeSettings;
	/**To Control the Clock*/
	private boolean Clock_Settings = false;
	private String clock_time;
	AlarmManager am;
		/**习惯的ID*/
	private String uuid;
	private ContentValues statics;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.habit_preference);
		/* 获取闹钟管理的实例 */
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = this.getIntent();
		if(intent.hasExtra(UUID)){
			uuid = intent.getStringExtra(UUID);
		}else{
			System.out.println("no uuid in the intent");
		}
		prepareData();
		initView();
	}

	/**
	 * 将内容写到数据库中（输入情况合法时）
	 * */
	private void updateInHabitDatabase() {
		HabitDatabase habit = new HabitDatabase(this);
		ContentValues cv = new ContentValues();

		cv.put(TAG, tag);
		cv.put(BACKGROUND, background.toString());
		cv.put(UUID, uuid); // 习惯的全局唯一id
		
		Habit h = new Habit(cv);
		if(habit.update(h)) {
			habit.close();
			// 把uuid放到intent当中，返回的时候刷新界面
			i.putExtra(UUID, uuid);
		} else {
			Toast.makeText(HabitPrefActivity.this, 
					R.string.addHabitFal, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 根据UUID来查询相关信息
	 * */
	private void prepareData(){
		// 查询数据库
		HabitDatabase h = new HabitDatabase(this);
		statics = h.queryStatistics(uuid);
		background = Uri.parse(statics.getAsString(BACKGROUND));
		tag = statics.getAsInteger(TAG);
		SharedPreferences Clock_Save = this.getSharedPreferences("User_Clock", 0);
		String s = Clock_Save.getString(uuid, "109:00");
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		if(s.charAt(0) == '1'){
			Clock_Settings = true;
			clock_time = s.substring(1);
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(1, 3)));
			calendar.set(Calendar.MINUTE, Integer.parseInt(s.substring(4)));
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}else{
			Clock_Settings = false;
			clock_time = "09:00";
			calendar.set(Calendar.HOUR_OF_DAY, 9);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		
	}
	
	private void initView() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams
				.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 进入Activity不弹输入法
		// TODO 选择周期用下拉的方式
		timeSwitch = (Button) findViewById(R.id.hp_notificationState);
		if(Clock_Settings == true){
			timeSwitch.setText("开启");
		}
		else{
			timeSwitch.setText("关闭");
		}
		
		/**闹钟是否开启*/
		timeSwitch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(Clock_Settings == true){
					Clock_Settings = false;
					timeSwitch.setText("关闭");
				}else{
					Clock_Settings = true;
					timeSwitch.setText("开启");
				}
			}
		});
		
		/**闹钟的具体时间*/
		timeSettings = (Button) findViewById(R.id.hp_TimeDetail);
		timeSettings.setText(clock_time);
		timeSettings.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(HabitPrefActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								calendar.setTimeInMillis(System
										.currentTimeMillis());
								calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
								calendar.set(Calendar.MINUTE, minute);
								calendar.set(Calendar.SECOND, 0);
								calendar.set(Calendar.MILLISECOND, 0);
								String tmpS = "设置闹钟时间为" + format(hourOfDay)
										+ ":" + format(minute);
								String time_show = format(hourOfDay) + ":" + format(minute);
								timeSettings.setText(time_show);
							}
						}, mHour, mMinute, true).show();
				//timeSettings.setText(teps);
			}
		});
		
		addHabit = (ImageButton) findViewById(R.id.ib_hpTitleOk);
		addHabit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// add to the habit database 
				updateInHabitDatabase();
				return2Detail();
				finish();
			}
		});
		cancel = (ImageButton) findViewById(R.id.ib_hpTitleReturn);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		chooseBackground = (Button) findViewById(R.id.btn_hpChangePic);
		Bitmap mBitmap = null;
		try {
			mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
					background);
			mBitmap = createPic(mBitmap);
		} catch (FileNotFoundException e1) {
			mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_bg);
			mBitmap = createPic(mBitmap);
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// 把得到的图片绑定在控件上显示
		BitmapDrawable drawable = new BitmapDrawable(mBitmap);
		chooseBackground.setBackgroundDrawable(drawable);
		chooseBackground.setText("");
		chooseBackground.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 选相机or相册
				final CharSequence[] items = {"从相册中选择", "拍一张" };
				AlertDialog dlg = new AlertDialog.Builder(HabitPrefActivity.this)
						.setTitle("选择图片").setItems(items,
						new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, 
							int item) {
						// 这里item是根据选择的方式，
						// 在items数组里面定义了两种方式，
						// 拍照的下标为1所以就调用拍照方法
						if (item == CHOOSE_PIC_FROM_CAMERA) {
							Intent getImageByCamera = 
								new Intent("android.media.action.IMAGE_CAPTURE");
							startActivityForResult(getImageByCamera, 
									CHOOSE_PIC_FROM_CAMERA);
						} else {
							Intent getImage = 
								new Intent(Intent.ACTION_GET_CONTENT);
							getImage.addCategory(Intent.CATEGORY_OPENABLE);
							getImage.setType("image/jpeg");
							startActivityForResult(getImage, 
									CHOOSE_PIC_FROM_ALBUM);
						}
					}
				}).create();
				dlg.show();
			}
		});
		final Animation tagAnimation = AnimationUtils.loadAnimation(this, 
				R.anim.choose_tag);
		habitTag1 = (ImageView) findViewById(R.id.iv_hptag1);
		habitTag2 = (ImageView) findViewById(R.id.iv_hptag2);
		habitTag3 = (ImageView) findViewById(R.id.iv_hptag3);
		habitTag4 = (ImageView) findViewById(R.id.iv_hptag4);
		habitTag5 = (ImageView) findViewById(R.id.iv_hptag5);
		habitTag6 = (ImageView) findViewById(R.id.iv_hptag6);
		habitTag7 = (ImageView) findViewById(R.id.iv_hptag7);
		processTag();
		habitTag1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tag = RED_TAG;
				clearAllAnimation();
				habitTag1.startAnimation(tagAnimation);
			}
		});
		habitTag2.setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View v) {
				tag = ORANGE_TAG;
				clearAllAnimation();
				habitTag2.startAnimation(tagAnimation);
			}
		});
		habitTag3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tag = YELLOW_TAG;
				clearAllAnimation();
				habitTag3.startAnimation(tagAnimation);
			}
		});
		habitTag4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tag = GREEN_TAG;
				clearAllAnimation();
				habitTag4.startAnimation(tagAnimation);
			}
		});
		habitTag5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				tag = DARK_GREEN_TAG;
				clearAllAnimation();
				habitTag5.startAnimation(tagAnimation);
			}
		});
		habitTag6.setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View arg0) {
				tag = LIGHT_PURPLE_TAG;
				clearAllAnimation();
				habitTag6.startAnimation(tagAnimation);
			}
		});
		habitTag7.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				tag = PURPLE_TAG;
				clearAllAnimation();
				habitTag7.startAnimation(tagAnimation);
			}
		});
		
	}
	
	private void processTag() {
		final Animation tagAnimation = AnimationUtils.loadAnimation(this, 
				R.anim.choose_tag);
		switch (tag) {
		case RED_TAG:
			habitTag1.startAnimation(tagAnimation);
			break;
		case ORANGE_TAG:
			habitTag2.startAnimation(tagAnimation);
			break;
		case YELLOW_TAG:
			habitTag3.startAnimation(tagAnimation);
			break;
		case GREEN_TAG:
			habitTag4.startAnimation(tagAnimation);
			break;
		case DARK_GREEN_TAG:
			habitTag5.startAnimation(tagAnimation);
			break;
		case LIGHT_PURPLE_TAG:
			habitTag6.startAnimation(tagAnimation);
			break;
		case PURPLE_TAG:
			habitTag7.startAnimation(tagAnimation);
			break;
		default:
			break;
		}
	}

	/**
	 * 清除标签上所有的动画
	 * */
	protected void clearAllAnimation() {
		habitTag1.clearAnimation();
		habitTag2.clearAnimation();
		habitTag3.clearAnimation();
		habitTag4.clearAnimation();
		habitTag5.clearAnimation();
		habitTag6.clearAnimation();
		habitTag7.clearAnimation();
	}
	
	/**
	 * 添加完毕，返回到主界面
	 * */
	protected void return2Detail() {
		SharedPreferences Clock_Save = this.getSharedPreferences("User_Clock", 0);
		Intent intent = new Intent();
		intent.putExtra(UUID, uuid);
		intent.setClass(mAddHabit, AlarmReceiver.class);
		intent.setAction("org.dian.kiyan");
		intent.addCategory(uuid);
		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(mAddHabit, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		if(Clock_Settings == true){
			//Clock_Flag
			Editor editor = Clock_Save.edit();
			editor.putString(uuid, "1" + timeSettings.getText().toString()).commit();
			/* 设置闹钟 */
			am.cancel(pendingIntent);
//			am.set(AlarmManager.RTC_WAKEUP, calendar
//					.getTimeInMillis(), pendingIntent);
			/* 设置周期闹 */
			if(System.currentTimeMillis() > calendar.getTimeInMillis()){
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
			}
			am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (24 * 60 * 60 * 1000),
					pendingIntent);
		}else{
			Clock_Save.edit().putString(uuid, "009:00").commit();
			am.cancel(pendingIntent);
		}
		setResult(RESULT_OK, i);
		finish();
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
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ContentResolver resolver = getContentResolver();
		switch (requestCode) {
		case CHOOSE_PIC_FROM_ALBUM:
			if(data != null) {
				try {
					// 获得图片的uri
					background = data.getData();
					// 将图片内容解析成字节数组
					byte[] mContent = readStream(resolver
							.openInputStream(
							Uri.parse(background.toString())));
					// 将字节数组转换为ImageView可调用的Bitmap对象
					Bitmap myBitmap = getPicFromBytes(mContent, null);
					myBitmap = createPic(myBitmap);
					// 把得到的图片绑定在控件上显示
					BitmapDrawable drawable = new BitmapDrawable(myBitmap);
					chooseBackground.setBackgroundDrawable(drawable);
					chooseBackground.setText("");
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
			break;
		case CHOOSE_PIC_FROM_CAMERA:
			if(data != null) {
				try {
					background = data.getData();
					byte[] mContent = readStream(resolver
							.openInputStream(
							Uri.parse(background.toString())));
					// 将字节数组转换为ImageView可调用的Bitmap对象
					Bitmap myBitmap = getPicFromBytes(mContent, null);
					myBitmap = createPic(myBitmap);
					BitmapDrawable drawable = new BitmapDrawable(myBitmap);
					chooseBackground.setBackgroundDrawable(drawable);
					chooseBackground.setText("");
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 产生一个大小设定好的Bitmap
	 * */
	private Bitmap createPic(Bitmap myBitmap) {
		WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        System.out.println(screenHeight + " " + screenWidth);
		myBitmap = ThumbnailUtils.extractThumbnail(myBitmap, 
				screenWidth, screenHeight * 1 / 8, 
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return myBitmap;
	}

	/**
	 * 从byte得到bitmap的方法
	 * */
	public static Bitmap getPicFromBytes(byte[] bytes , BitmapFactory.Options opts ) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}

	/**
	 * 将输入流转化为byte的方法
	 * */
	public static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}
	
	private boolean checkNetworkInfo() {
        /*ConnectivityManager conMan = (ConnectivityManager) 
        		getSystemService(Context.CONNECTIVITY_SERVICE);
        // mobile 3G Data Network
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        // wifi
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
            return true;
        if(wifi==State.CONNECTED||wifi==State.CONNECTING)
            return true;
        return false;
        // startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面*/
		ConnectivityManager cm = (ConnectivityManager) 
				mAddHabit.getSystemService(Context.CONNECTIVITY_SERVICE);
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
	
	
	/** 格式化字符串(7:3->07:03) */
	private String format(int x) {
		String s = "" + x;
		if (s.length() == 1)
			s = "0" + s;
		return s;
	}
}