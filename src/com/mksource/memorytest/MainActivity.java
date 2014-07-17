package com.mksource.memorytest;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;


import com.mksource.utils.FlickService;
import com.mksource.utils.Randomize;
import com.squareup.picasso.Picasso;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


public class MainActivity extends Activity implements AnimationListener,OnItemClickListener{

	private GridView grid;
	private ImageAdapter adapter;
	private ImageView imageSwitcher;
	private static String TAG="MainActivity";
	private TextView text;
	
	//Animations to flip the images in gridview
	private Animation animation1;
	private Animation animation2;
	
	//variable to hold currently selected image
	private ImageView selectedImage;
	
	//Initially Selected index is -1 since no image is selected
	private int selectedIndex=-1;
	
	//urls for the images downloaded
	private String images[];
	
	//Class to Shuffle the array
	private Randomize random;
	
	//To indicate activity indicator
	private ProgressDialog progressDialog;
	
	//variable which tells a current turn of game is running
	private boolean isGameStarted=false;
	
	//variable which tells whether the previous turn has selected correctly
	private boolean isPreviousCorrect=false;
	
	//variable which tells the array is shuffled or for new game
	private boolean isArrayShuffled=true;
	
	//Hit array which tells whether the particular 
	private boolean[] foundArray=new boolean[9];
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.d(TAG,"OnCreated");
		
		//Get the ID for the gridview
		grid=(GridView) findViewById(R.id.gridView1);
		
		//Get the ID for the imageSwitcher
		imageSwitcher=(ImageView)findViewById(R.id.imageSwitcher1);
		
		//Get the TextView 
		text=(TextView)findViewById(R.id.textview);
		text.setTextColor(Color.BLUE);
		
		//Set a Custom Adapter for the gridview which loads the imageview
		adapter=new ImageAdapter(this,true,this);
		grid.setAdapter(adapter);
		
		//Load the Flip Animation
		animation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
		animation1.setAnimationListener(this);
		animation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
		animation2.setAnimationListener(this);
		
		//Set the OnItemclickListener for the gridview
		grid.setOnItemClickListener(this);
		
		//Initially make the gridview invisible
		grid.setVisibility(View.INVISIBLE);
			
		//Set the randomize class
		random=new Randomize();
		
		//If Network Connection is available then load the images
		//Otherwise show a warning
		if(haveNetworkConnection()){
			//Download the images in the background
			
			CharSequence text = "Loading Images for game";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
			new DownloadImageTask().execute();
			
			progressDialog = ProgressDialog.show(this, null, null, true, false);
			progressDialog.setContentView(R.layout.progress_layout);
		}
		else{
			CharSequence text = "Please make sure you are connected to network";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case R.id.action_refresh:
				//Load a new game
				
				if(isGameStarted){
					CharSequence timetext = "Complete the Current game";
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(this,timetext, duration);
					toast.show();
					return true;
				}	
				
				//Check if the array is already shuffled 
				if(!isArrayShuffled){
					adapter.setBlank(false);
					random.ShuffleArray(images);
					adapter.setUrls(images);
					adapter.notifyDataSetChanged();
				}
				CharSequence timetext = "You have 15 seconds to remember the images";
				int duration = Toast.LENGTH_LONG;
				Toast toast = Toast.makeText(this,timetext, duration);
				toast.show();
				
				//Start the Game
				isGameStarted=true;
				
				
				
				//Clear the SelectedIndex
				selectedIndex=-1;
				
				//initialize the index found array
				for(int i=0;i<foundArray.length;i++)
					foundArray[i]=false;
				
				text.setText("seconds remaining: 15");
				text.setTextColor(Color.BLUE);
				
				new CountDownTimer(15000, 1000) {

				     public void onTick(long millisUntilFinished) {
				         text.setText("seconds remaining: " + millisUntilFinished / 1000);
				     }

				     public void onFinish() {
				    	 
				    	//Is the Previous turn 
						isPreviousCorrect=true;
				    	 
				    	 //Add Blank Images
				         adapter.setBlank(true);
				         
				         //Remove the counter from text
				         text.setText(R.string.Next);
				        
				         //Reload the GridView with blank images
				         adapter.notifyDataSetChanged();
				     }
				  }.start();
				
				return true;
						
			case R.id.action_forward:
				
				//Generate a random number within the range 0 to 9 
				//and check whether the image is already found
				if(selectedIndex!=-1 && !foundArray[selectedIndex])
					return true;
				
				//Generate next random image
				if(isGameStarted && isPreviousCorrect){
					Random random=new Random();
					int next=random.nextInt(9);
					while(foundArray[next]){
						next=random.nextInt(9);
					}	
					selectedIndex=next;
					Picasso.with(this).load(images[next]).into(imageSwitcher);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	//Inner Class which downloads the metadata for images on flickr cloud  in json format
	//Then it builds the url for the images and cache it
	private class DownloadImageTask extends AsyncTask<Void,Integer,String[]>{

		@Override
		protected String[] doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			Log.d(TAG,"Starting Flickr Download Service");
			//Download the images from flickr
			FlickService flickr=new FlickService();
			
			//List of Urls For the images
			String urls[] = null;
			
			
			try{
				String photolist=flickr.getPhotosList();
				if(photolist!=null){
					
					Log.d(TAG,"Successfully downloaded Images from flickr");
					//Parse the response to get the indiviual urls for the 
					JSONObject response=new JSONObject(photolist);
					String state=response.getString("stat");
					
					
					if(state.equalsIgnoreCase("ok")){
						
						//Get the Array of photos
						
						JSONObject photosObj=response.getJSONObject("photos");
						JSONArray photos=photosObj.getJSONArray("photo");
						Log.d(TAG,photos.length()+"");
						if(photos.length()!=0){
							
							//build a urls object 
							urls=new String[photos.length()];
							
							//Build the Url for photo object 
							for(int i=0;i<photos.length();i++){
								
								JSONObject photo=(JSONObject) photos.get(i);
								String secert=photo.getString("secret");
								String farm=photo.getString("farm");
								String server=photo.getString("server");
								String id=photo.getString("id");
								String url="https://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secert+".jpg";
								Log.d(TAG,url);
								urls[i]=url;
									
							}
							
							
						}
						
					}
					
					
				}
			}
			catch(Exception e){
				
			}
			return urls;
		}
		 protected void onProgressUpdate(Integer... progress) {
	         
	     }

	     protected void onPostExecute(String[] urls) {
	        
	    	 //If the url is not null reload the adapter
	    	if(urls!=null){
	    		
	    		images=new String[urls.length];
	    		for(int i=0;i<images.length;i++)
	    			images[i]=urls[i];    		
	    		//Shuffle the array
	    		random.ShuffleArray(images);
	    		
	    		adapter.setUrls(images);
	    		
	    		//Show the images
	    		adapter.setBlank(false);
	    		
	    		//Reload the GridView
	    		adapter.notifyDataSetChanged();
	    		
	    		//Make the GridView Visible and loading asynchronously
	    		grid.setVisibility(View.VISIBLE);
	    		
	    		
	    	}
	    	else{
	    		//Not able to download the images show an error
	    		CharSequence text = "Please make sure you are connected to network";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(MainActivity.this, text, duration);
				toast.show();
	    		
	    	}
	    	
	     }
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		if(animation==animation1){
			Picasso.with(this).load(images[selectedIndex]).into(selectedImage);
			selectedImage.clearAnimation();
			selectedImage.startAnimation(animation2);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// TODO Auto-generated method stub
		selectedImage=(ImageView)v;
		
		//Check if no image is selected 
		if(selectedIndex==-1)
			return;
		
		//Check if the position has been already selected
		if(foundArray[position] || foundArray[selectedIndex])
			return;
		
		
		
		if(position==selectedIndex){
			
			text.setTextColor(Color.GREEN);
			text.setText(R.string.Correct);
			
			
			foundArray[position]=true;
			
			//Flip the correct image
			selectedImage.clearAnimation();
			selectedImage.startAnimation(animation1);
			
			//The current turn is over
			isPreviousCorrect=true;
			
			CharSequence ntext = "Select next image";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, ntext, duration);
			toast.show();
			
			boolean hasGamefinished=true;
			//Check if all the images are selected
			for(int i=0;i<foundArray.length;i++){
				if(!foundArray[i])
					hasGamefinished=false;
				
			}
			//If game has finished
			if(hasGamefinished){
				isGameStarted=false;
				
				text.setText(R.string.Won);
				
				//Shuffle the array
				isArrayShuffled=false;
				
				imageSwitcher.setImageDrawable(null);
				
			}
			
			
		}
		else{
			
			text.setTextColor(Color.RED);
			
			text.setText(R.string.Wrong);
			
			//Current Turn not Over 
			isPreviousCorrect=false;
		}
	
	}
	
	public void stopProgressDialog(){
		
		//Stop the Progress Dialo
		progressDialog.cancel();
		
		
		
	}
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}
	
	

}
