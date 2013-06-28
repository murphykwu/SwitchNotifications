package com.konka.switchnotifications;

import java.util.ArrayList;
import java.util.List;

import com.konka.switchnotifications.widget.SwitchButton;

import android.app.INotificationManager;
import android.content.Context;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
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
	private List<AppInfo> mData;
	private ViewHolder viewHolder;
	//存放数据改变过的位置，并且记录起来，在列表滑动的时候进行修改
	public int[] mChangePosition;
	public static final int THE_POSITION_CHANGED = 8;	
	
	public AppsListAdapter(Context context, List<AppInfo> data)
	{
		mContext = context;
		mData = data;
		mLayoutInflater = LayoutInflater.from(context);
		//初始化记录某项是否点击的数组，如果点击过后就置为1，在重画的时候就会重新获取改变后的值而不是使用缓存值。
		mChangePosition = new int[mData.size()];
		for(int i = 0; i < mData.size(); i ++)
		{
//			Log.i(MainActivity.TAGS, "init mChangePosition");
			mChangePosition[i] = 0;
		}
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
		//如果是有改变的行，那么就不使用缓存数据，直接重新赋值
		if(mChangePosition[position] == 8)
		{
			Log.i(MainActivity.TAGS, "值有改变的行mChangePosition[" + position + "] = " + mChangePosition[position]);
			convertView = null;
		}
		if(convertView == null)
		{
			convertView = mLayoutInflater.inflate(R.layout.app_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.iv_app = (ImageView)convertView.findViewById(R.id.iv_app_icon);
			viewHolder.tv_app = (TextView)convertView.findViewById(R.id.tv_app_name);
			viewHolder.sbtn_app = (SwitchButton)convertView.findViewById(R.id.sb_switch);
			viewHolder.sbtn_app.setOnCheckedChangeListener(new SwitchButtonOnCheckedChangeListener(position));
			Log.i(MainActivity.TAGS, "convertView为空。position = " + position);
			convertView.setTag(viewHolder);
		}else
		{
			Log.i(MainActivity.TAGS, "视图不为空 Position = " + position);
			viewHolder = (ViewHolder)convertView.getTag();
		}

		//设置图标、名称、消息通知可否
		viewHolder.iv_app.setImageDrawable(mData.get(position).appIcon);
		viewHolder.tv_app.setText(mData.get(position).appName);
		Log.v(MainActivity.TAGS, "getView Position = " + position
				+ ", pkgName = " + mData.get(position).appName
				+ ", appCanNotification = " + mData.get(position).appCanNotification);
		viewHolder.sbtn_app.setChecked(mData.get(position).appCanNotification);
		//如果要针对每一行的switchbutton响应滑动操作，就需要针对每个按钮设置监听函数。为了区别是哪个应用
		//需要传入position来确定
//		viewHolder.sbtn_app.setOnCheckedChangeListener(new SwitchButtonOnCheckedChangeListener(position));
		
		return convertView;
	}

	class SwitchButtonOnCheckedChangeListener implements OnCheckedChangeListener
	{
		private int mPosition;
		private INotificationManager nm;
		
		public SwitchButtonOnCheckedChangeListener(int position) {
			// TODO Auto-generated constructor stub
			mPosition = position;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			
			int vid = buttonView.getId();
			Log.e(MainActivity.TAGS, "按钮的响应函数（onCheckedChanged） Position = " + mPosition
					+ ", pkgName = " + mData.get(mPosition).packageName);
			if(vid == viewHolder.sbtn_app.getId())
			{
				//可以在这里对触发的每个程序进行设置。当关闭的时候isChecked为false，如果打开则为true				
				ifCanNotify(mData.get(mPosition).packageName, isChecked);
				//标识这个位置的元素改变了。
				mChangePosition[mPosition] = THE_POSITION_CHANGED;
				Log.e(MainActivity.TAGS, "在响应函数中将新的值写入系统中onCheckedChanged Position = " + mPosition 
						+ ", pkgName = " + mData.get(mPosition).packageName 
						+ ", isChecked = " + isChecked + ", mChangePosition[" + mPosition + "] = " + mChangePosition[mPosition]);
			}
		}
		
		/**
		 * 打开关闭应用程序的通知功能。
		 * @param pkgName 包名
		 * @param isChecked 是否打开通知功能
		 */
		private void ifCanNotify(String pkgName, boolean isChecked)
		{
			nm = INotificationManager.Stub.asInterface(
					ServiceManager.getService(Context.NOTIFICATION_SERVICE));
			try {
//				long sortTime = SystemClock.uptimeMillis();
				mData.get(mPosition).appCanNotification = isChecked;				
//				Log.i(MainActivity.TAG, "赋值时间：" + (SystemClock.uptimeMillis() - sortTime));
//				sortTime = SystemClock.uptimeMillis();
				nm.setNotificationsEnabledForPackage(pkgName, isChecked);
//				Log.i(MainActivity.TAG, "修改应用通知时间：" + (SystemClock.uptimeMillis() - sortTime));
				Log.i(MainActivity.TAGS, "ifCanNotify pkgName = " + pkgName
						+ ", mPosition = " + mPosition
						+ ", isChecked = " + isChecked
						+ ", 系统设置中通知开关 = " + nm.areNotificationsEnabledForPackage(pkgName));
				
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private final static class ViewHolder{
		ImageView iv_app;
		TextView  tv_app;
		SwitchButton sbtn_app;
	}
}
