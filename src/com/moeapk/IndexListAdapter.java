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

public class IndexListAdapter extends BaseAdapter {
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;  
    private static LayoutInflater inflater=null;  
    public ImageLoader imageLoader;
    
    public IndexListAdapter(Activity a, ArrayList<HashMap<String, String>> d){
    	activity = a;  
        data=d;  
        imageLoader = new ImageLoader(activity.getApplicationContext());
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();  
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi=convertView;  
        if(convertView==null)  
            vi = inflater.inflate(R.layout.listitem_index, null);  
        
        TextView title = (TextView)vi.findViewById(R.id.index_list_item_text);
        ImageView icon = (ImageView)vi.findViewById(R.id.index_list_item_icon);
        
        HashMap<String, String> item = new HashMap<String, String>();  
        item = data.get(position);  
        
        title.setText(item.get("Title"));
        imageLoader.setCacheAsFile(true);
        imageLoader.setSaveFileName("Icon_" + item.get("Package") + "_small.png");
        imageLoader.DisplayImage(item.get("Icon"), icon);  
        return vi;
	}

}
