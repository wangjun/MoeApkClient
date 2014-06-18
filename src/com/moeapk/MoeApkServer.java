package com.moeapk;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class MoeApkServer {
	public String IndexUrl(){
		return "http://moeapk.com/index_json.php";
	}
	
	public int current_type_code;
	//Ӧ���б�API��ַ
	public String AppListUrl(int type, int page){
		current_type_code = type;
		return "http://moeapk.com/app_general_list_json.php?type=" + type + "&page=" + page;
	}
	//ҳ��API��ַ
	public String AppListPageUrl(int type){
		current_type_code = type;
		return "http://moeapk.com/app_general_list_json.php?type=" + type + "&action=getpage";
	}
	
	public String current_app_package;
	//Ӧ����ϢAPI��ַ
	public String AppInfoUrl(String app_package){
		current_app_package = app_package;
		return "http://moeapk.com/app_general_info_json.php?package=" + app_package;
	}
	
	//Ӧ��ͼ��API��ַ
	//��Ҫͼ�ֻ꣬��Ҫ��package
	public String AppMainIconUrl(String app_package){
		return "http://cdn.moeapk.com/apk/" + app_package + "/ico_128.png";
	}
	
	public String AppMainIconUrl(String app_package,String size){
		if(size == "max") return "http://cdn.moeapk.com/apk/" + app_package + "/ico.png";
		if(size == "small") return "http://cdn.moeapk.com/apk/" + app_package + "/ico_64.png";
		return "http://cdn.moeapk.com/apk/" + app_package + "/ico_128.png";
	}
	//���汾ͼ��
	public String AppIconUrl(String app_package,String vcode,String special){
		return "http://cdn.moeapk.com/apk/" + app_package + "/" + app_package + "_" + vcode + "_" + special + ".png";
	}
	
	//APK��API��ַ
	public String ApkListUrl(String app_package){
		return "http://moeapk.com/apk_json.php?package=" + app_package;
	}
	
	//APK���ص�ַ
	public String ApkDownloadUrl(String app_package,String current_apklist_vcode2,String current_apklist_special2){
		return "http://moeapk.com/apk_download.php?from=androidclient&package=" + app_package + "&versioncode=" + current_apklist_vcode2 +"&special=" + current_apklist_special2;
	}
	
	//Ӧ�ý�ͼ����ͼ��ַ
	public String AppScreenshotThumbUrl(String hash){
		return "http://cdn.moeapk.com/thumbs/" + hash.substring(0, 2) + "/" + hash + ".jpg"; //����ͼ��cdn���ٵ�
	}
	
	//Ӧ�ý�ͼԭͼ��ַ
	public String AppScreenshotUrl(String hash,String ext){
		return "http://cdn.moeapk.com/images/" + hash.substring(0, 2) + "/" + hash + "." + ext;
	}
	
	//�ļ�����
	public FileUtils file = new FileUtils();
	
	
	//API�汾
	public int ApiVersion = 3;
	
	//�汾����ַ��Code
	public String updateCheckUrl = "http://api.moeapk.com/client/version";
	
	//API�汾����ַ
	public String updateCheckApi = "http://api.moeapk.com/client/apiversion";
	
	//��ȡ���°汾���ص�ַ
	public String updateDownloadUrl = "http://api.moeapk.com/client/update";
	
	//ע��ͻ��˵�ַ
	public String registerClientUrl = "http://api.moeapk.com/client/action/register/";
	
	//�õ����°汾���ص�ַ
	public String downloadUpdateUrl = "";
	
	//��ʼ���������ṩ�İ汾��
	int serverVersionCode = 0;
	
	//��ʼ������Ӧ�ð汾��
	int localVersionCode = 0;
	
	//����״̬��ö��
	int myStatus = 0;
	
	//����״̬
	int myInternet = 1;

	//��ʼ������Ӧ�ú�ϵͳ��Ϣ����
	LocalInfo localinfo;
	
	//��ʼ��ȫ�ִ�����
	Handler myHandler;
	int statusHandler = 0;
	
	//���¹���
	Context myContext;
	
	//Ӧ�÷���
	public String[] App_Type = {"ȫ��","��Ϸ","С����","��̬��ֽ","ʱ������","չʾƯ��","��Ϣ��Դ","����ģ��","����AR","ѧϰ�ο�","ƴͼ��ֽ","GalGame","��չ����","��ACG��"};
	public String[] App_Type_Id = {"0","1","2","3","4","5","6","7","8","9","10","11","12","999"};
	
	//���캯��
	protected MoeApkServer(Context context){
		//���ر���Ӧ�ú�ϵͳ��Ϣ��
		myContext = context;
		localinfo = new LocalInfo(myContext);
		
		myHandler = new Handler(){
			public void handleMessage(Message msg){
				switch (msg.what){
				case 0:
					if(myInternet == 1) ConnectFailedDialog();
					break;
				}
			}
		};
		
		
	}
	
	//�Ի�����ʾ����
	public void ConnectFailedDialog(){
    	Log.e("DEBGU","����ʧ��");
    	AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
    	builder.setTitle(R.string.dialog_title_internet_connect_failed);
    	builder.setMessage(R.string.dialog_message_internet_connect_failed);
		builder.setNeutralButton(R.string.button_exit,exit_button);
    	builder.create().show();
    }
	
	//�����ӶԻ���
	public void NoConnectionDialog(){
		Log.e("DEBGU","û������");
    	AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
    	builder.setTitle(R.string.dialog_title_internet_noconnect);
    	builder.setMessage(R.string.dialog_message_internet_noconnect);
    	builder.setPositiveButton(R.string.button_retry,ButtonRetryCheckConnection);
		builder.setNeutralButton(R.string.button_exit,exit_button);
    	builder.create().show();
	}
	
	//�����ӶԻ������԰�ť
	private DialogInterface.OnClickListener ButtonRetryCheckConnection = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if(localinfo.isNetworkConnected()){
				myInternet = 1;
			}else{
				CheckConnection();
			}
		}
	};
	
	//�����Ӿͷ�����ʾ
	public boolean CheckConnection(){
		if(!localinfo.isNetworkConnected()){
				myInternet = 0;
				NoConnectionDialog();
				return false;
		}
		return true;
	}
	
	//�˳���ť
	private DialogInterface.OnClickListener exit_button = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			ActivityManager.getInstance().exit();
		}
	};
	
	//����Ƿ����°汾���������
	public boolean checkUpdate(int myVersionCode){
		myStatus = 0; //��֤״̬
		statusHandler = 0;
		String returnString = null;
		Log.i("NET","��ʼ���汾����");
		
		
		//������Ϣ��ȡ�Լ�����
		if(localVersionCode == 0)
			localVersionCode = myVersionCode;
		
		if(serverVersionCode ==0){
					returnString = getStringFromUrl(updateCheckUrl);
					Log.i("DEBUG","����������:" + returnString );
		}
		//�������ʧ�ܣ�����Ϊ�޸��£������ظ�����
		if(returnString == "false" || myInternet == 0) return false;
		serverVersionCode = Integer.parseInt(returnString.replaceAll("\r|\n|\r\n", ""));		
		
		Log.i("DEBUG","���ذ汾�ţ�" + localVersionCode + " �������ϵİ汾�ţ�" + serverVersionCode);
		//�����ж�
		if(localVersionCode == serverVersionCode)
			return false;
		return true;
	}
	
	public boolean checkAPI(){
		int serverApiVersion;
		String returnString = null;
		Log.i("DEBUG","���API�汾");
		
		returnString = getStringFromUrl(updateCheckApi);
		if(returnString == "false" || myInternet == 0) return false;
		serverApiVersion = Integer.parseInt(returnString.replaceAll("\r|\n|\r\n", ""));
		if(serverApiVersion == ApiVersion) return false;
		return true;
		
	}
	
	//ע��ͻ���
	public void registerClient(String id){
		getStringFromUrl(registerClientUrl + id);
	}
	
	//��ȡ�°汾���ص�ַ
	public String getUpdateDownloadUrl(){
		return getStringFromUrl(updateDownloadUrl);
	}

	//�ӵ�ַ��ȡ�ı�
	public String getStringFromUrl(String url){
		Log.i("NetWork","���Դӷ�������ȡ�ַ���[" + url + "]" );
		URL myurl;
		String result;
		Message msg = new Message();
        byte[] data = null;
        //if(myInternet == 1){
        	try{
            	myurl = new URL(url);
            	HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            	conn.setConnectTimeout(6000);
            	conn.setDoInput(true);
            	conn.setUseCaches(false);
            	conn.connect();
            	InputStream is = conn.getInputStream();
            	data = readInputStream(is);
            	is.close();
            }catch (Exception e){
            	e.printStackTrace();
            }
        //}
        
        if(data != null){
        	result = new String(data);
        }else{
        	msg.what = 0;
    		msg.obj = null;
    		myInternet = 0;
    		myHandler.sendMessage(msg);
        	result = "false";
        }
        Log.i("DEBGU","�ӷ���������ı���" + result);
		return result;
	}
	
	//�ӵ�ַ��ȡBitmap
	public Bitmap getBitmapFromUrl(String url){
		Bitmap bitmap = null;
        URL myurl;
        try{
        	myurl = new URL(url);
        	HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
        	conn.setConnectTimeout(6000);
        	conn.setDoInput(true);
        	conn.setUseCaches(false);
        	conn.connect();
        	InputStream is = conn.getInputStream();
        	bitmap = BitmapFactory.decodeStream(is);
        	is.close();
        }catch (Exception e){
        	e.printStackTrace();
        }
        return bitmap;
	}
	
	//Ӧ���б��ַ���תJSON��ת��Hash����
	
	public ArrayList<HashMap<String, String>> Hash_AppList;
	public String[] current_applist_package;
	public int current_applist_total_page;
	
	
	public ArrayList<HashMap<String, String>> AppList_Json2HashArray(String json){
		current_applist_package = new String[10];
		
		
		Hash_AppList = new ArrayList<HashMap<String, String>>();
		JSONArray jsonarr = null;
        try {
        	//����������
			jsonarr = new JSONArray(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        for(int i=0;i<jsonarr.length();i++)
        {  
        	JSONObject temp;
			try {
				temp = (JSONObject) jsonarr.get(i);//��������ȡ����i����������
				HashMap<String, String> map = new HashMap<String, String>();  
				current_applist_package[i] = temp.getString("package");//���б�ÿ��package����һ�����鷽���б��������Ķ���
				map.put("Title", temp.getString("appname")); 
				map.put("Text", temp.getString("type") + " " + temp.getString("viewtime") + "�鿴"); 
				map.put("Icon", AppMainIconUrl(temp.getString("package"),"small"));
				map.put("Package", temp.getString("package"));
				Hash_AppList.add(map); 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        
        return Hash_AppList;
	}
	
	
	
	//�õ��б�ҳ��
	public void getAppListPage(){
		String json;
		JSONObject jsonobj;
		int type_code = current_type_code;
		json = getStringFromUrl(AppListPageUrl(type_code));
		try {
			jsonobj = new JSONObject(json);
			current_applist_total_page = jsonobj.getInt("total_page");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("DEBUG","����:" + type_code + "����ҳ��" + current_applist_total_page);
	}
	
	
	//APK�б��ַ���תJSON��ת��Hash����
	
	public ArrayList<HashMap<String, String>> Hash_ApkList;
	public String[] current_apklist_vcode;
	public String[] current_apklist_special;
	public String[] current_apklist_appname;
	public String[] current_apklist_vname;
	public String[] current_apklist_sname;
	
	
	public ArrayList<HashMap<String, String>> ApkList_Json2HashArray(String json){
		current_apklist_vcode = new String[100];
		current_apklist_special = new String[100];
		current_apklist_appname = new String[300];
		current_apklist_vname = new String[100];
		current_apklist_sname = new String[50];
		Hash_ApkList = new ArrayList<HashMap<String, String>>();
		JSONArray jsonarr = null;
        try {
        	//����������
			jsonarr = new JSONArray(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for(int i=0;i<jsonarr.length();i++)
        {  
        	JSONObject temp;
			try {
				temp = (JSONObject) jsonarr.get(i);//��������ȡ����i����������
				HashMap<String, String> map = new HashMap<String, String>();  
				current_apklist_vcode[i] = temp.getString("versioncode");
				current_apklist_special[i] = temp.getString("special");
				current_apklist_appname[i] = temp.getString("appname");
				current_apklist_vname[i] = temp.getString("versionname");
				current_apklist_sname[i] = temp.getString("special_name");
				map.put("Title", temp.getString("appname")); 
				map.put("Text", "�汾:" + temp.getString("versionname") + " " + temp.getString("special_name") + " " + temp.getString("size")); 
				map.put("Icon", AppIconUrl(temp.getString("package"),temp.getString("versioncode"),temp.getString("special")));
				map.put("Package", temp.getString("package"));
				map.put("VersionCode", temp.getString("versioncode"));
				map.put("SpecialCode", temp.getString("special"));
				Hash_ApkList.add(map); 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return Hash_ApkList;
	}

	public ArrayList<HashMap<String, String>> Hash_IndexList;
	
	public ArrayList<HashMap<String, String>> IndexList_Json2HashArray(String json){
		JSONArray jsonarr = null;
		Hash_IndexList = new ArrayList<HashMap<String, String>>();
		try {
			jsonarr = new JSONArray(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0;i<jsonarr.length();i++){
			JSONObject temp;
			try {
				temp = (JSONObject) jsonarr.get(i);//��������ȡ����i����������
				HashMap<String, String> map = new HashMap<String, String>();  
				map.put("Title", App_Type[i]); 
				//map.put("Text", "�汾:" + temp.getString("versionname") + " " + temp.getString("special_name") + " " + temp.getString("size")); 
				map.put("Icon", AppMainIconUrl(temp.getString("package"),"small"));
				map.put("Package", temp.getString("package"));
				Hash_IndexList.add(map); 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Hash_IndexList;
	}
	
	//����Ӧ����Ϣ
	public JSONObject getAppInfo(String app_package){
		String mystring = getStringFromUrl(AppInfoUrl(app_package));
		try {
			return new JSONObject(mystring);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	//��������������
	private static byte[] readInputStream(InputStream ImStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = ImStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		ImStream.close();
		return outStream.toByteArray();
	}
}
