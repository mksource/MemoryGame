package com.mksource.utils;

import java.util.Random;


/*
 * This class will randomize the given input array
 */
public class Randomize {
	
	//Shuffle Array function 
	public void ShuffleArray(String[] urls){
		
		int index;
		String url;
		Random random = new Random();
	    for (int i = urls.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	       
	        url = urls[index];
	        urls[index] = urls[i];
	        urls[i] = url;
	    }
		
	}

}
