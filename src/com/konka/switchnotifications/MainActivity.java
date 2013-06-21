package com.konka.switchnotifications;

//import android.app.INotificationManager;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.os.ServiceManager;
import android.app.Activity;
import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.Menu;

public class MainActivity extends Activity {
//	private ApplicationsState mApplicationsState;
	PackageManager mPackageManager;
	List<PackageInfo> mPackageInfoList;
	ArrayList<AppInfo> mAppsList;//存放所有安装程序的数据
		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPackageManager = this.getPackageManager();
		mPackageInfoList = mPackageManager.getInstalledPackages(0);
		mAppsList = new ArrayList<AppInfo>();
		
		initAppsList();
	}
	
	private void initAppsList()
	{
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
				
				
				mAppsList.add(tmpInfo);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
