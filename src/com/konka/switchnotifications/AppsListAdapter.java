package com.konka.switchnotifications;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.konka.switchnotifications.widget.SwitchButton;
import com.konka.switchnotifications.widget.SwitchOnCheckedChangeListener;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<AppInfo> mData = new ArrayList<AppInfo>();
	private ViewHolder viewHolder;
	
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
		viewHolder = new ViewHolder();
		if(convertView == null)
		{
			convertView = mLayoutInflater.inflate(R.layout.app_list_item, null);
			viewHolder.iv_app = (ImageView)convertView.findViewById(R.id.iv_app_icon);
			viewHolder.tv_app = (TextView)convertView.findViewById(R.id.tv_app_name);
			viewHolder.sbtn_app = (SwitchButton)convertView.findViewById(R.id.sb_switch);
//			viewHolder.sbtn_app.setOnCheckedChangeListener(new SwitchOnCheckedChangeListener() , mData.get(position).packageName);
//			viewHoler.sbtn_app.setClickable(false);
			convertView.setTag(viewHolder);
		}else
		{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		//设置图标、名称、消息通知可否
		viewHolder.iv_app.setImageDrawable(mData.get(position).appIcon);
		viewHolder.tv_app.setText(mData.get(position).appName);
		viewHolder.sbtn_app.setChecked(mData.get(position).appCanNotification);
		//如果要针对每一行的switchbutton响应滑动操作，就需要针对每个按钮设置监听函数。为了区别是哪个应用
		//需要传入position来确定
		Log.v(MainActivity.TAG, "getView Position = " + position);
		viewHolder.sbtn_app.setOnCheckedChangeListener(new SwitchButtonOnCheckedChangeListener(position));
		
		return convertView;
	}

	class SwitchButtonOnCheckedChangeListener implements OnCheckedChangeListener
	{
		private int mPosition;
		
		public SwitchButtonOnCheckedChangeListener(int position) {
			// TODO Auto-generated constructor stub
			mPosition = position;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			
			int vid = buttonView.getId();
			Log.e(MainActivity.TAG, "onCheckedChanged11111 Position = "
					+ mPosition + ", pkgName = " + mData.get(mPosition).packageName + "vid = " + vid);
			if(vid == viewHolder.sbtn_app.getId())
			{
				Log.e(MainActivity.TAG, "onCheckedChanged Position = "
			+ mPosition + ", pkgName = " + mData.get(mPosition).packageName);
			}
			
		}
		
	}
	
	private final static class ViewHolder{
		ImageView iv_app;
		TextView  tv_app;
		SwitchButton sbtn_app;
	}
}
