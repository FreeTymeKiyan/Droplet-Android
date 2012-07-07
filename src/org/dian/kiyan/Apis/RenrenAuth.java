package org.dian.kiyan.Apis;

import org.dian.kiyan.R;

import com.mobclick.android.MobclickAgent;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import static org.dian.kiyan.Constants.Constants.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class RenrenAuth extends Activity {
	
	private Button auth;
	private Renren mRenren;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_auth);
		mRenren = new Renren(RENREN_API_KEY, RENREN_SECRET_KEY, 
				REREN_APP_ID, this);
		//ApiDemoInvoker.init(mRenren);
        handler = new Handler();
		initViews();
	}
	
	private void initViews() {
		final RenrenAuthListener listener = new RenrenAuthListener() {
			
			@Override
			public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(RenrenAuth.this, 
								"Auth failed", 
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onComplete(Bundle values) {
				// TODO get APIs
				Intent data = new Intent();
				data.putExtra("mRenren", mRenren);
				setResult(RESULT_OK, data);
				finish();
			}
			
			@Override
			public void onCancelLogin() {
				
			}
			
			@Override
			public void onCancelAuth(Bundle values) {
				
			}
		};
		auth = (Button) findViewById(R.id.btn_auth);
		auth.setText("Renren auth");
		auth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// get authorization
				mRenren.authorize(RenrenAuth.this, listener);
			}
		});
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
