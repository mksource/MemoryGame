package com.mksource.memorytest;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter{
	
	private Context mContext;
	
	//Will tell whether a blank image should be loaded or it should be fetched from flickr
	private boolean isBlank;
	
	//Counter to count the number of images downloaded
	private int count=0;
	
	//Handle for the activity
	private MainActivity activity;
	
	//List of urls
	private String urls[];
	
	//IsBlank Loads a Blank Image into gridview initially
	public ImageAdapter(Context context,boolean isBlank,MainActivity activity){
		this.mContext=context;
		this.isBlank=isBlank;
		this.activity=activity;
	}

	public void setUrls(String[] urls){
		this.urls=urls;
	}
	public void setBlank(boolean isBlank){
		this.isBlank=isBlank;
	}
	public boolean isBlank(){
		return this.isBlank;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 9;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public synchronized void increment() {
        count++;
    }
	public synchronized int value(){
		return count;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
				
		ImageView imageView=null;
		if(convertView==null){
			imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(
            (int)mContext.getResources().getDimension(R.dimen.grid_width),
            (int)mContext.getResources().getDimension(R.dimen.grid_hieght)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(16, 16, 16, 16);
            
		}
		else
			imageView=(ImageView) convertView;
		
		
		if(isBlank)
			imageView.setImageResource(R.drawable.blank);
		else{
			Picasso.with(mContext).load(urls[position]).into(imageView,new Callback(){

				@Override
				public void onError() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					
					//Increemnet the counter
					increment();
					
					if(value()==9){
						count=0;
						activity.stopProgressDialog();
						
					}
					
				}
				
			});
		}
		
		return imageView;
	}

}
