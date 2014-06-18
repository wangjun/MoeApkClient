package com.moeapk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class FileUtils {

	public String SDCARD = Environment.getExternalStorageDirectory() + "/";
	public String MyFolder = SDCARD + "MoeApk/";
	public String CACHE = MyFolder + "Cache/";
	
	private String sd_status = Environment.getExternalStorageState();
	
	public FileUtils(){
		createDir("MoeApk");
		createDir("Cache");
		createDir("Cache/.nomedia");//防止被媒体库收录
		CheckSdcardStatus();
	}
	
	private boolean CheckSdcardStatus(){
		if(!sd_status.equals(Environment.MEDIA_MOUNTED))return false;
		if(!checkFileExists("MoeApk")) return false;
		return true;
	}
	
	private File createFile(String file) throws IOException {  
		File tmp = new File(SDCARD + file);  
        if(!tmp.exists())tmp.createNewFile();  
        return tmp;  
    }  
	
	public File createDir(String path){
		File dir = new File(SDCARD + path);
		if(dir.exists()) return dir;
		dir.mkdir();
		return dir;
	}
	
	public boolean checkFileExists(String path) {
        File file=new File(SDCARD + path);
        return file.exists();
    }
	
	public void clearCache(){
		try {
			deleteFolderFile(MyFolder + "Cache",false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createDir("Cache");
		
	}
	
	public File writeInputStream(String path,String filename,InputStream input){
		File file = null;
		OutputStream output = null;
		try{
			createDir(path);
			file = createFile(path + "/" + filename);
			output = new FileOutputStream(file);
			byte buffer [] = new byte[4 * 1024];  
			//while((input.read(buffer)) != -1){  
                //output.write(buffer);  
            //}  
			int len=0;
			while((len = input.read(buffer)) != -1){
				output.write(buffer, 0, len);
			}
            output.flush(); 
            output.close();
		}catch(Exception e){  
            e.printStackTrace();  
        }  
        finally{  
            try{  
                //output.close();  
            }  
            catch(Exception e){  
                e.printStackTrace();  
            }  
        }  
        return file;
	}
	
	//这里抄来的http://www.2cto.com/kf/201302/192000.html
	public void deleteFolderFile(String filePath, boolean deleteThisPath)  throws IOException {  
        if (!TextUtils.isEmpty(filePath)) {  
            File file = new File(filePath);  
  
            if (file.isDirectory()) {// 处理目录  
                File files[] = file.listFiles();  
                for (int i = 0; i < files.length; i++) {  
                    deleteFolderFile(files[i].getAbsolutePath(), true);  
                } 
            }  
            if (deleteThisPath) {  
                if (!file.isDirectory()) {// 如果是文件，删除  
                    file.delete();  
                } else {// 目录  
                    if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除  
                        file.delete();  
                    }  
                }  
            }  
        }  
    }  
	
	
}
