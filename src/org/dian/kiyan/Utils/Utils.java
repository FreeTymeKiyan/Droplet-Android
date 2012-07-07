package org.dian.kiyan.Utils;

import static org.dian.kiyan.Constants.Constants.SUCCESSFUL_TIMES;
import static org.dian.kiyan.Constants.Constants.TIMESTAMP;
import static org.dian.kiyan.Constants.Constants.TITLE;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.dian.kiyan.R;

import android.content.ContentValues;
import android.content.Context;

/**
 * 程序常用工具方法类
 * @author FreeTymeKiyan
 * @version 1.0
 * */
public class Utils {
	
	/**
	 * 通过Calendar得到当前日期的字符串
	 * @return String dateStr
	 * */
	public static String getCalendarString() {
		String dateStr = "";
	    Calendar calendar1 = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
	    dateStr = sdf.format(calendar1.getTime());
	    System.out.println(dateStr);
	    return dateStr; 
	}
	
	/**
	 * 精确计算两个Calendar日期的天数差
	 * */
	public static int differ(Calendar dateBefore, Calendar dateAfter) {
		int Now = dateAfter.get(Calendar.DATE);
		int Before = dateBefore.get(Calendar.DATE);
	    return Now - Before;  //用立即数，减少乘法计算的开销
	}
	
	/**
	 * 通过Date得到当前日期的字符串
	 * */
	public static String getDateString() {
		String dateStr = "";
		Date date = new Date();
		dateStr = date.toString();
		return dateStr;
	}
	
	public static String getFormatTime(long temp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String time = sdf.format(temp);
		return time;
	}
	
	/** 
     * 获得一个UUID 
     * @return String UUID 
     */ 
    public static String getUUID(){ 
        String s = UUID.randomUUID().toString(); 
        //去掉“-”符号 
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
    }  
    
    /** 
     * 获得指定数目的UUID 
     * @param number int 需要获得的UUID数量 
     * @return String[] UUID数组 
     */ 
    public static String[] getUUID(int number){ 
        if(number < 1){ 
            return null; 
        } 
        String[] ss = new String[number]; 
        for(int i=0;i<number;i++){ 
            ss[i] = getUUID(); 
        } 
        return ss; 
    } 
    
    /**
     * 将待分享信息处理成固定格式
     * */
    protected void processContent(ContentValues habitStatistics, 
    		Context context, String text) {
		/* 
		 * 发布模板
		 * #每月一个好习惯#+title+date+成功？次+一句话
		 * */
		String header = "#每月一个好习惯#";
		String title = habitStatistics.getAsString(TITLE);
		String date = habitStatistics.getAsString(TIMESTAMP);
		date = date.split(" ")[0];
		int successfulTimes = habitStatistics.getAsInteger(SUCCESSFUL_TIMES);
		String success = context.getResources().getString(R.string.success) + 
					+ successfulTimes 
					+ context.getResources().getString(R.string.times);
		StringBuilder temp = new StringBuilder();
		temp.append(header).append("+")
				.append(title).append("+")
				.append(date).append("+")
				.append(success).append("+").append(text);
		text = temp.toString();
	}
}
