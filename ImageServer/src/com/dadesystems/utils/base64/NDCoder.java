package com.dadesystems.utils.base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

public class NDCoder {

	public NDCoder(){
		super();
	}
	
	private byte encodeData[];
	private byte[] decodeArray;
	
	public String ncode(String imagePath) {
		
		String encodedString = "";
		
		File file = new File(imagePath);
		
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			
			encodeData = new byte[(int) file.length()];
			imageInFile.read(encodeData);
			encodedString = "SUCCESS:" + Base64.getEncoder().encodeToString(encodeData);
			
		} catch (FileNotFoundException e) {
			encodedString = "ERROR:Image not found" + e.getMessage();
		} catch (IOException ioe) {
			encodedString = "ERROR:Exception while reading the Image " + ioe.getMessage();
		}
		
		return encodedString;
	}
 
	public String dcode(String encodedImage) {
		
		String decodedString = "";
		
		try {
			
			decodeArray = Base64.getDecoder().decode(encodedImage);
			decodedString = "SUCCESS";
			
		} catch (Exception ioe) {
			decodedString = "ERROR:Exception while reading the Image " + ioe.getMessage();
		}
		
		return decodedString;
	}

	public byte[] getDecodeArray() {
		return decodeArray;
	}

	public void setDecodeArray(byte[] decodeArray) {
		this.decodeArray = decodeArray;
	}
	
	public byte[] getEncodeData() {
		return encodeData;
	}

	public void setEncodeData(byte[] encodeData) {
		this.encodeData = encodeData;
	}	
}
