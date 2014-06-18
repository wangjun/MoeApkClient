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
	//应用列表API地址
	public String AppListUrl(int type, int page){
		current_type_code = type;
		return "http://moeapk.com/app_general_list_json.php?type=" + type + "&page=" + page;
	}
	//页数API地址
	public String AppListPageUrl(int type){
		current_type_code = type;
		return "http://moeapk.com/app_general_list_json.php?type=" + type + "&action=getpage";
	}
	
	public String current_app_package;
	//应用信息API地址
	public String AppInfoUrl(String app_package){
		current_app_package = app_package;
		return "http://moeapk.com/app_general_info_json.php?package=" + app_package;
	}
	
	//应用图标API地址
	//主要图标，只需要传package
	public String AppMainIconUrl(String app_package){
		return "http://cdn.moeapk.com/apk/" + app_package + "/ico_128.png";
	}
	
	public String AppMainIconUrl(String app_package,String size){
		if(size == "max") return "http://cdn.moeapk.com/apk/" + app_package + "/ico.png";
		if(size == "small") return "http://cdn.moeapk.com/apk/" + app_package + "/ico_64.png";
		return "http://cdn.moeapk.com/apk/" + app_package + "/ico_128.png";
	}
	//各版本图标
	public String AppIconUrl(String app_package,String vcode,String special){
		return "http://cdn.moeapk.com/apk/" + app_package + "/" + app_package + "_" + vcode + "_" + special + ".png";
	}
	
	//APK库API地址
	public String ApkListUrl(String app_package){
		return "http://moeapk.com/apk_json.php?package=" + app_package;
	}
	
	//APK下载地址
	public String ApkDownloadUrl(String app_package,String current_apklist_vcode2,String current_apklist_special2){
		return "http://moeapk.com/apk_download.php?from=androidclient&package=" + app_package + "&versioncode=" + current_apklist_vcode2 +"&special=" + current_apklist_special2;
	}
	
	//应用截图缩略图地址
	public String AppScreenshotThumbUrl(String hash){
		return "http://cdn.moeapk.com/thumbs/" + hash.substring(0, 2) + "/" + hash + ".jpg"; //缩略图是cdn加速的
	}
	
	//应用截图原图地址
	public String AppScreenshotUrl(String hash,String ext){
		return "http://cdn.moeapk.com/images/" + hash.substring(0, 2) + "/" + hash + "." + ext;
	}
	
	//文件处理
	public FileUtils file = new FileUtils();
	
	
	//API版本
	public int ApiVersion = 3;
	
	//版本检查地址，Code
	public String updateCheckUrl = "http://api.moeapk.com/client/version";
	
	//API版本检查地址
	public String updateCheckApi = "http://api.moeapk.com/client/apiversion";
	
	//获取更新版本下载地址
	public String updateDownloadUrl = "http://api.moeapk.com/client/update";
	
	//注册客户端地址
	public String registerClientUrl = "http://api.moeapk.com/client/action/register/";
	
	//得到的新版本下载地址
	public String downloadUpdateUrl = "";
	
	//初始化服务器提供的版本号
	int serverVersionCode = 0;
	
	//初始化本地应用版本号
	int localVersionCode = 0;
	
	//特殊状态用枚举
	int myStatus = 0;
	
	//网络状态
	int myInternet = 1;

	//初始化本地应用和系统信息变量
	LocalInfo localinfo;
	
	//初始化全局处理器
	Handler myHandler;
	int statusHandler = 0;
	
	//上下关联
	Context myContext;
	
	//应用分类
	public String[] App_Type = {"全部","游戏","小部件","动态壁纸","时钟闹钟","展示漂浮","信息资源","播放模拟","摄像AR","学习参考","拼图壁纸","GalGame","扩展主题","非ACG类"};
	public String[] App_Type_Id = {"0","1","2","3","4","5","6","7","8","9","10","11","12","999"};
	
	//构造函数
	protected MoeApkServer(Context context){
		//加载本地应用和系统信息类
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
	
	//对话框提示部分
	public void ConnectFailedDialog(){
    	Log.e("DEBGU","联网失败");
    	AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
    	builder.setTitle(R.string.dialog_title_internet_connect_failed);
    	builder.setMessage(R.string.dialog_message_internet_connect_failed);
		builder.setNeutralButton(R.string.button_exit,exit_button);
    	builder.create().show();
    }
	
	//无连接对话框
	public void NoConnectionDialog(){
		Log.e("DEBGU","没有网络");
    	AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
    	builder.setTitle(R.string.dialog_title_internet_noconnect);
    	builder.setMessage(R.string.dialog_message_internet_noconnect);
    	builder.setPositiveButton(R.string.button_retry,ButtonRetryCheckConnection);
		builder.setNeutralButton(R.string.button_exit,exit_button);
    	builder.create().show();
	}
	
	//无连接对话框重试按钮
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
	
	//无连接就反复提示
	public boolean CheckConnection(){
		if(!localinfo.isNetworkConnected()){
				myInternet = 0;
				NoConnectionDialog();
				return false;
		}
		return true;
	}
	
	//退出按钮
	private DialogInterface.OnClickListener exit_button = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			ActivityManager.getInstance().exit();
		}
	};
	
	//检查是否有新版本，返回真假
	public boolean checkUpdate(int myVersionCode){
		myStatus = 0; //保证状态
		statusHandler = 0;
		String returnString = null;
		Log.i("NET","开始检测版本更新");
		
		
		//本地信息获取以及缓存
		if(localVersionCode == 0)
			localVersionCode = myVersionCode;
		
		if(serverVersionCode ==0){
					returnString = getStringFromUrl(updateCheckUrl);
					Log.i("DEBUG","服务器返回:" + returnString );
		}
		//如果连接失败，则认为无更新，避免重复弹窗
		if(returnString == "false" || myInternet == 0) return false;
		serverVersionCode = Integer.parseInt(returnString.replaceAll("\r|\n|\r\n", ""));		
		
		Log.i("DEBUG","本地版本号：" + localVersionCode + " 服务器上的版本号：" + serverVersionCode);
		//进行判断
		if(localVersionCode == serverVersionCode)
			return false;
		return true;
	}
	
	public boolean checkAPI(){
		int serverApiVersion;
		String returnString = null;
		Log.i("DEBUG","检查API版本");
		
		returnString = getStringFromUrl(updateCheckApi);
		if(returnString == "false" || myInternet == 0) return false;
		serverApiVersion = Integer.parseInt(returnString.replaceAll("\r|\n|\r\n", ""));
		if(serverApiVersion == ApiVersion) return false;
		return true;
		
	}
	
	//注册客户端
	public void registerClient(String id){
		getStringFromUrl(registerClientUrl + id);
	}
	
	//获取新版本下载地址
	public String getUpdateDownloadUrl(){
		return getStringFromUrl(updateDownloadUrl);
	}

	//从地址获取文本
	public String getStringFromUrl(String url){
		Log.i("NetWork","尝试从服务器读取字符串[" + url + "]" );
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
        Log.i("DEBGU","从服务器获得文本：" + result);
		return result;
	}
	
	//从地址获取Bitmap
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
	
	//应用列表：字符串转JSON再转成Hash数组
	
	public ArrayList<HashMap<String, String>> Hash_AppList;
	public String[] current_applist_package;
	public int current_applist_total_page;
	
	
	public ArrayList<HashMap<String, String>> AppList_Json2HashArray(String json){
		current_applist_package = new String[10];
		
		
		Hash_AppList = new ArrayList<HashMap<String, String>>();
		JSONArray jsonarr = null;
        try {
        	//构建大数组
			jsonarr = new JSONArray(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        for(int i=0;i<jsonarr.length();i++)
        {  
        	JSONObject temp;
			try {
				temp = (JSONObject) jsonarr.get(i);//从数组中取出第i个二级数组
				HashMap<String, String> map = new HashMap<String, String>();  
				current_applist_package[i] = temp.getString("package");//将列表每项package存入一个数组方便列表点击监听的动作
				map.put("Title", temp.getString("appname")); 
				map.put("Text", temp.getString("type") + " " + temp.getString("viewtime") + "查看"); 
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
	
	
	
	//得到列表页数
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
		Log.i("DEBUG","分类:" + type_code + "共有页数" + current_applist_total_page);
	}
	
	
	//APK列表：字符串转JSON再转成Hash数组
	
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
        	//构建大数组
			jsonarr = new JSONArray(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for(int i=0;i<jsonarr.length();i++)
        {  
        	JSONObject temp;
			try {
				temp = (JSONObject) jsonarr.get(i);//从数组中取出第i个二级数组
				HashMap<String, String> map = new HashMap<String, String>();  
				current_apklist_vcode[i] = temp.getString("versioncode");
				current_apklist_special[i] = temp.getString("special");
				current_apklist_appname[i] = temp.getString("appname");
				current_apklist_vname[i] = temp.getString("versionname");
				current_apklist_sname[i] = temp.getString("special_name");
				map.put("Title", temp.getString("appname")); 
				map.put("Text", "版本:" + temp.getString("versionname") + " " + temp.getString("special_name") + " " + temp.getString("size")); 
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
				temp = (JSONObject) jsonarr.get(i);//从数组中取出第i个二级数组
				HashMap<String, String> map = new HashMap<String, String>();  
				map.put("Title", App_Type[i]); 
				//map.put("Text", "版本:" + temp.getString("versionname") + " " + temp.getString("special_name") + " " + temp.getString("size")); 
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
	
	//返回应用信息
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
	
	
	
	//解析网络数据流
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
