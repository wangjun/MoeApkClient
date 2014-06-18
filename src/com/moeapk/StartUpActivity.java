package com.moeapk;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;



public class StartUpActivity extends Activity {
	//定义服务器活动变量
	private MoeApkServer moeapk;
	
	//定义本地信息变量
	LocalInfo localinfo;
	
	FileUtils fileUtils=new FileUtils();
	
	//自身版本号
	public int myVersionCode;
	
	//联网检查状态
	private int checkStatus = 0;
	
	//此类全局处理器
	private Handler myHandler;
	
	//连接可用状态
	private int internetStatus = 0;
	
	//等待时间
	private static final long STATUP_WAIT_TIME = 3000;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //活动管理器
        ActivityManager.getInstance().addActivity(StartUpActivity.this);
        
        /*set it to be no title*/ 
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏窗口，全屏显示图片
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效*/ 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.activity_start_up);

        
        myHandler = new Handler(){
        	public void handleMessage(Message msg){
        		switch (msg.what){
        		case 0://检查更新
        			if((Boolean) msg.obj){
            			updateCheckDialog();
            		}else{
            			checkStatus++;
            		}
        			
        			break;
        		case 1://检查API
        			if((Boolean) msg.obj){
        				updateApiDialog();
        			}else{
        				checkStatus++;
        			}
        			break;
        		}
        		Log.i("DEBUG","此次Handler后checkStatus的值为" + checkStatus);
        		if(checkStatus == 2){
        			
        			goToIndexActivity_wait();
        		}
        	}
        };
        moeapk = new MoeApkServer(this);
        localinfo = new LocalInfo(this);
        
        myVersionCode = localinfo.getVersionCode();
        
        //检查连接
        if(!moeapk.CheckConnection()) internetStatus = 1;
        
        //新建线程检查更新
        new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.obj = moeapk.checkUpdate(myVersionCode);
				msg.what = 0;
				myHandler.sendMessage(msg);
			}
        }).start();
        
        //检查API版本
        new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.obj = moeapk.checkAPI();
				msg.what = 1;
				myHandler.sendMessage(msg);
			}
        }).start();
        
        
    }

    //升级提示对话框
    private void updateCheckDialog(){
    	Log.i("DEBGU","触发升级确认对话框");
    	AlertDialog.Builder builder = new AlertDialog.Builder(StartUpActivity.this);
    	builder.setTitle(R.string.dialog_title_info);
    	builder.setMessage(R.string.dialog_message_checkupdate);
    	builder.setPositiveButton(R.string.button_yes,buttonOnClickUpdateYes);
		builder.setNeutralButton(R.string.button_no,buttonOnClickUpdateNo);
		builder.setCancelable(false);
    	builder.create().show();
    }
    
    private void updateApiDialog(){
    	Log.i("DEBGU","触发升级确认对话框");
    	AlertDialog.Builder builder = new AlertDialog.Builder(StartUpActivity.this);
    	builder.setTitle(R.string.dialog_title_api_update);
    	builder.setMessage(R.string.dialog_message_api_update);
    	builder.setPositiveButton(R.string.button_update,buttonOnClickUpdateYes);
		builder.setNeutralButton(R.string.button_exit,buttonOnClickApiExit);
		builder.setCancelable(false);
    	builder.create().show();
    }
    
    
    //升级提示对话框“是”按钮监听器
    private DialogInterface.OnClickListener buttonOnClickUpdateYes = new DialogInterface.OnClickListener(){
    	public void onClick(DialogInterface dialog, int whichButton) {
    		myHandler = new Handler(){
    			public void handleMessage(Message msg){
    				String myUrl = String.valueOf(msg.obj);
    				Uri uri = Uri.parse(myUrl);
    				DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    				DownloadManager.Request request = new DownloadManager.Request(uri);
    				request.setDestinationInExternalPublicDir("MoeApk", "萌萌安卓.apk");
    				request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);//允许流量和wifi使用
    				request.setAllowedOverRoaming(false);//不允许在漫游时下载
    				request.setMimeType("application/vnd.android.package-archive");
    				request.setTitle("萌萌安卓");
    				request.setDescription("萌萌安卓客户端更新");
    				request.setVisibleInDownloadsUi(true);
    				long downloadid = mgr.enqueue(request);
    				Toast.makeText(getApplicationContext(), "文件将保存在" + fileUtils.SDCARD + "MoeApk文件夹下", Toast.LENGTH_LONG).show();
    			}
    		};
    		if(!moeapk.CheckConnection()) internetStatus = 1;
    		new Thread(new Runnable(){
    			public void run(){
    				Message msg = new Message();
    				msg.obj = moeapk.getUpdateDownloadUrl();
    				myHandler.sendMessage(msg);
    			}
    		}).start();
		}
    };
    
    //升级提示对话框“否”按钮监听器
    private DialogInterface.OnClickListener buttonOnClickUpdateNo = new DialogInterface.OnClickListener(){
    	public void onClick(DialogInterface dialog, int whichButton) {
    		//有升级但不选择更新就继续前往主活动
    		if(checkStatus != 2)
    			goToIndexActivity();
		}
    };
    
    //API更新提示对话框“退出”按钮监听器
    private DialogInterface.OnClickListener buttonOnClickApiExit = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			ActivityManager.getInstance().exit();
		}
	};
	
	
	//前进到主活动
	private void goToIndexActivity(){
		//启动新活动
		if(internetStatus ==0 ){ //如果有网，才继续
			Intent intent = new Intent(StartUpActivity.this,IndexActivity.class);
		startActivity(intent);
		}else{
			Log.i("Debug","无网络");
		}
	}
	
	//到主活动前的等待
	private void goToIndexActivity_wait(){
		new Handler().postDelayed(new Runnable() {
            public void run() {
                goToIndexActivity();
            }
        }, STATUP_WAIT_TIME);
	}
	


    @Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){ 
			//按下返回按钮即立即退出
			ActivityManager.getInstance().exit();
			return true; 
		} 
	return super.onKeyDown(keyCode, event); 
	} 
    
}
