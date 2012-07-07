package org.dian.kiyan.Apis;

import static org.dian.kiyan.Constants.Constants.WEIBO_APP_KEY;
import static org.dian.kiyan.Constants.Constants.WEIBO_APP_SECRET;
import static org.dian.kiyan.Constants.Constants.WEIBO_REDIRECT_URL;

import org.dian.kiyan.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

public class WeiboAuth extends Activity {
	private Weibo mWeibo = Weibo.getInstance();
	private Button auth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_auth);
		initViews();
		
	}
	
	private void initViews() {
		auth = (Button) findViewById(R.id.btn_auth);
		auth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// get authentication
				mWeibo.setupConsumerConfig(WEIBO_APP_KEY, WEIBO_APP_SECRET);
				mWeibo.setRedirectUrl(WEIBO_REDIRECT_URL);
				mWeibo.authorize(WeiboAuth.this, new AuthDialogListener());
			}
		});
	}
	
	public class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			AccessToken accessToken = new AccessToken(token, WEIBO_APP_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);
			// return to the main activity
			Intent i = new Intent();
			i.putExtra("sina_access_token", token);
			setResult(RESULT_OK, i);
			finish();
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
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
}
