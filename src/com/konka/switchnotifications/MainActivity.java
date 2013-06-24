package com.konka.switchnotifications;

//import android.app.INotificationManager;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends Activity {
//	private ApplicationsState mApplicationsState;
	private PackageManager mPackageManager;
	private List<PackageInfo> mPackageInfoList;
	private ArrayList<AppInfo> mAppsList;//存放所有安装程序的数据
	private ListView lv_apps;
	private AppsListAdapter mAppsAdp;
	private View mLayoutContainerLoading;
		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPackageManager = this.getPackageManager();
		mPackageInfoList = mPackageManager.getInstalledPackages(0);
		mAppsList = new ArrayList<AppInfo>();
		lv_apps = (ListView)this.findViewById(R.id.lv_apps);
		mLayoutContainerLoading = (View)this.findViewById(R.id.loading_container);
		mLayoutContainerLoading.setVisibility(View.VISIBLE);
		lv_apps.setVisibility(View.INVISIBLE);
		initAppsList();
//		mAppsAdp = new AppsListAdapter(this.getApplicationContext(), mAppsList);///////
//		lv_apps.setAdapter(mAppsAdp);
	}
	
	/**
	 * 在程序刚启动的时候，初始化目标应用程序的各种数据
	 * 名称、图标、是否可以推送消息。因为有可能用户在管理应用程序这个
	 * 界面对是否可以推送消息进行了修改。
	 */
	private void initAppsList()
	{
		//为了更友好的显示程序，要求在初始化列表的时候显示一个滚动条来提示用户等待
		INotificationManager nm = INotificationManager.Stub.asInterface(
				ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		for(int i = 0; i < mPackageInfoList.size(); i ++)
		{
			PackageInfo packageInfo = mPackageInfoList.get(i);
			AppInfo tmpInfo;
			//将非系统应用添加到列表中来
			if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)== 0)
			{
				tmpInfo = new AppInfo();
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
				
				mAppsList.add(tmpInfo);
			}
		}
		mAppsAdp = new AppsListAdapter(this.getApplicationContext(), mAppsList);///////
		lv_apps.setAdapter(mAppsAdp);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
