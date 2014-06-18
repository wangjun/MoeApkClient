package com.moeapk;


import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

public class ActivityManager extends Application {
	// ���activity�ļ���
	private ArrayList<Activity> list = new ArrayList<Activity>();
	private static ActivityManager instance;

	public ActivityManager() {
	}

	/**
	 * ���õ���ģʽ��ȡMyAppalicationʵ��
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
	 * ���activity��list����
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		list.add(activity);

	}

	/**
	 * �˳��������е�activity
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