package com.mksource.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class FlickService {
	
	private static String userName="username=mknarayan1711";
	private static String baseUrl="https://api.flickr.com/services/rest/?";
	private static String apiKey="api_key=4bc2dc7a417c3da13264ebf4146486c6";
	private static String format="format=json";
	private static String TAG="FlickrService";
	
	//Get the NSID for the username
	private String getNSID() throws URISyntaxException, ClientProtocolException, IOException, JSONException{
		
		//Response
		String nsid=null;
		
		String method="method=flickr.people.findByUsername";
		
		//Build the URL for the getNSID for the user
		String url=baseUrl+method+"&"+apiKey+"&"+userName+"&"+format;
		
		Log.d(TAG,"The url for fetching NSID is "+url);
		//Get the NSID for this particular user
		HttpClient client=new DefaultHttpClient();
		HttpGet request=new HttpGet();
		request.setURI(new URI(url));
		HttpResponse response=client.execute(request);
		StatusLine statusLine = response.getStatusLine();
	    int statusCode = statusLine.getStatusCode();
	    
	    //Parse the response if the status is successfull
	    if(statusCode==200){
	    	StringBuilder builder=new StringBuilder();
	    	HttpEntity entity = response.getEntity();
	        InputStream content = entity.getContent();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	        String line;
	        while ((line = reader.readLine()) != null) {
	          builder.append(line);
	        }
	        
	        Log.d(TAG,"The response from NSID is "+extractJSONString(builder.toString()));
	        
	        JSONObject fres=new JSONObject(extractJSONString(builder.toString()));
	        JSONObject user=(JSONObject) fres.get("user");
	        nsid=user.getString("nsid");
	        Log.d(TAG,nsid);
	        
	    }
	    
	    return nsid;
	}
	
	//Get the photos 
	private String downloadPhotos(String nsid) throws URISyntaxException, ClientProtocolException, IOException{
		
		
		String photolist=null;
		
		String method="method=flickr.people.getPublicPhotos";
		
		String userid="user_id="+nsid;
		
		//Build the URL for getPhoto Service
		String url=baseUrl+method+"&"+apiKey+"&"+userid+"&"+format;
		
		Log.d(TAG,"The url for fetching Public Photos is "+url);
		
		//Get the NSID for this particular user
		HttpClient client=new DefaultHttpClient();
		HttpGet request=new HttpGet();
		request.setURI(new URI(url));
		HttpResponse response=client.execute(request);
		StatusLine statusLine = response.getStatusLine();
	    int statusCode = statusLine.getStatusCode();
	    
	    //Parse the response if the status is successfull
	    if(statusCode==200){
	    	StringBuilder builder=new StringBuilder();
	    	HttpEntity entity = response.getEntity();
	        InputStream content = entity.getContent();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	        String line;
	        while ((line = reader.readLine()) != null) {
	          builder.append(line);
	        }
	        Log.d(TAG,"The response from getPublicPhotos is "+builder.toString());
	        photolist=extractJSONString(builder.toString());
	    }
		
	    return photolist;
	}
	
	private String extractJSONString(String input){
		String response=input;
		response = response.replace("jsonFlickrApi(", "");
        response = response.substring(0,response.length()-1);
        return response;
	}
		
	public String getPhotosList() throws ClientProtocolException, URISyntaxException, IOException, JSONException{
		
		String photoslist=null;
		
		//Get the User ID for the given user name
		String nsid=getNSID();
		
		if(nsid!=null){
			photoslist=downloadPhotos(nsid);
		}
		return photoslist;
	}

}
