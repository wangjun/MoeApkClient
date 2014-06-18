package com.moeapk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ApkListActivity extends Activity implements OnItemClickListener{
	//定义服务器活动变量
	MoeApkServer moeapk = new MoeApkServer(this);
	
	MemoryCache memoryCache=new MemoryCache();   
	FileUtils fileUtils=new FileUtils();
		
	//列表View
	public ListView listview;
	
	//显示的package
	private String app_package;

	//此Intent
	private Intent intent ;
	
	
	protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
        
        //活动管理器
        ActivityManager.getInstance().addActivity(ApkListActivity.this);
        
        setTitle(getResources().getString(R.string.title_choose_version));
        Log.i("DEBUG","进入APK库");
        intent =  getIntent();
        listview = (ListView) findViewById(R.id.AppListView);
        listview.setOnItemClickListener(this);
        app_package = intent.getStringExtra("app_package");
        ThreadGetList();
	}
	
	
	public void ThreadGetList(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 0;
				msg.obj = moeapk.ApkList_Json2HashArray(moeapk.getStringFromUrl(moeapk.ApkListUrl(app_package)));
				handler.sendMessage(msg);
			}
		}).start();
	}
	
	private Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		switch(msg.what){
    		case 0:
    			doListView((ArrayList<HashMap<String, String>>) msg.obj);
    			break;
    		}
    	}
	};

	private void doListView(ArrayList<HashMap<String, String>> array){
		AppListAdapter adapter = new AppListAdapter(this,array);
		adapter.setShowForMain(false);
		listview.setAdapter(adapter);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Uri uri = Uri.parse(moeapk.ApkDownloadUrl(app_package, moeapk.current_apklist_vcode[position], moeapk.current_apklist_special[position]));
		//Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		//startActivity(intent);
		
		DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(uri);
		request.setDestinationInExternalPublicDir("MoeApk", moeapk.current_apklist_appname[position] + "_" + moeapk.current_apklist_vname[position] + "_" + moeapk.current_apklist_sname[position] + ".apk");
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);//允许流量和wifi使用
		request.setAllowedOverRoaming(false);//不允许在漫游时下载
		request.setMimeType("application/vnd.android.package-archive");
		request.setTitle(moeapk.current_apklist_appname[position]);
		request.setDescription("来自萌萌安卓的下载");
		request.setVisibleInDownloadsUi(true);
		long downloadid = mgr.enqueue(request);
		Toast.makeText(getApplicationContext(), "文件将保存在" + fileUtils.SDCARD + "MoeApk下", Toast.LENGTH_LONG).show();
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
			Uri uri = Uri.parse("http://moeapk.com/apk/" + app_package + ".html");
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
			myintent.setClass(ApkListActivity.this,AboutActivity.class);
			startActivity(myintent);
			break;
		case R.id.menu_exit:
			ActivityManager.getInstance().exit();
			break;
		}
		return true;
	}
}
