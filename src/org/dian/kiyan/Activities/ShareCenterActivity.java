package org.dian.kiyan.Activities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.dian.kiyan.R;
import org.dian.kiyan.Databases.Habit;
import org.dian.kiyan.Databases.HabitDatabase;

import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.exception.RenrenException;
import com.renren.api.connect.android.status.StatusSetRequestParam;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.DialogError;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.ShareActivity;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

import static org.dian.kiyan.Constants.Constants.*;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

// TODO 调用的时候还是传一下title吧
// TODO 根据情况实现各界面的分享功能
// TODO 某些情况下要生成图片并显示在对应区域
// TODO 可以在图片下方留一句话，有字数限制
// TODO 两个ImageButton第一次点进去可以授权
// TODO 授权成功后点击可以改变亮暗状态
// TODO 用自定义布局将导航栏变成EditText的一部分
public class ShareCenterActivity extends Activity implements RequestListener{
	
	/**对应习惯的全局唯一标识ID*/
	private String uuid;
	/**待分享习惯的title*/
	private String title;
	/**待分享的文字内容的控件*/
	private EditText shareText;
	/**待分享的文字内容*/
	private String text;
	/**待分享的图片内容*/
	private ImageView shareImage;
	/**关于传过来Title的habit的最新统计信息*/
	private ContentValues habitStatistics = new ContentValues();
	/**授权情况：新浪*/
	private boolean sinaAuthorization = false;
	/**授权情况：人人*/
	private boolean renrenAuthorization;
	/**分享状态：新浪*/
	private int sinaShareState = STATE_NOT_PUBLISH;
	/**分享状态：人人*/
	private int renrenShareState = STATE_NOT_PUBLISH;
	/**全局的偏好设置*/
	private SharedPreferences settings;
	/**人人SDK的封装类*/
	private Renren mRenren;
	/**分享到人人的按钮*/
	private ImageButton shareToRenren;
	/**分享到微博的按钮*/
	private ImageButton shareToWeibo;
	/**微博的SDK封装类*/
	private Weibo weibo= Weibo.getInstance();;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO 从哪里来...
		setContentView(R.layout.share_center);
		prepareData();
		initView();
		// TODO 到哪里去...回到上一页面
	}

	private void prepareData() {
		Intent i = getIntent();
		if(i.hasExtra(UUID)){
			uuid = i.getStringExtra(UUID);
			System.out.println(uuid);
			HabitDatabase h = new HabitDatabase(this);
			habitStatistics = h.queryStatistics(uuid);
			sinaShareState = habitStatistics.getAsInteger(SHARE_TO_WEIBO);
			renrenShareState = habitStatistics.getAsInteger(SHARE_TO_RENREN);
		} else {
			// TODO 没有title，还能干吗？
		}
		/* 从全局的sharedpreference读取授权状态 */
		settings = getSharedPreferences(AUTHORIZATION, 
				MODE_WORLD_WRITEABLE);
		if(settings.contains("weibo access token")) {
			System.out.println("access token~");
			sinaAuthorization = settings.getBoolean(WEIBO_AUTHORIZE, false); 
			String token = settings.getString("weibo access token", "");
			AccessToken at = new AccessToken(token, WEIBO_APP_SECRET);
			weibo.setAccessToken(at);
			weibo.setRedirectUrl(WEIBO_REDIRECT_URL);
			Utility.setAuthorization(new Oauth2AccessTokenHeader());
		}
		/*renrenAuthorization = settings.getBoolean(RENREN_AUTHORIZE, false);*/ 
		renrenAuthorization = false;
	}

	private void initView() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams
				.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 进入Activity不弹输入法
		/* 上方导航栏的两个Button */
		ImageButton shareCancel = (ImageButton) findViewById(R.id.ib_shareCancel);
		shareCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		ImageButton shareAdmit = (ImageButton) findViewById(R.id.ib_shareAdmit);
		shareAdmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(judgeValidity()) { // 判断分享的内容合法性
					// TODO 读取待分享的内容文字和图片
					processContent();
					// TODO 处理待分享的内容文字加上app相关的内容？
					if(sinaShareState == STATE_PUBLISH) {
						// TODO 异步分享到微博
						share2weibo(text);
					}
					if(renrenShareState == STATE_PUBLISH) {
						// TODO 异步分享到人人
						share2renren(text);
					}
					// TODO 想办法确定分享成功
					setResult(RESULT_OK);
					finish();
				}else {
					// TODO 提示内容为什么不符合要求
				}
			}
		});
		shareImage = (ImageView) findViewById(R.id.iv_shareImage);
		// TODO shareImage.setImageResource(resId);
		shareText = (EditText) findViewById(R.id.et_shareText);
		shareToRenren = (ImageButton) findViewById(R.id.ib_shareToRenren);
		if(renrenAuthorization && renrenShareState == STATE_PUBLISH)
			shareToRenren.setImageResource(R.drawable.renren_icon_light);
		else
			shareToRenren.setImageResource(R.drawable.renren_icon_dark);
		shareToRenren.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 如果没授权则授权，如果授权了则改变分享状态
				if(renrenAuthorization) {
					// TODO 改变分享状态
					if(renrenShareState == STATE_PUBLISH) { // 授权已有，改变分享状态
						renrenShareState = STATE_NOT_PUBLISH;
						// TODO 数据库里面数据改变
						onChangeShareState(UPDATE_RENREN_STATE);
						// 图片效果也需要随之改变
						shareToRenren.setImageResource(R.drawable.renren_icon_dark);
					} else {
						renrenShareState = STATE_PUBLISH;
						// TODO 数据库数据改变
						onChangeShareState(UPDATE_RENREN_STATE);
						// 图片效果也需要随之改变
						shareToRenren.setImageResource(R.drawable.renren_icon_light);
					}
				} else {
					// 跳转到授权页面
					getRenrenAccess();
				}
			}
		});
		shareToWeibo = (ImageButton) findViewById(R.id.ib_shareToWeibo);
		if(sinaAuthorization && sinaShareState == STATE_PUBLISH)
			shareToWeibo.setImageResource(R.drawable.weibo_icon_light);
		else
			shareToWeibo.setImageResource(R.drawable.weibo_icon_dark);
		shareToWeibo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 如果没授权则授权，如果授权了则改变分享状态
				if(sinaAuthorization) {
					if(sinaShareState == STATE_NOT_PUBLISH) { // 授权已有，改变分享状态
						sinaShareState = STATE_PUBLISH;
						// TODO 数据库数据改变
						onChangeShareState(UPDATE_SINA_STATE);
						// 图片效果也需要随之改变
						shareToWeibo.setImageResource(R.drawable.weibo_icon_light);
					} else {
						sinaShareState = STATE_NOT_PUBLISH;
						// TODO 数据库数据改变
						onChangeShareState(UPDATE_SINA_STATE);
						// 图片效果也需要随之改变
						shareToWeibo.setImageResource(R.drawable.weibo_icon_dark);
					}
				} else {
					// TODO 需要搞清楚新浪的授权为什么记不住
					// 跳转到授权页面
					weibo.setupConsumerConfig(WEIBO_APP_KEY, 
							WEIBO_APP_SECRET);
					weibo.setRedirectUrl(WEIBO_REDIRECT_URL);
					weibo.authorize(ShareCenterActivity.this, 
							new AuthDialogListener());
				}
			}
		});
	}
	
	/**
	 * 把内容处理成我们想要的
	 * 当然，内容必须限定字数以满足社交网络的需求
	 * 如果不限定字数的话，需实现分多条发送的功能。
	 * */
	protected void processContent() {
		/* 
		 * 发布模板
		 * #每月一个好习惯#+title+date+成功？次+一句话
		 * */
		String header = "#每月一个好习惯#";
		String title = habitStatistics.getAsString(TITLE);
		String date = habitStatistics.getAsString(TIMESTAMP);
		date = date.split(" ")[0];
		int successfulTimes = habitStatistics.getAsInteger(SUCCESSFUL_TIMES);
		String success = getResources().getString(R.string.success) + 
					+ successfulTimes 
					+ getResources().getString(R.string.times);
		StringBuilder temp = new StringBuilder();
//		temp.append(header).append("+")
//				.append(title).append("+")
//				.append(date).append("+")
//				.append(success).append("+").append(text);
//		temp.append(title).append("+")
//		.append(date).append("+")
//		.append(success).append("+").append(text);
		temp.append(text).append("在").append(date).append("这个月黑风高的白天");
		text = temp.toString();
	}

	/**
	 * 判断输入内容的合法性
	 * 如：输入文字长度、是否为空、图片大小...
	 * */
	protected boolean judgeValidity() {
		// TODO 文字长度没判断
		text = shareText.getText().toString().trim();
		if(text.equals(""))
			return false;
		return true;
	}
	
	/**
	 * 获取人人授权
	 * */
	protected boolean getRenrenAccess() {
		mRenren = new Renren(RENREN_API_KEY, RENREN_SECRET_KEY, 
				REREN_APP_ID, this);
		final RenrenAuthListener listener = new RenrenAuthListener() {
			
			@Override
			public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
				new Handler().post(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(ShareCenterActivity.this, 
								"Auth failed", 
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onComplete(Bundle values) {
				/* 成功认证之后 */
				renrenAuthorization = true; // 授权成功，改变授权状态
				renrenShareState = STATE_PUBLISH; // 成功后默认改分享状态
				/*Editor editor = settings.edit(); // 改变全局偏好设置的状态
				editor.putBoolean(RENREN_AUTHORIZE, renrenAuthorization);
				editor.commit();*/ // 提交修改
				onChangeShareState(UPDATE_RENREN_STATE);
				/*HabitDatabase h = new HabitDatabase(ShareCenter.this);
				ContentValues tempValue = new ContentValues();
				tempValue.put(TITLE, title);
				tempValue.put(SHARE_TO_RENREN, renrenShareState);
				Habit habit = new Habit(tempValue);
				Message msg = new Message();
				msg.what = UPDATE_RENREN_STATE;
				h.update(habit, msg);*/ // 更新数据库里的单个习惯状态：分享到人人
				Toast.makeText(ShareCenterActivity.this, getResources()
						.getString(R.string.renrenAuth), Toast.LENGTH_SHORT).show();
				// TODO 图片的效果也要随之改变
				shareToRenren.setImageResource(R.drawable.renren_icon_light);
			}
			
			@Override
			public void onCancelLogin() {
				
			}
			
			@Override
			public void onCancelAuth(Bundle values) {
				
			}
		};
		mRenren.authorize(ShareCenterActivity.this, listener);
		return true;
	}
	
	/**
	 * 对更新人人文字信息的一层封装 
	 * */
	private void share2renren(String content) {
		String wait2publish = content;
		StatusSetRequestParam status = new StatusSetRequestParam(wait2publish);
		try {
			mRenren.publishStatus(status);
		} catch (RenrenException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 对更新微博文字信息的一层封装
	 * */
	private void share2weibo(String content) {
	    try {
	        if (!TextUtils.isEmpty((String) (weibo.getAccessToken().getToken()))) {
	            // Just update a text weibo
	        	System.out.println("update now");
	            update(weibo, Weibo.getAppKey(), content, "", "");
	        } else if(TextUtils.isEmpty((String) 
	        		(weibo.getAccessToken().getToken())) 
	        		&& settings.contains("weibo access token")) {
	        	String rlt = settings.getString("weibo access token", "");
				AccessToken token = new AccessToken(rlt);
				weibo.setAccessToken(token);
				weibo.setupConsumerConfig(WEIBO_APP_KEY, 
						WEIBO_APP_SECRET);
				weibo.setRedirectUrl(WEIBO_REDIRECT_URL);
				update(weibo, Weibo.getAppKey(), content, "", "");
	        } else {
	            Toast.makeText(this, this.getString(R.string.please_login), Toast.LENGTH_LONG);
	        }
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (WeiboException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * 实际分享到微博的方法
	 * */
	private String update(Weibo weibo, String source, String status, 
			String lon, String lat)throws MalformedURLException, 
			IOException, WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("source", source);
		bundle.add("status", status);
		if (!TextUtils.isEmpty(lon)) {
		    bundle.add("lon", lon);
		}
		if (!TextUtils.isEmpty(lat)) {
		    bundle.add("lat", lat);
		}
		String rlt = "";
		String url = Weibo.SERVER + "statuses/update.json";
		AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
		weiboRunner.request(this, url, bundle, Utility.HTTPMETHOD_POST, 
				this);
		return rlt;
	}
	
	/**
	 * 微博认证的内部类
	 * 根据认证结果返回内容
	 * 成功时获得access_token并返回上一界面
	 * */
	class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			AccessToken accessToken = new AccessToken(token, 
					WEIBO_APP_SECRET);
			accessToken.setExpiresIn(expires_in);
			weibo.setAccessToken(accessToken);
			/* 返回上一界面 */
			sinaAuthorization = true; // 授权成功，改变授权状态
			sinaShareState = STATE_PUBLISH; // 成功后默认改分享状态
			Editor editor = settings.edit(); // 改变全局偏好设置的状态
			editor.putBoolean(WEIBO_AUTHORIZE, sinaAuthorization);
			editor.putString("weibo access token", token);
			editor.commit(); // 提交修改
			onChangeShareState(UPDATE_SINA_STATE);
			/*HabitDatabase h = new HabitDatabase(ShareCenter.this);
			ContentValues tempValue = new ContentValues();
			tempValue.put(TITLE, title);
			tempValue.put(SHARE_TO_WEIBO, sinaShareState);
			Habit habit = new Habit(tempValue);
			Message msg = new Message();
			msg.what = UPDATE_SINA_STATE; 
			h.update(habit, msg);*/ // 更新数据库里的单个习惯状态：分享到新浪
			Toast.makeText(ShareCenterActivity.this, getResources()
					.getString(R.string.weiboAuth), Toast.LENGTH_SHORT).show();
			// TODO 图片的效果也要随之改变
			shareToWeibo.setImageResource(R.drawable.weibo_icon_light);
		}

		@Override
		public void onError(DialogError e) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onComplete(String response) {
		
	}

	@Override
	public void onIOException(IOException e) {
		
	}

	@Override
	public void onError(WeiboException e) {
		
	}
	
	/**每次改变分享状态的时候改变数据库数据状态*/
	private void onChangeShareState(int when) {
		HabitDatabase h = new HabitDatabase(ShareCenterActivity.this);
		ContentValues tempValue = new ContentValues();
		tempValue.put(UUID, uuid);
		switch (when) {
		case UPDATE_RENREN_STATE:
			tempValue.put(SHARE_TO_RENREN, renrenShareState);
			Habit habit1 = new Habit(tempValue);
			Message msg1 = new Message();
			msg1.what = UPDATE_RENREN_STATE;
			h.update(habit1, msg1);
			break;
		case UPDATE_SINA_STATE:
			tempValue.put(SHARE_TO_WEIBO, sinaShareState);
			Habit habit2 = new Habit(tempValue);
			Message msg2 = new Message();
			msg2.what = UPDATE_SINA_STATE; 
			h.update(habit2, msg2);
			break;
		default:
			break;
		}
	}
}