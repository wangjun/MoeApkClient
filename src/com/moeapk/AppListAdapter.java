package com.moeapk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AppListAdapter extends BaseAdapter{
	private Activity activity;  
    private ArrayList<HashMap<String, String>> data;  
    private static LayoutInflater inflater=null;  
    public ImageLoader imageLoader; //��������ͼƬ���࣬�����н���  
    
    private boolean SHOW_FOR_MAIN = true;
      
    public AppListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {  
        activity = a;  
        data=d;  
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        imageLoader=new ImageLoader(activity.getApplicationContext());  
    }  
  
    public int getCount() {  
        return data.size();  
    }  
  
    public Object getItem(int position) {  
        return position;  
    }  
  
    public long getItemId(int position) {  
        return position;  
    }  
    
    //���ò�����ͼ���״̬
    public void setShowForMain(boolean value){
    	SHOW_FOR_MAIN = value;
    }
      
    public View getView(int position, View convertView, ViewGroup parent) {  
        View vi=convertView;  
        if(convertView==null)  
            vi = inflater.inflate(R.layout.listitem_applist, null);  
  
        TextView title = (TextView)vi.findViewById(R.id.ItemTitle); // ����  
        TextView text = (TextView)vi.findViewById(R.id.ItemText); 
          
        HashMap<String, String> app = new HashMap<String, String>();  
        app = data.get(position);  
          
        // ����ListView�����ֵ  
        title.setText(app.get("Title"));  
        text.setText(app.get("Text"));  
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.image_icon);
        imageLoader.setCacheAsFile(true);
        if(SHOW_FOR_MAIN){
        	imageLoader.setSaveFileName("Icon_" + app.get("Package") + "_small.png");
        }else{
        	imageLoader.setSaveFileName("App_" + app.get("Package") + "_" + app.get("VersionCode") + "_" + app.get("SpecialCode") + "_small.png");
        }
        
        imageLoader.DisplayImage(app.get("Icon"), thumb_image);  
        return vi;  
    }  
}
