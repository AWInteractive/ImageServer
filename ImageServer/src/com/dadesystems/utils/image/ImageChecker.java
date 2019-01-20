package com.dadesystems.utils.image;

import java.io.File;

public class ImageChecker {
	
	private final String[] okFileExtensions = new String[] {"jpg", "jpeg", "png", "gif"};
	
	public ImageChecker(){
		super();
	}
	
	public boolean isGoodFile(File file){
	    for (String extension : okFileExtensions)
	    {
	      if (file.getName().toLowerCase().endsWith(extension))
	      {
	        return true;
	      }
	    }
	    return false;
	  }

}
