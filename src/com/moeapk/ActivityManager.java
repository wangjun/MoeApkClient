package com.moeapk;


import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

public class ActivityManager extends Application {
	// 存放activity的集合
	private ArrayList<Activity> list = new ArrayList<Activity>();
	private static ActivityManager instance;

	public ActivityManager() {
	}

	/**
	 * 利用单例模式获取MyAppalication实例
	 * 
	 * @return
	 */
	public static ActivityManager getInstance() {
		if (null == instance) {
			instance = new ActivityManager();
		}
		return instance;
	}

	/**
	 * 添加activity到list集合
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		list.add(activity);

	}

	/**
	 * 退出集合所有的activity
	 */
	public void exit() {
		try {
			for (Activity activity : list) {
				activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
}