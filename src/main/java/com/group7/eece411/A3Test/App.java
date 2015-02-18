package com.group7.eece411.A3Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Vector;



public class App 
{	
	public static final int SUCCESS = 0x00;
	public static final int NON_EXISTENT_KEY = 0x01;
	public static final int OUT_OF_SPACE = 0x02;
	public static final int SYSTEM_OVERLOAD = 0x03;
	public static final int INTERNAL_FAILURE = 0x04;
	public static final int UNRECOGNIZED_COMMAND = 0x05;
	
	public static final String NO_VALUE = "";
	
	public static Tests test;
	
    public static void main( String[] args ) throws Exception
    {
    	test = new Tests("file/hosts.txt");

    	assertThat(SUCCESS, NO_VALUE, test.put("yolo-batman", "yolo-batman-value"));
    	assertThat(SUCCESS, "yolo-batman-value", test.get("yolo-batman"));
    	assertThat(SUCCESS, NO_VALUE, test.remove("yolo-batman"));
    }
    
    public static void assertThat(int respCode, String value, byte[] respBytes) {
    	int rc = test.getResponseCode(respBytes);
    	String v = test.getValue(respBytes);
    	
    	if (respCode == rc && v.equals(value)) {
    		System.out.println("OK expected response code: "+respCode+" got: "+rc+", expected value: "+value+" got: "+v);
    	} else {
    		System.out.println("FAIL expected response code: "+respCode+" got: "+rc+", expected value: "+value+" got: "+v);
    	}
    }
}
