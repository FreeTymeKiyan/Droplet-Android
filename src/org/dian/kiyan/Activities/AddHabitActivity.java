package org.dian.kiyan.Activities;

import static org.dian.kiyan.Constants.Constants.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;

import org.dian.kiyan.R;
import org.dian.kiyan.Apis.Share2RenrenRunnable;
import org.dian.kiyan.Apis.Share2WeiboRunnable;
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
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
 * 新增习惯的Activity
 * 同时养成的习惯上限设为7
 * */
public class AddHabitActivity extends Activity {
	/* 私有常量 */
	/**从相册选择图片*/
	private static final int CHOOSE_PIC_FROM_ALBUM = 0;
	/**从相机选择图片*/
	private static final int CHOOSE_PIC_FROM_CAMERA = 1;
	/* 控件 */
	/**输入习惯周期*/
	private EditText habitT;
	/**输入习惯内容*/
	private EditText habitTitle;
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
	/**用来暂存习惯的内容*/
	private String title = "";
	/**用来暂存习惯的周期*/
	private int T;
	/**用来暂存习惯的背景图，默认为白色*/
	private Uri background = Uri.parse("android.resource://" +
			"org.dian.kiyan/drawable-hdpi/test_bg.png"); 
	/**用来暂存习惯的标签，默认为白色*/
	private int tag = RED_TAG;
	/**创建后分享到人人的状态*/
	private int shareToRenren = STATE_NOT_PUBLISH;
	/**创建后分享到微博的状态*/
	private int shareToWeibo = STATE_NOT_PUBLISH;
	/**从sharedpreference读取的微博授权状态*/
	private boolean weiboAuth;
	/**从sharedpreference读取的人人授权状态*/
	private boolean renrenAuth;
	/**分享到人人的图片按钮*/
	private ImageButton addRenren;
	/**分享到微博的图片按钮*/
	private ImageButton addWeibo;
	/**存储认证状态的preference*/
	private SharedPreferences settings;
	/**创建习惯待分享的内容*/
	private String words = "";
	/**处理刷新消息*/
	private Handler handler;
	/**Activity自身的引用*/
	private AddHabitActivity mAddHabit = this;
	/**自定义dialog中分享到人人的按钮*/
	private Button share2Renren;
	/**自定义dialog中分享到微博的按钮*/
	private Button share2Weibo;
	/**自定义dialog中退出按钮*/
	private Button cancelShare;
	/**时钟相关的参数*/
	Calendar calendar;
	/**控制闹钟是否开启*/
	private Button timeSwitch;
	/**控制闹钟播放的时间*/
	private Button timeSettings;
	/***/
	public String Clock_Flag;
	/**To Control the Clock*/
	private boolean Clock_Settings = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		seeTotalCount();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_habit);
		prepareData();
		initView();
	}
	
	private void prepareData() {
		// 从认证状态里改变人人和微博的状态
		settings = getSharedPreferences(AUTHORIZATION, 
				MODE_WORLD_WRITEABLE);
		weiboAuth = settings.getBoolean(WEIBO_AUTHORIZE, false);
		if(weiboAuth)
			shareToWeibo = STATE_PUBLISH;
		renrenAuth = settings.getBoolean(RENREN_AUTHORIZE, false);
		if(renrenAuth)
			shareToRenren = STATE_PUBLISH;
	}

	/**
	 * 看数据库中的习惯数是否已经达到7个
	 * 达到7个则关闭页面，不能再增加更多习惯
	 * */
	private void seeTotalCount() {
		HabitDatabase h = new HabitDatabase(this);
		int temp = h.queryTotalCount();
		if(temp > 6) {
			Toast.makeText(this, R.string.habitTotalAchieved, 
					Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	/**
	 * 将内容写到数据库中（输入情况合法时）
	 * */
	private void pushIntoHabitDatabase() {
		HabitDatabase habit = new HabitDatabase(this);
		Message msg = new Message();
		msg.what = WHEN_CREATE;
		ContentValues cv = new ContentValues();
		cv.put(TITLE, title);
		cv.put(DAYS, T);
		cv.put(TAG, tag);
		cv.put(BACKGROUND, background.toString());
		String uuid = Utils.getUUID();
		Clock_Flag = uuid; // TODO
		cv.put(UUID, uuid); // 习惯的全局唯一id
		if(renrenAuth)
			cv.put(SHARE_TO_RENREN, STATE_PUBLISH);
		if(weiboAuth)
			cv.put(SHARE_TO_WEIBO, STATE_PUBLISH);
		Habit h = new Habit(cv);
		if(habit.insert(h, msg, this)) {
			habit.close();
			// 把uuid放到intent当中，返回的时候刷新界面
			i.putExtra(UUID, uuid);
		} else {
			Toast.makeText(AddHabitActivity.this, 
					R.string.addHabitFal, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 判断所有输入的信息
	 * 如：Title不能为空，Title要控制长度，
	 * 周期不能为空，周期要从0到30
	 * 背景默认为白色，标签默认为没有
	 * */
	private boolean judgeValidity() {
		/*从edittext中获取对应的值*/
		title = habitTitle.getText().toString();
		String tempT = habitT.getText().toString();
		T = Integer.parseInt(tempT); // 从string得到整数值
		// System.out.println(T + ""); // 得到的是正确的周期值
		// TODO 判断title的长度，给用户提示
		// 周期的范围没给用户提示
		if(title.equals("") || tempT.equals("") || T < 0 || T > 30) {
			Toast.makeText(AddHabitActivity.this, 
					R.string.invalid, Toast.LENGTH_SHORT).show();
			return false;
		}else{
			return true;
		}
	}

	private void initView() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams
				.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 进入Activity不弹输入法
		// TODO 选择周期用下拉的方式？
		timeSwitch = (Button) findViewById(R.id.setting_on);
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
					Intent intent = new Intent(mAddHabit, AlarmReceiver.class);
					PendingIntent pendingIntent = PendingIntent.getBroadcast(
							AddHabitActivity.this, 0, intent, 0);
					AlarmManager am;
					/* 获取闹钟管理的实例 */
					am = (AlarmManager) getSystemService(ALARM_SERVICE);
					Clock_Settings = true;
					timeSwitch.setText("开启");
				}
			}
		});
		
		calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		/**闹钟的具体时间*/
		timeSettings = (Button) findViewById(R.id.time_detail);
		timeSettings.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(AddHabitActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								calendar.setTimeInMillis(System
										.currentTimeMillis());
								calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
								calendar.set(Calendar.MINUTE, minute);
								calendar.set(Calendar.SECOND, 0);
								calendar.set(Calendar.MILLISECOND, 0);
								String time_show = format(hourOfDay) + ":" + format(minute);
								timeSettings.setText(time_show);
							}
						}, mHour, mMinute, true).show();
				//timeSettings.setText(teps);
			}
		});
		habitT = (EditText) findViewById(R.id.et_T);
		habitTitle = (EditText) findViewById(R.id.et_habitTitle);
		habitTitle.requestFocus();
		habitTitle.addTextChangedListener(new TextWatcher() {
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
				selectionStart = habitTitle.getSelectionStart();
                selectionEnd = habitTitle.getSelectionEnd();
                if (temp.length() > TITLE_MAX_LENGTH) {
                    Toast.makeText(AddHabitActivity.this,
                            R.string.onTextChanged, Toast.LENGTH_SHORT)
                            .show();
                    s.delete(selectionStart-1, selectionEnd);
                    int tempSelection = selectionStart;
                    habitTitle.setText(s);
                    habitTitle.setSelection(tempSelection);
                }
			}
		});
		addHabit = (ImageButton) findViewById(R.id.ib_create);
		addHabit.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View v) {
				// judge validity first 
				if(judgeValidity()) {
					if(checkNetworkInfo()) {
						words = process(words);
						if(shareToWeibo == STATE_PUBLISH) {
							Thread t = new Thread(new Share2WeiboRunnable(words, 
									AddHabitActivity.this, mAddHabit, handler));
							t.start();
						}
						if(shareToRenren == STATE_PUBLISH) {
							Thread t = new Thread(new Share2RenrenRunnable(words, 
									AddHabitActivity.this, mAddHabit, handler));
							t.start();
						}
					} else {
						Toast.makeText(AddHabitActivity.this, R.string.noNetworkConnection, 
								Toast.LENGTH_SHORT).show();
					}
					if(!renrenAuth && !weiboAuth) { // 都认证了
						showShareDialog();
					} else if(shareToRenren == STATE_NOT_PUBLISH && 
							shareToWeibo == STATE_NOT_PUBLISH){
						showShareDialog();
					} else {
						// add to the habit database 
						pushIntoHabitDatabase();
						return2Main();
					}
				}
			}
		});
		cancel = (ImageButton) findViewById(R.id.ib_return);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		chooseBackground = (Button) findViewById(R.id.btn_choosePic);
		chooseBackground.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 选相机or相册
				final CharSequence[] items = {"从相册中选择", "拍一张" };
				AlertDialog dlg = new AlertDialog.Builder(AddHabitActivity.this)
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
		habitTag1 = (ImageView) findViewById(R.id.iv_tag1);
		habitTag2 = (ImageView) findViewById(R.id.iv_tag2);
		habitTag3 = (ImageView) findViewById(R.id.iv_tag3);
		habitTag4 = (ImageView) findViewById(R.id.iv_tag4);
		habitTag5 = (ImageView) findViewById(R.id.iv_tag5);
		habitTag6 = (ImageView) findViewById(R.id.iv_tag6);
		habitTag7 = (ImageView) findViewById(R.id.iv_tag7);
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
		addRenren = (ImageButton) findViewById(R.id.ib_addRenren);
		if(renrenAuth)
			addRenren.setImageResource(R.drawable.renren_icon_light);
		addRenren.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 是否授权，切换是否分享的状态
				if(renrenAuth) {
					if(shareToRenren == STATE_PUBLISH) {
						shareToRenren = STATE_NOT_PUBLISH;
						addRenren.setImageResource(R.drawable.renren_icon_dark);
					} else {
						shareToRenren = STATE_PUBLISH;
						addRenren.setImageResource(R.drawable.renren_icon_light);
					}
				} else {
					// TODO 跳转至授权页面
					Thread t = new Thread(new Share2RenrenRunnable(
							AddHabitActivity.this, mAddHabit, handler));
					t.start();
				}
			}
		});
		addWeibo = (ImageButton) findViewById(R.id.ib_addWeibo);
		if(weiboAuth)
			addWeibo.setImageResource(R.drawable.weibo_icon_light);
		addWeibo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 是否授权，切换是否分享的状态
				if(weiboAuth) {
					if(shareToWeibo == STATE_PUBLISH) {
						shareToWeibo = STATE_NOT_PUBLISH;
						addWeibo.setImageResource(R.drawable.weibo_icon_dark);
					} else {
						shareToWeibo = STATE_PUBLISH;
						addWeibo.setImageResource(R.drawable.weibo_icon_light);
					}
				} else {
					// TODO 跳转至授权页面
					Thread t = new Thread(new Share2WeiboRunnable( 
							AddHabitActivity.this, mAddHabit, handler));
					t.start();
				}
			}
		});
		handler = new MyHandler(Looper.getMainLooper());
	}
	
	/**处理待发送的习惯*/
	protected String process(String words) {
		StringBuilder sb = new StringBuilder();
		String temp = "";
		switch (T) {
		case 1:
			temp = "每天";
			break;
		case 2:
			temp = "每两天";
		case 7:
			temp = "每周";
		default:
			temp = "每" + T + "天";
			break;
		}
		sb.append("我要开始坚持")
				.append(temp)
				.append(title)
				.append("这个习惯。")
				.append("求监督！");
		return words = sb.toString();
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
	 * 显示创建后分享的Dialog
	 * 如果左下角点亮则不弹出，直接分享
	 * 如果左下角未点亮则弹出，提示分享
	 * */
	private void showShareDialog() {
		AlertDialog.Builder builder;
		Context mContext = AddHabitActivity.this;
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.after_create_dialog, 
				(ViewGroup) findViewById(R.id.rl_afterCreate));
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);
		builder.setCancelable(false);
		final AlertDialog alertdialog = builder.create();
		alertdialog.show();
		share2Renren = (Button) layout.findViewById(R.id.btn_share2Renren);
		share2Weibo = (Button) layout.findViewById(R.id.btn_share2Weibo);
		cancelShare = (Button) layout.findViewById(R.id.btn_cancelShare);
		share2Renren.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO share the plan to renren
				if(checkNetworkInfo()) {
					if(!renrenAuth) { // 没认证先认证
						Thread t = new Thread(new Share2RenrenRunnable(
								AddHabitActivity.this, mAddHabit, handler));
						t.start();
						// 认证完成功后该按钮失效
					} else if(renrenAuth && shareToRenren == STATE_NOT_PUBLISH){
						shareToRenren = STATE_PUBLISH;
						Toast.makeText(mAddHabit, "即将分享到人人", 
								Toast.LENGTH_SHORT).show();
						share2Renren.setEnabled(false);
					} else {
						
					}
				} else {
					Toast.makeText(mAddHabit, R.string.noNetworkConnection, 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		share2Weibo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO share the plan to weibo
				if(checkNetworkInfo()) {
					if(!weiboAuth) {
						Thread t = new Thread(new Share2WeiboRunnable( 
								mAddHabit, mAddHabit, handler));
						t.start();
						// TODO 认证完成后按钮失效
					} else if(weiboAuth && shareToWeibo == STATE_NOT_PUBLISH) {
						shareToWeibo = STATE_PUBLISH;
						Toast.makeText(mAddHabit, "即将分享到新浪微博", 
								Toast.LENGTH_SHORT).show();
						share2Weibo.setEnabled(false);
					} else {
						
					}
				} else {
					Toast.makeText(mAddHabit, R.string.noNetworkConnection, 
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		if(weiboAuth || renrenAuth) {
			cancelShare.setEnabled(true);
		} else {
			cancelShare.setEnabled(false);
		}
		cancelShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// add to the habit database 
				pushIntoHabitDatabase();
				if(checkNetworkInfo()) {
					if(renrenAuth) {
						Thread t = new Thread(new Share2RenrenRunnable(words, 
								AddHabitActivity.this, mAddHabit, handler));
						t.start();
					} 
					if(weiboAuth) {
						Thread t = new Thread(new Share2WeiboRunnable(words, 
								AddHabitActivity.this, mAddHabit, handler));
						t.start();
					}
					alertdialog.cancel();
					
					return2Main();
				} else {
					Toast.makeText(mAddHabit, R.string.noNetworkConnection, 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	/**
	 * 添加完毕，返回到主界面
	 * */
	protected void return2Main() {
		SharedPreferences Clock_Save = this.getSharedPreferences("User_Clock", 0);
		Editor editor = Clock_Save.edit();
		if(Clock_Settings == true){
			//Clock_Flag
			editor.putString(Clock_Flag, "1" + timeSettings.getText().toString()).commit();
			Intent intent = new Intent();
			intent.putExtra(UUID, Clock_Flag);
			intent.setClass(mAddHabit,AlarmReceiver.class);
			intent.setAction("org.dian.kiyan");
			intent.addCategory(Clock_Flag);
			PendingIntent pendingIntent = PendingIntent
					.getBroadcast(mAddHabit, 0,
					intent, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager am;
			/* 获取闹钟管理的实例 */
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
			/* 设置闹钟 */
			am.set(AlarmManager.RTC_WAKEUP, calendar
					.getTimeInMillis(), pendingIntent);
			/* 设置周期闹 */
	//		am.setRepeating(AlarmManager.RTC_WAKEUP, System
	//				.currentTimeMillis()
	//				+ (10 * 1000), (24 * 60 * 60 * 1000),
	//				pendingIntent);
			if(System.currentTimeMillis() > calendar.getTimeInMillis()){
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
			}
			am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (24 * 60 * 60 * 1000),
					pendingIntent);
		}else{
			editor.putString(Clock_Flag, "009:00").commit();
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
					if(share2Renren != null) {
						share2Renren.setEnabled(false);
						if(!cancelShare.isEnabled())
							cancelShare.setEnabled(true);
					}
				}
				break;
			case HANDLER_MSG_REFRESH_WEIBO:
				weiboAuth = settings.getBoolean(WEIBO_AUTHORIZE, false);
				if(weiboAuth) {
					addWeibo.setImageResource(R.drawable.weibo_icon_light);
					shareToWeibo = STATE_PUBLISH;
					System.out.println("新浪认证成功");
					if(share2Weibo != null) {
						share2Weibo.setEnabled(false);
						if(!cancelShare.isEnabled())
							cancelShare.setEnabled(true);
					}
				}
				break;
			default:
				break;
			}
		}
	}
	/** 格式化字符串(7:3->07:03) */
	private String format(int x) {
		String s = "" + x;
		if (s.length() == 1)
			s = "0" + s;
		return s;
	}
}