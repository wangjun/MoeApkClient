package com.moeapk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class AppScreenshotActivity extends Activity{
	//定义服务器活动变量
	MoeApkServer moeapk = new MoeApkServer(this);
	
	private Intent intent;
	
	private String url;
	
	private ImageLoader imageloader;//图片加载器
	
	private ImageView screenshotView;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appscreenshot);
        
        
		//活动管理器
        ActivityManager.getInstance().addActivity(AppScreenshotActivity.this);
        
        imageloader = new ImageLoader(this);
        
        intent = getIntent(); 
        
        setTitle(getResources().getString(R.string.title_viewscreenshot));
        
        url = intent.getStringExtra("url");
        
        screenshotView = (ImageView) findViewById(R.id.screenshot);
        
        //改变图片显示方式
        screenshotView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageloader.setCacheAsFile(true);
		imageloader.setSaveFileName("截图_" + intent.getStringExtra("hash") + "_." + intent.getStringExtra("ext"));
        imageloader.DisplayImage(url, screenshotView);
	}
	
}
