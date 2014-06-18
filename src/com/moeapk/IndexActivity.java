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
	
	//�б�����������
	IndexListAdapter indexlistadapter;
	
	MoeApkServer moeapk = new MoeApkServer(this);
	
	LocalInfo localinfo ;
	
	ArrayList<HashMap<String, String>> list_item_hashmap;
	
	String DeviceId;
	
	//Ӧ�������ļ���
	String MoeApk_PREF_NAME="MoeApk";
	//���ñ༭��
	SharedPreferences.Editor MoeApkSetEditor;
	//�����ļ���ȡ��
	SharedPreferences MoeApkSetReader;
	
	public String[] list_item ;
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        
        localinfo = new LocalInfo(this);
        
        
        //�������
        ActivityManager.getInstance().addActivity(IndexActivity.this);
        
        //���б���ʽ
        index_listview = (ListView) findViewById(R.id.index_list);
        
        //�趨���������
        index_listview.setOnItemClickListener(this);
        
		MoeApkSetEditor = this.getSharedPreferences(MoeApk_PREF_NAME, 2).edit();
		MoeApkSetReader = this.getSharedPreferences(MoeApk_PREF_NAME,1);
        
        //��ȡ����
        list_item = moeapk.App_Type;
        
        //�����б�
        ThreadGetList();
        FirstStart();
      //��API����������һ������������ͻ���
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

	//�����б���Ķ���
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		Log.i("DEBUG","����" + moeapk.App_Type_Id.length);
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
    			//����ҳ����ʾ
    			break;
    		}
    	}
	};
	
	private void doListView(ArrayList<HashMap<String, String>> array){
		IndexListAdapter adapter = new IndexListAdapter(this,array);
		index_listview.setAdapter(adapter);
	}
	
	//�ٰ�һ�η��ؼ��˳��Ķ���
	private long exitTime = 0; 
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){ 
			if((System.currentTimeMillis()-exitTime) > 2000){ 
				Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_LONG).show();
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
	
	///���ò���
	public boolean FirstStart(){
		boolean b;
		b = MoeApkSetReader.getBoolean("status_first_start", true); //���û�м�¼��Ϊ��һ��
		if(b){
			MoeApkSetEditor.putBoolean("status_first_start", false);//��һ�κ�϶����ǵ�һ��
			firstStartDialog();
			DeviceId = generalDeviceID();
			MoeApkSetEditor.putString("DeviceId", DeviceId);
			MoeApkSetEditor.commit();
			Log.i("DEBUG","��һ��ʹ��");
		}else{
			DeviceId = MoeApkSetReader.getString("DeviceId", null);
			Log.i("DEBUG","���ǵ�һ��ʹ�ã���ȡ��"+ DeviceId);
		}
		return b;
	}
	
	private String generalDeviceID(){
		Log.i("DeviceId","�����豸Id:"+System.currentTimeMillis());
		return String.valueOf(System.currentTimeMillis());
	}
	

}
