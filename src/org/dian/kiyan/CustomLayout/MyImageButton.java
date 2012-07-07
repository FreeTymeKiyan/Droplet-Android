package org.dian.kiyan.CustomLayout;

import org.dian.kiyan.R;

import android.content.Context; 
import android.view.Gravity;
import android.widget.ImageView; 
import android.widget.LinearLayout; 
import android.widget.TextView; 
 
/**
 * 自定义ImageButton，模拟ImageButton，并在其下方显示文字
 * 提供Button的部分接口 
 * */ 
public class MyImageButton extends LinearLayout { 
 
	public MyImageButton(Context context, int imageResId, int textResId) { 
		super(context); 
 
		mButtonImage = new ImageView(context); 
		mButtonText = new TextView(context); 
 
		setImageResource(imageResId); 
		mButtonImage.setPadding(5, 5, 5, 5); 
 
		setText(textResId); 
		setTextColor(0xFF000000); 
		mButtonText.setPadding(5, 20, 5, 0); 
 
		//设置本布局的属性 
		setClickable(true);  //可点击 
		setFocusable(true);  //可聚焦 
		setBackgroundResource(R.drawable.u14_original);  //布局才用普通按钮的背景 
		setOrientation(LinearLayout.VERTICAL);  //垂直布局 
		mButtonText.setGravity(Gravity.CENTER);
		//首先添加Image，然后才添加Text 
		//添加顺序将会影响布局效果 
		addView(mButtonText); 
		addView(mButtonImage); 
	} 
 
	// ----------------public method----------------------------- 
	/** 
	 * setImageResource方法 
	 */ 
	public void setImageResource(int resId) { 
		mButtonImage.setImageResource(resId); 
	} 
	 
	/** 
	 * setText方法 
	 */ 
	public void setText(int resId) { 
		mButtonText.setText(resId); 
	} 
	 
	public void setText(CharSequence buttonText) { 
		mButtonText.setText(buttonText); 
	} 
	 
	/** 
	 * setTextColor方法 
	 */ 
	public void setTextColor(int color) { 
		mButtonText.setTextColor(color); 
	} 
	 
	// ----------------private attribute----------------------------- 
	private ImageView mButtonImage = null; 
	private TextView mButtonText = null; 
} 