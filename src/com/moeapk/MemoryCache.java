package com.moeapk;
import java.lang.ref.SoftReference;  
import java.util.Collections;  
import java.util.HashMap;  
import java.util.Map;  
import android.graphics.Bitmap;  
import android.util.Log;
  
public class MemoryCache {  
    private Map<String, SoftReference<Bitmap>> cache=Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());//»Ì“˝”√  
      
    public Bitmap get(String id){  
        if(!cache.containsKey(id))  
            return null;  
        SoftReference<Bitmap> ref=cache.get(id);  
        Log.i("MC","GET MemoryCache for id:" +id);
        return ref.get();  
    }  
      
    public void put(String id, Bitmap bitmap){  
    	Log.i("MC","PUT MemoryCache for id:" +id);
        cache.put(id, new SoftReference<Bitmap>(bitmap));  
    }  
  
    public void clear() {  
        cache.clear();  
    }  
}  