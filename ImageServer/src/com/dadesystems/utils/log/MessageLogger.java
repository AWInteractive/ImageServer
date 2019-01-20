package com.dadesystems.utils.log;

import java.io.PrintStream;

public class MessageLogger {
	
	public static final String GLOBAL_LOGGER_NAME = "globlog";
	
	private PrintStream err;
	private PrintStream out;
	
	
	public MessageLogger(){
		super();
	}
	
	public void setLogging(){
		
		System.err.println("ImageApp logging enabled");
		
		// Set-Up Log File
		
	}

}
