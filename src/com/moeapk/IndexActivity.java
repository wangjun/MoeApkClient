package com.moeapk;



import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class IndexActivity extends Activity implements OnItemClickListener{
	ListView index_listview;
	
	MemoryCache memoryCache=new MemoryCache();   
	FileUtils fileUtils=new FileUtils();
	
	//列表适配器变量
	IndexListAdapter indexlistadapter;
	
	MoeApkServer moeapk = new MoeApkServer(this);
	
	LocalInfo localinfo ;
	
	ArrayList<HashMap<String, String>> list_item_hashmap;
	
	String DeviceId;
	
	//应用配置文件名
	String MoeApk_PREF_NAME="MoeApk";
	//配置编辑器
	SharedPreferences.Editor MoeApkSetEditor;
	//配置文件读取器
	SharedPreferences MoeApkSetReader;
	
	public String[] list_item ;
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        
        localinfo = new LocalInfo(this);
        
        
        //活动管理器
        ActivityManager.getInstance().addActivity(IndexActivity.this);
        
        //绑定列表样式
        index_listview = (ListView) findViewById(R.id.index_list);
        
        //设定点击监听器
        index_listview.setOnItemClickListener(this);
        
		MoeApkSetEditor = this.getSharedPreferences(MoeApk_PREF_NAME, 2).edit();
		MoeApkSetReader = this.getSharedPreferences(MoeApk_PREF_NAME,1);
        
        //获取分类
        list_item = moeapk.App_Type;
        
        //制造列表
        ThreadGetList();
        FirstStart();
      //向API服务器报告一次运行了这个客户端
      		moeapk.registerClient(DeviceId);
	}
	
	
	
	private void firstStartDialog(){
		//if(localinfo.FirstStart()){
		if(true){
			//moeapk.registerClient();
			AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
	    	builder.setTitle(R.string.dialog_firstrun_title);
	    	builder.setMessage(R.string.dialog_firstrun_text);
	    	builder.setPositiveButton(R.string.button_ok,clickFirstRunOK);
	    	builder.create().show();
		}
	}
	DialogInterface.OnClickListener clickFirstRunOK = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			Toast.makeText(getApplicationContext(), R.string.toast_welcome_to_use,Toast.LENGTH_LONG).show();
		}
	};

	//按下列表项的动作
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		Log.i("DEBUG","长度" + moeapk.App_Type_Id.length);
		intent.putExtra("type_code", Integer.parseInt(moeapk.App_Type_Id[position]));
		intent.putExtra("type_position", position);
		intent.setClass(IndexActivity.this,AppListActivity.class);
		startActivity(intent);
	}
	
	public void ThreadGetList(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 0;
				msg.obj = moeapk.IndexList_Json2HashArray(moeapk.getStringFromUrl(moeapk.IndexUrl()));
				handler.sendMessage(msg);
			}
		}).start();
	}
	
	private Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		switch(msg.what){
    		case 0:
    			doListView((ArrayList<HashMap<String, String>>) msg.obj);
    			//更新页数显示
    			break;
    		}
    	}
	};
	
	private void doListView(ArrayList<HashMap<String, String>> array){
		IndexListAdapter adapter = new IndexListAdapter(this,array);
		index_listview.setAdapter(adapter);
	}
	
	//再按一次返回键退出的动作
	private long exitTime = 0; 
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){ 
			if((System.currentTimeMillis()-exitTime) > 2000){ 
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_LONG).show();
				exitTime = System.currentTimeMillis(); 
			} else { 
				ActivityManager.getInstance().exit();
			} 
		return true; 
		} 
	return super.onKeyDown(keyCode, event); 
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
			Uri uri = Uri.parse("http://moeapk.com");
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
			myintent.setClass(IndexActivity.this,AboutActivity.class);
			startActivity(myintent);
			break;
		case R.id.menu_exit:
			ActivityManager.getInstance().exit();
			break;
		}
		return true;
	}
	
	///配置操作
	public boolean FirstStart(){
		boolean b;
		b = MoeApkSetReader.getBoolean("status_first_start", true); //如果没有记录则为第一次
		if(b){
			MoeApkSetEditor.putBoolean("status_first_start", false);//第一次后肯定不是第一次
			firstStartDialog();
			DeviceId = generalDeviceID();
			MoeApkSetEditor.putString("DeviceId", DeviceId);
			MoeApkSetEditor.commit();
			Log.i("DEBUG","第一次使用");
		}else{
			DeviceId = MoeApkSetReader.getString("DeviceId", null);
			Log.i("DEBUG","不是第一次使用，读取到"+ DeviceId);
		}
		return b;
	}
	
	private String generalDeviceID(){
		Log.i("DeviceId","产生设备Id:"+System.currentTimeMillis());
		return String.valueOf(System.currentTimeMillis());
	}
	

}
