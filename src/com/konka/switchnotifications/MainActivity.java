package com.konka.switchnotifications;

//import android.app.INotificationManager;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.INotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.Switch;

public class MainActivity extends Activity {
	public static final String TAG = "SwitchNotificationssss";
	public static final String TAGS = "SwitchNotificationssss2";
	private PackageManager mPackageManager;
	private List<PackageInfo> mPackageInfoList;
	private ArrayList<AppInfo> mAppsList;//存放所有安装程序的数据
	ArrayList<AppInfo> tempAppList;
	private ListView lv_apps;
	private AppsListAdapter mAppsAdp;
	private View mLayoutContainerLoading;
	private Thread mInitListThread;
	private Context mContext;
	public static final int SEND_INIT_LIST_MSG = 1000; 
	public static final int SWITCH_LIST = SEND_INIT_LIST_MSG + 1;
	private Switch mSwitchAll;
	private ProgressBar mPb;
	private ProgressDialog mPd;
	
		
//当所有的应用开关都是关闭的时候，需要设置switch为true，当所有的开关是打开的时候，需要设置switch为false。
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSwitchAll = (Switch)this.findViewById(R.id.switch_all_Notifications);
		mSwitchAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				/**
				 * 当开关打开的时候，就是屏蔽所有应用的通知。
				 * 直接将下面的listview灰显，并且改变下面list中每项的状态为真，同时记录所有的状态。
				 * 当开关关闭的时候，将所有应用的通知选项恢复成全部屏蔽之前的，
				 * 也就是需要存储屏蔽所有之前应用通知状态。
				 * 在没有确认的情况下，只需要将所有的应用置为打开就行了。可以用发送handle的方式来更新，免得阻塞UI界面
				 */
				mLayoutContainerLoading.setVisibility(View.VISIBLE);
				lv_apps.setVisibility(View.INVISIBLE);
				Log.i(TAGS, "onCheckedChanged switch all isChecked = " + isChecked);
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						boolean isChecked = mSwitchAll.isChecked();
						int appsSize = mAppsList.size();				
						for(int i = 0; i < appsSize; i ++)
						{
							//如果这个开关打开了，那么就关闭下面所有应用的通知开关。
							mAppsList.get(i).appCanNotification = !isChecked;
							mAppsList.get(i).setNotify(!isChecked);
						}
						Message msg = new Message();
						msg.what = SWITCH_LIST;
						mHandler.sendMessage(msg);
					}
					
				}).start();
			}
		});
		
		mContext = MainActivity.this;
		mPackageManager = this.getPackageManager();
		mPackageInfoList = mPackageManager.getInstalledPackages(0);
		mAppsList = new ArrayList<AppInfo>();
		lv_apps = (ListView)this.findViewById(R.id.lv_apps);
		mLayoutContainerLoading = (View)this.findViewById(R.id.loading_container);
		mLayoutContainerLoading.setVisibility(View.VISIBLE);
		lv_apps.setVisibility(View.INVISIBLE);
		Log.i(TAG, "onCreate");

		mInitListThread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!Thread.currentThread().isInterrupted())
				{
					Log.i(TAG, "Thread run() start initAppList");
					initAppsList();
				}
			}
		});
		mInitListThread.start();//启动初始化列表线程
	}
	
	
	/**
	 * 在程序刚启动的时候，初始化目标应用程序的各种数据
	 * 名称、图标、是否可以推送消息。因为有可能用户在管理应用程序这个
	 * 界面对是否可以推送消息进行了修改。
	 */
	private void initAppsList()
	{
		//为了更友好的显示程序，要求在初始化列表的时候显示一个滚动条来提示用户等待
		Log.i(TAG, "initAppsList");
		INotificationManager nm = INotificationManager.Stub.asInterface(
				ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		AppInfo tmpInfo = null;
		tempAppList = new ArrayList<AppInfo>();
		int size = mPackageInfoList.size();
		for(int i = 0; i < size; i ++)
		{
//			Log.i(TAG, "initAppsList i = " + i);
			PackageInfo packageInfo = mPackageInfoList.get(i);
			//将非系统应用添加到列表中来
			if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)== 0)
			{
				tmpInfo = new AppInfo(mContext);
				tmpInfo.appName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
				tmpInfo.packageName = packageInfo.packageName;
				tmpInfo.versionName = packageInfo.versionName;
				tmpInfo.versionCode = packageInfo.versionCode;
				tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(mPackageManager);
				
				//获取当前应用通知状态，是否可以发送通知
				try {
					tmpInfo.appCanNotification = nm.areNotificationsEnabledForPackage(tmpInfo.packageName);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					tmpInfo.appCanNotification = false;
				}
				tempAppList.add(tmpInfo);
			}
		}
		Log.i(TAG, "initAppsList send a message to show listview");
		Message message = new Message();
		message.what = MainActivity.SEND_INIT_LIST_MSG;
		message.obj = tempAppList;
		mHandler.sendMessage(message);
	}

	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case SEND_INIT_LIST_MSG:
				Log.i(TAG, "handleMessage SEND_INIT_LIST_MSG");
				//特别注意，在非UI线程下不能够对主线程中listview绑定的list数据进行修改。只能创建一个temlist，然后在
				//handle里面赋值。
				mAppsList = (ArrayList<AppInfo>) msg.obj;
				mAppsAdp = new AppsListAdapter(mContext, mAppsList);
				lv_apps.setAdapter(mAppsAdp);
				mAppsAdp.notifyDataSetChanged();
				mLayoutContainerLoading.setVisibility(View.INVISIBLE);
				lv_apps.setVisibility(View.VISIBLE);
				break;
			case SWITCH_LIST:
				mLayoutContainerLoading.setVisibility(View.INVISIBLE);
				lv_apps.setVisibility(View.VISIBLE);
				mAppsAdp.notifyDataSetChanged();
				lv_apps.invalidate();
				break;
			}
			super.handleMessage(msg);
		}
	};


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.finish();
	}
	
//	//在手指离开屏幕的时候打印mData来的内容。看看结果到底改变没有。only for test
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		// TODO Auto-generated method stub
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_UP:
//			for(int i = 0; i < mAppsList.size(); i ++)
//			{
//				Log.i(TAG, "onTouchEvent mApplist[" + i + "]:" 
//						+ ", pkgName = " + mAppsList.get(i).packageName
//						+ ", canNotify = " + mAppsList.get(i).appCanNotification);
//			}
//			
//			break;
//
//		default:
//			break;
//		}
//		return super.onTouchEvent(event);
//	}
	

	

}
