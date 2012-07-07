package org.dian.kiyan.Apis;

import static org.dian.kiyan.Constants.Constants.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.dian.kiyan.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.DialogError;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

public class Share2WeiboRunnable implements Runnable, RequestListener{
	
	/**微博的实例*/
	private Weibo weibo = Weibo.getInstance();
	/**从settings里面读取授权状态*/
	private SharedPreferences settings;
	/**分享的上下文环境*/
	private Context mContext;
	/**待分享的文字内容*/
	private String mContent;
	/**新浪微博的授权状态*/
	private boolean sinaAuthorization = false;
	/**ShareCenter Activity*/
	private Activity mShareActivity;
	/**从activity传过来的handler*/
	private Handler mHandler;
	
	public Share2WeiboRunnable(Context context, Activity shareCenter, 
			Handler handler) {
		mContext = context;
		mShareActivity = shareCenter;
		settings = context.getSharedPreferences(AUTHORIZATION, 
				Context.MODE_WORLD_WRITEABLE);
		sinaAuthorization = settings.getBoolean(WEIBO_AUTHORIZE, false);
		mHandler = handler;
	}
	
	public Share2WeiboRunnable (String content, Context context, Activity shareCenter, 
			Handler handler) {
		this(context, shareCenter, handler);
		mContent = content;
	}
	
	@Override
	public void run() {
		if(sinaAuthorization) {
			String token = settings.getString("weibo access token", "");
			AccessToken at = new AccessToken(token, WEIBO_APP_SECRET);
			weibo.setAccessToken(at);
			weibo.setRedirectUrl(WEIBO_REDIRECT_URL);
			Utility.setAuthorization(new Oauth2AccessTokenHeader());
			share2weibo(mContent);
			System.out.println("weibo is sharing:" + token);
		} else {
			weibo.setupConsumerConfig(WEIBO_APP_KEY, 
					WEIBO_APP_SECRET);
			weibo.setRedirectUrl(WEIBO_REDIRECT_URL);
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					weibo.authorize(mShareActivity, 
							new AuthDialogListener());
				}
			};
			mHandler.post(r);
		}
	}
	
	/**
	 * 对更新微博文字信息的一层封装
	 * */
	private void share2weibo(String content) {
	    try {
	        if (!TextUtils.isEmpty((String) (weibo.getAccessToken().getToken()))) {
	            // Just update a text weibo
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
	        	Runnable r = new Runnable() {
	    			
	    			@Override
	    			public void run() {
	    				Toast.makeText(mContext, mContext.getString(R.string.please_login), Toast.LENGTH_LONG);
	    			}
	    		};
	    		mHandler.post(r);
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
		weiboRunner.request(mContext, url, bundle, Utility.HTTPMETHOD_POST, 
				this);
		return rlt;
	}

	@Override
	public void onComplete(String response) {
		// 发送成功之后返回的信息
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(mContext, R.string.weiboShare, 
						Toast.LENGTH_SHORT).show();
			}
		};
		mHandler.post(r);
	}

	@Override
	public void onIOException(IOException e) {
		
	}

	@Override
	public void onError(WeiboException e) {
		
	}
	
	class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			// 认证成功之后的处理
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			AccessToken accessToken = new AccessToken(token, 
					WEIBO_APP_SECRET);
			accessToken.setExpiresIn(expires_in);
			System.out.println("expires_in=" + expires_in);
			weibo.setAccessToken(accessToken);
			sinaAuthorization = true;
			Editor editor = settings.edit();
			editor.putBoolean(WEIBO_AUTHORIZE, sinaAuthorization);
			editor.putString("weibo access token", token);
			editor.commit(); // 提交修改
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources()
							.getString(R.string.weiboAuth), Toast.LENGTH_SHORT)
							.show();
				}
			};
			mHandler.post(r);
			mHandler.sendEmptyMessage(HANDLER_MSG_REFRESH_WEIBO);
		}

		@Override
		public void onWeiboException(WeiboException e) {
		}

		@Override
		public void onError(DialogError e) {
		}

		@Override
		public void onCancel() {
		}
	}
}