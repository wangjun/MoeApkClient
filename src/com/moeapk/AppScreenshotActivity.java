package com.moeapk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class AppScreenshotActivity extends Activity{
	//��������������
	MoeApkServer moeapk = new MoeApkServer(this);
	
	private Intent intent;
	
	private String url;
	
	private ImageLoader imageloader;//ͼƬ������
	
	private ImageView screenshotView;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appscreenshot);
        
        
		//�������
        ActivityManager.getInstance().addActivity(AppScreenshotActivity.this);
        
        imageloader = new ImageLoader(this);
        
        intent = getIntent(); 
        
        setTitle(getResources().getString(R.string.title_viewscreenshot));
        
        url = intent.getStringExtra("url");
        
        screenshotView = (ImageView) findViewById(R.id.screenshot);
        
        //�ı�ͼƬ��ʾ��ʽ
        screenshotView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageloader.setCacheAsFile(true);
		imageloader.setSaveFileName("��ͼ_" + intent.getStringExtra("hash") + "_." + intent.getStringExtra("ext"));
        imageloader.DisplayImage(url, screenshotView);
	}
	
}
