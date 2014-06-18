package com.moeapk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class AppInfoScreenshotAdapter extends BaseAdapter  {
	private MoeApkServer moeapk;
	private Context context;
	private ArrayList<HashMap<String,String>> data;
	public ImageLoader imageLoader;
	
	public AppInfoScreenshotAdapter(Context context,ArrayList<HashMap<String, String>> d){
		this.context = context;
		this.data = d;
		moeapk = new MoeApkServer(context);
		imageLoader=new ImageLoader(context.getApplicationContext());
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
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView imageview = new ImageView(this.context);
		//设置显示比例
		imageview.setLayoutParams(new Gallery.LayoutParams(300, 300));
		//设置显示方式
		imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
		
		HashMap<String,String> temp = new HashMap<String, String>();
		temp = data.get(position);
		imageLoader.setCacheAsFile(true);
		imageLoader.setSaveFileName("截图_" + temp.get("hash") + "_缩略图.jpg");
		imageLoader.DisplayImage(moeapk.AppScreenshotThumbUrl(temp.get("hash")), imageview);
		
		return imageview;
	}

}
