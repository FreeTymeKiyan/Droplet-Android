package org.dian.kiyan.Apis;

import static org.dian.kiyan.Constants.Constants.*;

import org.dian.kiyan.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.exception.RenrenException;
import com.renren.api.connect.android.status.StatusSetRequestParam;
import com.renren.api.connect.android.view.RenrenAuthListener;

public class Share2RenrenRunnable implements Runnable {

	/**人人的封装类*/
	private Renren mRenren;
	/**分享的上下文环境*/
	private Context mContext;
	/**Activity的引用*/
	private Activity mActivity;
	/**从settings里面读取授权状态*/
	private SharedPreferences settings;
	/**从activity传过来的handler*/
	private Handler mHandler;
	/**人人的授权状态*/
	private boolean renrenAuthorization = false;
	/**待分享的文字内容*/
	private String mContent;
	
	public Share2RenrenRunnable(Context context, Activity activity, Handler handler) {
		mContext = context;
		mActivity = activity;
		settings = context.getSharedPreferences(AUTHORIZATION, 
				Context.MODE_WORLD_WRITEABLE);
		renrenAuthorization = settings.getBoolean(RENREN_AUTHORIZE, false);
		mHandler =  handler;
	}
	
	public Share2RenrenRunnable(String content, Context context, Activity activity, Handler handler) {
		this(context, activity, handler);
		mContent = content;
	}
	
	@Override
	public void run() {
		if(renrenAuthorization) {
			mRenren = new Renren(RENREN_API_KEY, RENREN_SECRET_KEY, 
					REREN_APP_ID, mContext);
			StatusSetRequestParam status = new StatusSetRequestParam(mContent);
			try {
				mRenren.publishStatus(status);
			} catch (RenrenException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			mRenren = new Renren(RENREN_API_KEY, RENREN_SECRET_KEY, 
					REREN_APP_ID, mContext);
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					mRenren.authorize(mActivity, ral);
				}
			};
			mHandler.post(r);
		}
	}
	
	private RenrenAuthListener ral = new RenrenAuthListener() {
		
		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(mContext, 
							"Auth failed", 
							Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		@Override
		public void onComplete(Bundle values) {
			/* 成功认证之后 */
			renrenAuthorization = true; // 授权成功，改变授权状态
			Editor editor = settings.edit(); // 改变全局偏好设置的状态
			editor.putBoolean(RENREN_AUTHORIZE, renrenAuthorization);
			editor.commit(); // 提交修改
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources()
							.getString(R.string.renrenAuth), Toast.LENGTH_SHORT).show();
				}
			};
			mHandler.post(r);
			mHandler.sendEmptyMessage(HANDLER_MSG_REFRESH_RENREN);
		}
		
		@Override
		public void onCancelLogin() {
			
		}
		
		@Override
		public void onCancelAuth(Bundle values) {
			
		}
	};
}