package com.konka.switchnotifications;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.konka.switchnotifications.widget.SwitchButton;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<AppInfo> mData = new ArrayList<AppInfo>();
	
	public AppsListAdapter(Context context, List<AppInfo> data)
	{
		mContext = context;
		mData = data;
		mLayoutInflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHoler = new ViewHolder();
		if(convertView == null)
		{
			convertView = mLayoutInflater.inflate(R.layout.app_list_item, null);
			viewHoler.iv_app = (ImageView)convertView.findViewById(R.id.iv_app_icon);
			viewHoler.tv_app = (TextView)convertView.findViewById(R.id.tv_app_name);
			viewHoler.sbtn_app = (SwitchButton)convertView.findViewById(R.id.sb_switch);
			viewHoler.sbtn_app.setClickable(false);
			convertView.setTag(viewHoler);
		}else
		{
			viewHoler = (ViewHolder)convertView.getTag();
		}
		
		//设置图标、名称、消息通知可否
		viewHoler.iv_app.setImageDrawable(mData.get(position).appIcon);
		viewHoler.tv_app.setText(mData.get(position).appName);
		viewHoler.sbtn_app.setChecked(mData.get(position).appCanNotification);
		
		
		return convertView;
	}

	
	
	private final static class ViewHolder{
		ImageView iv_app;
		TextView  tv_app;
		SwitchButton sbtn_app;
	}
}
