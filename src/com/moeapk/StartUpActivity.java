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
	//��������������
	private MoeApkServer moeapk;
	
	//���屾����Ϣ����
	LocalInfo localinfo;
	
	FileUtils fileUtils=new FileUtils();
	
	//����汾��
	public int myVersionCode;
	
	//�������״̬
	private int checkStatus = 0;
	
	//����ȫ�ִ�����
	private Handler myHandler;
	
	//���ӿ���״̬
	private int internetStatus = 0;
	
	//�ȴ�ʱ��
	private static final long STATUP_WAIT_TIME = 3000;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //�������
        ActivityManager.getInstance().addActivity(StartUpActivity.this);
        
        /*set it to be no title*/ 
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //���ش��ڣ�ȫ����ʾͼƬ
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**����������View�ģ����Դ������е����β��ֱ����غ������Ȼ��Ч*/ 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.activity_start_up);

        
        myHandler = new Handler(){
        	public void handleMessage(Message msg){
        		switch (msg.what){
        		case 0://������
        			if((Boolean) msg.obj){
            			updateCheckDialog();
            		}else{
            			checkStatus++;
            		}
        			
        			break;
        		case 1://���API
        			if((Boolean) msg.obj){
        				updateApiDialog();
        			}else{
        				checkStatus++;
        			}
        			break;
        		}
        		Log.i("DEBUG","�˴�Handler��checkStatus��ֵΪ" + checkStatus);
        		if(checkStatus == 2){
        			
        			goToIndexActivity_wait();
        		}
        	}
        };
        moeapk = new MoeApkServer(this);
        localinfo = new LocalInfo(this);
        
        myVersionCode = localinfo.getVersionCode();
        
        //�������
        if(!moeapk.CheckConnection()) internetStatus = 1;
        
        //�½��̼߳�����
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
        
        //���API�汾
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

    //������ʾ�Ի���
    private void updateCheckDialog(){
    	Log.i("DEBGU","��������ȷ�϶Ի���");
    	AlertDialog.Builder builder = new AlertDialog.Builder(StartUpActivity.this);
    	builder.setTitle(R.string.dialog_title_info);
    	builder.setMessage(R.string.dialog_message_checkupdate);
    	builder.setPositiveButton(R.string.button_yes,buttonOnClickUpdateYes);
		builder.setNeutralButton(R.string.button_no,buttonOnClickUpdateNo);
		builder.setCancelable(false);
    	builder.create().show();
    }
    
    private void updateApiDialog(){
    	Log.i("DEBGU","��������ȷ�϶Ի���");
    	AlertDialog.Builder builder = new AlertDialog.Builder(StartUpActivity.this);
    	builder.setTitle(R.string.dialog_title_api_update);
    	builder.setMessage(R.string.dialog_message_api_update);
    	builder.setPositiveButton(R.string.button_update,buttonOnClickUpdateYes);
		builder.setNeutralButton(R.string.button_exit,buttonOnClickApiExit);
		builder.setCancelable(false);
    	builder.create().show();
    }
    
    
    //������ʾ�Ի����ǡ���ť������
    private DialogInterface.OnClickListener buttonOnClickUpdateYes = new DialogInterface.OnClickListener(){
    	public void onClick(DialogInterface dialog, int whichButton) {
    		myHandler = new Handler(){
    			public void handleMessage(Message msg){
    				String myUrl = String.valueOf(msg.obj);
    				Uri uri = Uri.parse(myUrl);
    				DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    				DownloadManager.Request request = new DownloadManager.Request(uri);
    				request.setDestinationInExternalPublicDir("MoeApk", "���Ȱ�׿.apk");
    				request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);//����������wifiʹ��
    				request.setAllowedOverRoaming(false);//������������ʱ����
    				request.setMimeType("application/vnd.android.package-archive");
    				request.setTitle("���Ȱ�׿");
    				request.setDescription("���Ȱ�׿�ͻ��˸���");
    				request.setVisibleInDownloadsUi(true);
    				long downloadid = mgr.enqueue(request);
    				Toast.makeText(getApplicationContext(), "�ļ���������" + fileUtils.SDCARD + "MoeApk�ļ�����", Toast.LENGTH_LONG).show();
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
    
    //������ʾ�Ի��򡰷񡱰�ť������
    private DialogInterface.OnClickListener buttonOnClickUpdateNo = new DialogInterface.OnClickListener(){
    	public void onClick(DialogInterface dialog, int whichButton) {
    		//����������ѡ����¾ͼ���ǰ�����
    		if(checkStatus != 2)
    			goToIndexActivity();
		}
    };
    
    //API������ʾ�Ի����˳�����ť������
    private DialogInterface.OnClickListener buttonOnClickApiExit = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			ActivityManager.getInstance().exit();
		}
	};
	
	
	//ǰ�������
	private void goToIndexActivity(){
		//�����»
		if(internetStatus ==0 ){ //����������ż���
			Intent intent = new Intent(StartUpActivity.this,IndexActivity.class);
		startActivity(intent);
		}else{
			Log.i("Debug","������");
		}
	}
	
	//�����ǰ�ĵȴ�
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
			//���·��ذ�ť�������˳�
			ActivityManager.getInstance().exit();
			return true; 
		} 
	return super.onKeyDown(keyCode, event); 
	} 
    
}
