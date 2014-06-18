package com.moeapk;

import java.util.ArrayList;
import java.util.HashMap;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AppListActivity extends Activity implements OnItemClickListener{
	//��������������
	MoeApkServer moeapk = new MoeApkServer(this);
	
	MemoryCache memoryCache=new MemoryCache();   
	FileUtils fileUtils=new FileUtils();
		
	//չʾ�ķ�����
	public int type_code;
	public int type_position;//�÷����ڷ���ListView�е�λ��
	
	//ҳ��
	public int page = 1;
	
	//�б�View
	public ListView listview;
	
	//��Intent
	private Intent intent ;
	
	//��ҳ��ť
	private Button lastButton;
	private Button nextButton;
	private Button jumpButton;
	
	//��ҳ�����
	private EditText jumpEditText;
	
	//ҳ����ʾTestView
	private TextView showpage;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);

        //�������
        ActivityManager.getInstance().addActivity(AppListActivity.this);
        
        intent =  getIntent();
        listview = (ListView) findViewById(R.id.AppListView);
        //�õ���һ��Activity����������Ҫ��ʾ�ķ���ı��
        type_code = intent.getIntExtra("type_code", 0);
        type_position = intent.getIntExtra("type_position", 0);
        
        //�趨����
        setTitle(moeapk.App_Type[type_position]);
        
        //��ӵײ���ҳ��ť
        View footerview = getLayoutInflater().inflate(R.layout.list_button_pageswitch, null);
        listview.addFooterView(footerview);
        listview.setOnItemClickListener(this);
        
        lastButton = (Button) findViewById(R.id.list_last_page_button);
        nextButton = (Button) findViewById(R.id.list_next_page_button);
        jumpButton = (Button) findViewById(R.id.list_jump_page_button);
        showpage = (TextView) findViewById(R.id.list_page_show);
        
        jumpEditText = (EditText) findViewById(R.id.list_jump_page_editText);
        
        lastButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goLastPage();
			}});
        nextButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goNextPage();
			}});
        
        jumpButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				jumpPage();
			}});
        
        
        //��ʼ�����б�
        ThreadGetList();
	}
	
	public void ThreadGetList(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 0;
				msg.obj = moeapk.AppList_Json2HashArray(moeapk.getStringFromUrl(moeapk.AppListUrl(type_code, page)));
				//����һ��ҳ��
				moeapk.getAppListPage();
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
    			showpage.setText("��" + moeapk.current_applist_total_page + "ҳ��");
    			jumpEditText.setText(String.valueOf(page));
    			break;
    		}
    	}
	};
	
	private void doListView(ArrayList<HashMap<String, String>> array){
		AppListAdapter adapter = new AppListAdapter(this,array);
		listview.setAdapter(adapter);
	}
	
	//��һҳ
	private void goLastPage(){
		//��һҳ��������
		if(page == 1){
			Toast.makeText(getApplicationContext(), R.string.toast_on_the_first_page,Toast.LENGTH_SHORT).show();
		}else{
			page--;
			ThreadGetList();
		}
	}
	
	//��һҳ
	private void goNextPage(){
		//���ﵽ��ҳ�����򲻽��з�ҳ
		if(page == moeapk.current_applist_total_page){
			Toast.makeText(getApplicationContext(), R.string.toast_on_the_last_page,Toast.LENGTH_SHORT).show();
		}else{
			page++;
			ThreadGetList();
		}
	}
	
	//��ҳ
	private void jumpPage(){
		int input_page;
		input_page = Integer.valueOf(jumpEditText.getText().toString());
		if(input_page < 1) input_page=1;
		if(input_page > moeapk.current_applist_total_page) input_page=moeapk.current_applist_total_page;
		page = input_page;
		ThreadGetList();
	}
	
	//��������б���ķ���
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent appinfoIntent = new Intent();
        appinfoIntent.putExtra("info_package", moeapk.current_applist_package[position]);
        appinfoIntent.setClass(AppListActivity.this, AppInfoActivity.class);
        startActivity(appinfoIntent);
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
			Uri uri = Uri.parse("http://moeapk.com/list/" + type_code + "/" + page);
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
			myintent.setClass(AppListActivity.this,AboutActivity.class);
			startActivity(myintent);
			break;
		case R.id.menu_exit:
			ActivityManager.getInstance().exit();
			break;
		}
		return true;
	}
}
