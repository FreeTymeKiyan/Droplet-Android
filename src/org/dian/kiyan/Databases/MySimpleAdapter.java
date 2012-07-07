package org.dian.kiyan.Databases;

import java.util.List;
import java.util.Map;

import org.dian.kiyan.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
    private List<Map<String, Object>> list;
    private int layoutID;
    private String flag[];
    private int ItemIDs[];
	private View view;
	
    public MySimpleAdapter(Context context, List<Map<String, Object>> list,
            int layoutID, String flag[], int ItemIDs[]) {
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.layoutID = layoutID;
        this.flag = flag;
        this.ItemIDs = ItemIDs;
    }
    
    @Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/*ViewHolder vh;
		if(convertView == null) {
			convertView = mInflater.inflate(layoutID, null);
			Map<String, Object> map = list.get(position);
			vh = new ViewHolder();
			vh.state = (ImageView) convertView.findViewById(R.id.iv_detailState);
			vh.state.setBackgroundResource((Integer) map.get(flag[1]));
			vh.detail = (TextView) convertView.findViewById(R.id.tv_detailStateTitle);
			vh.detail.setText((String) map.get(flag[2]));
			vh.timestamp = (TextView) convertView.findViewById(R.id.tv_timestamp);
			vh.timestamp.setText((String) map.get(flag[3]));
			vh.words = (TextView) convertView.findViewById(R.id.tv_detailWords);;
			vh.words.setText((String) map.get(flag[0]));
			convertView.setTag(vh);  
		} else {
			vh = (ViewHolder) convertView.getTag();
		}*/
		if(layoutID == 0) {
			convertView = view;
		} else {
			convertView = mInflater.inflate(layoutID, null);
		}
		for(int i = 0; i < flag.length; i++) {
			if (convertView.findViewById(ItemIDs[i]) instanceof ImageView) {
                ImageView iv = (ImageView) convertView.findViewById(ItemIDs[i]);
                iv.setBackgroundResource((Integer) list.get(position).get(
                        flag[i]));
            } else if (convertView.findViewById(ItemIDs[i]) instanceof TextView) {
                TextView tv = (TextView) convertView.findViewById(ItemIDs[i]);
                tv.setText((String) list.get(position).get(flag[i]));
                if(list.get(position).containsKey("titleColor")) { 
                	// 设置颜色
                	if(tv.getId() == R.id.tv_itemTitle) {
                		final String colorStr = list.get(position).get("titleColor").toString();
                		final int color = Integer.parseInt(colorStr); 
                		tv.setTextColor(color);
                	}
                }
            } else {
                // ...备注2
            }
			addListener(convertView);
		}
        return convertView;
	}
	
	/**
     * 只需要将需要设置监听事件的组件写在下面这方法里就可以啦！ 
     * 别的不需要修改
     */
    public void addListener(View convertView) {
    	
    }
    
    static class ViewHolder {
    	ImageView state;
    	TextView words;
    	TextView detail;
    	TextView timestamp;
    	
    	public ViewHolder() {
    		
    	}
    }
}
