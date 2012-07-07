package org.dian.kiyan.Databases;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PopupListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
    private List<Map<String, Object>> list;
    private int layoutID;
    private String flag[];
    private int ItemIDs[];
	
    public PopupListAdapter(Context context, List<Map<String, Object>> list,
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
		return 0;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(layoutID, null);
		for(int i = 0; i < flag.length; i++) {
			if (convertView.findViewById(ItemIDs[i]) instanceof ImageView) {
                ImageView iv = (ImageView) convertView.findViewById(ItemIDs[i]);
                iv.setBackgroundResource((Integer) list.get(position).get(
                        flag[i]));
            } else if (convertView.findViewById(ItemIDs[i]) instanceof TextView) {
                TextView tv = (TextView) convertView.findViewById(ItemIDs[i]);
                tv.setText((String) list.get(position).get(flag[i]));
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
}
