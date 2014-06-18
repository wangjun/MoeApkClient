package com.moeapk;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class LocalInfo {
	Context context;
	
	
	//区分设备唯一Id
	public String DeviceId;
	
	

	
	public LocalInfo(Context in_context){
		context = in_context;
		
	}
	
	/*获取当前应用的版本号*/
	public int getVersionCode(){      
		try {
		      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	//获得版本名字
	public String getAppVersionName() {    
	     String versionName = "";    
	    try {    
	         // ---get the package info---    
	        PackageManager pm = context.getPackageManager();    
	         PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);    
	         versionName = pi.versionName;    
	         if (versionName == null || versionName.length() <= 0) {    
	             return "";    
	         }    
	     } catch (Exception e) {    
	         Log.e("VersionInfo", "Exception", e);    
	     }    
	     return versionName;    
	 }   
	
	//判断是否有网络连接
	public boolean isNetworkConnected() {  
		if (context != null) {  
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
				NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
				if (mNetworkInfo != null) {  
					return mNetworkInfo.isAvailable();  
				}  
		}  
		return false;  
	}
	
	//判断是否为WIFI
	public boolean isWifiConnected() {  
	    if (context != null) {  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                .getSystemService(Context.CONNECTIVITY_SERVICE);  
	        NetworkInfo mWiFiNetworkInfo = mConnectivityManager  
	                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
	        if (mWiFiNetworkInfo != null) {  
	            return mWiFiNetworkInfo.isAvailable();  
	        }  
	    }  
	    return false;  
	}
	
	//判断是否为流量
	public boolean isMobileConnected() {  
	    if (context != null) {  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                .getSystemService(Context.CONNECTIVITY_SERVICE);  
	        NetworkInfo mMobileNetworkInfo = mConnectivityManager  
	                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
	        if (mMobileNetworkInfo != null) {  
	            return mMobileNetworkInfo.isAvailable();  
	        }  
	    }  
	    return false;  
	}
	

	

}
