package com.konka.switchnotifications.widget;

import com.konka.switchnotifications.MainActivity;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SwitchOnCheckedChangeListener implements OnCheckedChangeListener {

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked, String pkgName)
	{
		Log.e(MainActivity.TAG, "buttonView is " + buttonView.toString() 
				+ ", isChecked = " + isChecked
				+ ", pkgName = " + pkgName);
		onCheckedChanged(buttonView, isChecked);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

}
