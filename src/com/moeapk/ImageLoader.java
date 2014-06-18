package com.moeapk;
import java.io.File;   
import java.io.FileInputStream;   
import java.io.FileNotFoundException;   
import java.io.FileOutputStream;   
import java.io.IOException;
import java.io.InputStream;   
import java.io.OutputStream;   
import java.net.HttpURLConnection;   
import java.net.URL;   
import java.util.Collections;   
import java.util.Map;   
import java.util.WeakHashMap;   
import java.util.concurrent.ExecutorService;   
import java.util.concurrent.Executors;    
import android.app.Activity;   
import android.content.Context;   
import android.graphics.Bitmap;   
import android.graphics.BitmapFactory;   
import android.util.Log;
import android.widget.ImageView;   
    
public class ImageLoader {   
    
    MemoryCache memoryCache=new MemoryCache();   
    FileCache fileCache;   
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());   
    ExecutorService executorService;    
    
    private boolean SAVE_AS_CACHE=true;
    private String SAVE_PATH;
    private String SAVE_NAME;
    private boolean READ_FROM_FILE = true;
    private boolean READ_FROM_MEMORY = true;
    
    public ImageLoader(Context context){   
        fileCache=new FileCache(context);   
        executorService=Executors.newFixedThreadPool(5);   
    }   
    
    final int stub_id = R.drawable.ic_launcher;   
    public void DisplayImage(String url, ImageView imageView)   
    {   
        imageViews.put(imageView, url);
        Bitmap bitmap=null;
        if(READ_FROM_MEMORY)bitmap = memoryCache.get(url);   
        if(bitmap!=null)   
            imageView.setImageBitmap(bitmap);   
        else  
        {   
            queuePhoto(url, imageView);   
            imageView.setImageResource(stub_id);   
        }   
    }   
    
    public void setCacheAsFile(boolean value){
    	SAVE_AS_CACHE = value;
    }
    
    public void setSaveFilePath(String value){
    	SAVE_PATH = value;
    }
    
    public void setSaveFileName(String value){
    	SAVE_NAME = value;
    }
    
    public void setReadFromFile(boolean value){
    	READ_FROM_FILE = value;
    }
    
    public void setCacheToMemory(boolean value){
    	READ_FROM_MEMORY = value;
    }
    
    private void queuePhoto(String url, ImageView imageView)   
    {   
    	if(SAVE_AS_CACHE) setSaveFilePath("MoeApk/Cache");
        PhotoToLoad p=new PhotoToLoad(url, imageView , SAVE_PATH ,SAVE_NAME);   
        executorService.submit(new PhotosLoader(p));   
    }   
    
    private Bitmap getBitmap(PhotoToLoad p)   
    {   
    	String url=p.url;
    	Bitmap b; 

 
        if(READ_FROM_FILE){
            b = BitmapFactory.decodeStream(p.readFile());
            if(b!=null){
            	
            	Log.i("ImageLoader","从缓存中读取:" + p.truePath);
            	return b;
            }
        }
        Log.i("ImageLoader","新下载文件");
    
        //从网络  
        Bitmap bitmap = null;
    	URL myurl;
        try{
        	myurl = new URL(url);
        	HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
        	conn.setConnectTimeout(6000);
        	conn.setDoInput(true);
        	conn.setUseCaches(true);
        	conn.connect();
        	//缓存文件
        	InputStream is = conn.getInputStream();
        	p.setInputStream(is);//设置流
        	p.SaveFile();
        	bitmap = BitmapFactory.decodeStream(p.readFile());
        	is.close();
        }catch (Exception e){
        	e.printStackTrace();
        }
        return bitmap;
    }   
    
    //解码图像用来减少内存消耗  
    private Bitmap decodeFile(File f){   
        try {   
            //解码图像大小  
            BitmapFactory.Options o = new BitmapFactory.Options();   
            o.inJustDecodeBounds = true;   
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);   
    
            //找到正确的刻度值，它应该是2的幂。  
            final int REQUIRED_SIZE=70;   
            int width_tmp=o.outWidth, height_tmp=o.outHeight;   
            int scale=1;   
            while(true){   
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)   
                    break;   
                width_tmp/=2;   
                height_tmp/=2;   
                scale*=2;   
            }   
    
            BitmapFactory.Options o2 = new BitmapFactory.Options();   
            o2.inSampleSize=scale;   
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);   
        } catch (FileNotFoundException e) {}   
        return null;   
    }   
    
    //任务队列  
    private class PhotoToLoad   
    {   
        public String url;   
        public ImageView imageView;   
        public InputStream inputStream;
        public String filePath;
        public String fileName;
        public String truePath;
        private FileUtils file;
        
        public PhotoToLoad(String u, ImageView i , String path, String name){   
            url=u;   
            imageView=i;   
            
            filePath = path;
            fileName = name;
            
            file = new FileUtils();
            
            truePath = file.SDCARD + path + "/" + name;
        }   
        
        public void setInputStream(InputStream is){
        	inputStream = is;
        }
        
        public boolean SaveFile(){
        	if(file.writeInputStream(filePath, fileName, inputStream)== null){
        		return false;
        	}
        	return true;
        }
        
        public InputStream readFile(){
        	try {
				return new FileInputStream(truePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return null;
        }
        
        public InputStream readFile(String otherPath){
        	try {
				return new FileInputStream(otherPath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return null;
        }
    }   
    
    class PhotosLoader implements Runnable {   
        PhotoToLoad photoToLoad;   
        PhotosLoader(PhotoToLoad photoToLoad){   
            this.photoToLoad=photoToLoad;   
        }   
    
        @Override  
        public void run() {   
            if(imageViewReused(photoToLoad))   
                return;   
            Bitmap bmp=getBitmap(photoToLoad);   
            memoryCache.put(photoToLoad.url, bmp);   
            if(imageViewReused(photoToLoad))   
                return;   
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);   
            Activity a=(Activity)photoToLoad.imageView.getContext();   
            a.runOnUiThread(bd);   
        }   
    }   
    
    boolean imageViewReused(PhotoToLoad photoToLoad){   
        String tag=imageViews.get(photoToLoad.imageView);   
        if(tag==null || !tag.equals(photoToLoad.url))   
            return true;   
        return false;   
    }   
    
    //用于显示位图在UI线程  
    class BitmapDisplayer implements Runnable   
    {   
        Bitmap bitmap;   
        PhotoToLoad photoToLoad;   
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}   
        public void run()   
        {   
            if(imageViewReused(photoToLoad))   
                return;   
            if(bitmap!=null)   
                photoToLoad.imageView.setImageBitmap(bitmap);   
            else  
                photoToLoad.imageView.setImageResource(stub_id);   
        }   
    }   
    
    public void clearCache() {   
        memoryCache.clear();   
        fileCache.clear();   
    }   
    
}   