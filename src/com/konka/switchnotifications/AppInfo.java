package com.konka.switchnotifications;

import android.app.INotificationManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class AppInfo {
	private Context mContext;
	private INotificationManager nm;
	public String appName = "";
	public String packageName = "";
	public String versionName = "";
	public int versionCode = 0;
	public Drawable appIcon = null;
	public boolean appCanNotification;
	
	public AppInfo(Context context)
	{
		this.mContext = context;
	}
	
	/**
	 * 打开关闭应用程序的通知功能。
	 * @param isChecked 是否打开通知功能
	 */
	public void setNotify(boolean isChecked)
	{
		nm = INotificationManager.Stub.asInterface(
				ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		try {
			nm.setNotificationsEnabledForPackage(packageName, isChecked);
			Log.i(MainActivity.TAGS, "ifCanNotify pkgName = " + packageName
					+ ", isChecked = " + isChecked
					+ ", 系统设置中通知开关 = " + nm.areNotificationsEnabledForPackage(packageName));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	public void print()
	{
        Log.v("app","Name:"+appName+" Package:"+packageName);
        Log.v("app","Name:"+appName+" versionName:"+versionName);
        Log.v("app","Name:"+appName+" versionCode:"+versionCode);
	}
}
