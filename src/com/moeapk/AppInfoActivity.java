package com.moeapk;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppInfoActivity extends Activity implements OnItemClickListener{
	//定义服务器活动变量
	MoeApkServer moeapk = new MoeApkServer(this);
	
	MemoryCache memoryCache=new MemoryCache();   
	FileUtils fileUtils=new FileUtils();
	
	//网络图片加载器
	ImageLoader imageloader ;
	
	//应用信息：JSONObject格式
	JSONObject AppInfo;
	
	//应用图标
	Bitmap bitmap_icon;
	
	//列表View
	public ListView listview;
		
	//此Intent
	private Intent intent ;
	
	//要显示的应用的package
	private String app_package;
	
	//应用图标
	ImageView app_icon;
	
	//应用标题
	TextView app_name;
	
	//应用分类
	TextView app_type;
	
	//信息文本
	TextView app_text;
	
	//按钮：下载
	Button app_button_download;
	
	//按钮：评论
	Button app_button_forum;
	
	//按钮：信息
	Button app_button_info;
	
	//按钮：介绍
	Button app_button_text;
	
	//按钮：更新
	Button app_button_news;
	
	//按钮：市场
	Button app_button_googleplay;
	
	//画廊：应用截图
	Gallery app_screenshot;
	
	//截图数据数组
	private ArrayList<HashMap<String, String>> ScreenshotHashMap;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appinfo);
		
		//活动管理器
        ActivityManager.getInstance().addActivity(AppInfoActivity.this);
		
		setTitle(getResources().getString(R.string.title_appinfo));
		
		intent =  getIntent();
		imageloader = new ImageLoader(this);
		
		//得到要显示的应用的包名称
		app_package = intent.getStringExtra("info_package");
		Log.i("DEBUG","要显示的Package：" + app_package);
		
		//各个元素绑定
		app_icon = (ImageView) findViewById(R.id.appinfo_icon);
		app_name = (TextView) findViewById(R.id.appinfo_name);
		app_type = (TextView) findViewById(R.id.appinfo_type);
		app_text = (TextView) findViewById(R.id.appinfo_text);
		app_button_download = (Button) findViewById(R.id.appinfo_button_download);
		app_button_forum = (Button) findViewById(R.id.appinfo_button_forum);
		app_button_info = (Button) findViewById(R.id.appinfo_button_info);
		app_button_text = (Button) findViewById(R.id.appinfo_button_text);
		app_button_news = (Button) findViewById(R.id.appinfo_button_news);
		app_button_googleplay = (Button) findViewById(R.id.appinfo_button_googleplay);
		app_screenshot = (Gallery) findViewById(R.id.appinfo_screenshot);
		
		//按钮设定监听器
		app_button_download.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Button_download();
			}
			
		});
		
		app_button_forum.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Button_forum();
			}
			
		});
		
		app_button_info.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Button_info();
			}
			
		});
		app_button_text.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Button_text();
			}
			
		});
		app_button_news.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Button_news();
			}
			
		});
		app_button_googleplay.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Button_googleplay();
			}
			
		});
		
		//设定截图Gallery监听器
		app_screenshot.setOnItemClickListener(this);
		
		
		//调用多线程方法获取信息
		ThreadGetInfo();
		ThreadGetIcon();
		
	}
	
	//多线程结果处理器
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case 0:
				AppInfo = (JSONObject) msg.obj;
				Show_info();
				Show_screenshot();
				break;
			case 1:
				bitmap_icon = (Bitmap) msg.obj;
				Show_icon();
				break;	
			}
			
		}
	};
	
	
	//新建线程获取信息
	private void ThreadGetInfo(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 0;
				msg.obj = moeapk.getAppInfo(app_package);
				handler.sendMessage(msg);
			}
			
		}).start();
	}
	//新建线程获取图标
	private void ThreadGetIcon(){
		/*
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 1;
				msg.obj = moeapk.getBitmapFromUrl(moeapk.AppIconUrl(app_package));
				handler.sendMessage(msg);
			}
			
		}).start();*/
		imageloader.setCacheAsFile(true);
		imageloader.setSaveFileName("Icon_" + app_package + "_normal.png");
		imageloader.DisplayImage(moeapk.AppMainIconUrl(app_package), app_icon);
	}
	
	private void Show_info(){
		try {
			app_name.setText(AppInfo.getString("appname"));
			app_type.setText(AppInfo.getString("type"));
			//来一次“信息”按钮 的动作来显示信息
			Button_info();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void Show_icon(){
		app_icon.setImageBitmap(bitmap_icon);
	}
	
	private void Show_screenshot(){
		AppInfoScreenshotAdapter screenshotadapter = new AppInfoScreenshotAdapter(this,getScreenshotArrayList());
		app_screenshot.setAdapter(screenshotadapter);
		
	}
	
	//按钮：下载
	private void Button_download(){
		Intent apkListIntent = new Intent();
		apkListIntent.putExtra("app_package", app_package);
		apkListIntent.setClass(AppInfoActivity.this, ApkListActivity.class);
		Log.i("DEBUG","下载按钮");
		startActivity(apkListIntent);
	}
	
	private void Button_forum(){
		Uri uri;
		try {
			uri = Uri.parse("http://bbs.moeapk.com/thread-" + AppInfo.getString("forum_tid") + "-1-1.html");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//按钮：信息
	private void Button_info(){
		String mystring = getResources().getString(R.string.textview_get_app_info_failed);
		try {
			mystring = getResources().getString(R.string.textview_app_developer) + "：" + AppInfo.getString("maker") + "\n" +
		getResources().getString(R.string.textview_app_views) + "：" + AppInfo.getString("viewtime") + "\n" +
		getResources().getString(R.string.textview_app_downloads) + "：" + AppInfo.getString("download_time") + "\n" +
		getResources().getString(R.string.textview_app_update_time) + "：" + AppInfo.getString("newtime") + "\n" +
		getResources().getString(R.string.textview_app_system_require) + "：" + AppInfo.getString("android") + "\n" +
		getResources().getString(R.string.textview_app_age_classification) + "：" + AppInfo.getString("age");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		app_text.setText(mystring);
	}
	
	//按钮：介绍
	private void Button_text(){
		try {
			app_text.setText(Html.fromHtml(AppInfo.getString("alltext")));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//按钮：更新
	private void Button_news(){
		try {
			app_text.setText(Html.fromHtml(AppInfo.getString("newinfo")));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//按钮：菜市场
	private void Button_googleplay(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + app_package));
		startActivity(intent);
	}
	
	//获取截图数组
	private ArrayList<HashMap<String, String>> getScreenshotArrayList(){
		ScreenshotHashMap = new ArrayList<HashMap<String, String>>();
		JSONArray screenshot = null;
		try {
			screenshot = AppInfo.getJSONArray("screenshot");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			HashMap<String, String> map = new HashMap<String, String>(); 
			map.put("hash", "aaa");
			map.put("ext", "aaa");
			ScreenshotHashMap.add(map);
			return ScreenshotHashMap;
		}

		
		JSONObject temp;
		
			try {
				
				for(int i=0;i<screenshot.length();i++){
					temp = (JSONObject) screenshot.getJSONObject(i);
					HashMap<String, String> map = new HashMap<String, String>(); 
					map.put("hash", temp.getString("hash"));
					map.put("ext", temp.getString("ext"));
					ScreenshotHashMap.add(map);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		
		return ScreenshotHashMap;
	}
	
	//点击截图后的动作
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Log.i("DEBUG","点击了第" + position + "个截图");
		HashMap<String, String> temp = new HashMap<String, String>();
		temp = ScreenshotHashMap.get(position);
		Intent app_screenshot_intent = new Intent();
		app_screenshot_intent.putExtra("url", moeapk.AppScreenshotUrl(temp.get("hash"), temp.get("ext")));
		app_screenshot_intent.putExtra("hash", temp.get("hash"));
		app_screenshot_intent.putExtra("ext", temp.get("ext"));
		app_screenshot_intent.setClass(AppInfoActivity.this, AppScreenshotActivity.class);
		startActivity(app_screenshot_intent);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.menu_website:
			Uri uri = Uri.parse("http://moeapk.com/info/" + app_package);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		case R.id.menu_clear_cache:
			memoryCache.clear();
			fileUtils.clearCache();
			Toast.makeText(getApplicationContext(), R.string.toast_clear_cache_finish, Toast.LENGTH_LONG).show();
			break;
		case R.id.menu_about:
			Intent myintent = new Intent();
			myintent.setClass(AppInfoActivity.this,AboutActivity.class);
			startActivity(myintent);
			break;
		case R.id.menu_exit:
			ActivityManager.getInstance().exit();
			break;
		}
		return true;
	}
}

