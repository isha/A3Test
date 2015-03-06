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
	public static int numFailTests = 0;
	public static int numTests = 0;
	
	public static Tests test;
	
    public static void main( String[] args ) throws Exception
    {
    	test = new Tests("file/hosts.txt");

    	assertThat("Normal put", SUCCESS, NO_VALUE, test.put("yolo-batman", "yolo-batman-value"));
    	assertThat("Normal get", SUCCESS, "yolo-batman-value", test.get("yolo-batman"));
    	assertThat("Normal remove", SUCCESS, NO_VALUE, test.remove("yolo-batman"));
    	
    	assertThat("Unrecognized key for remove", NON_EXISTENT_KEY, NO_VALUE, test.remove("yolo-batman"));
    	assertThat("Unrecognized key for get", NON_EXISTENT_KEY, NO_VALUE, test.get("fake-key"));
    	assertThat("Unrecognized command", UNRECOGNIZED_COMMAND, NO_VALUE, test.send((byte) 0x56, "fake-key", ""));
    	
    	assertThat("Put with incorrect short value length", INTERNAL_FAILURE, NO_VALUE, test.put("key-1", 2, "key-1-value"));
    	//assertThat("Out of space", OUT_OF_SPACE, NO_VALUE, test.testOutOfSpaceTooManyKeys());
    	
    	System.out.println("\n============================\n"+numFailTests+" failed tests out of "+numTests+" tests performed");
    }
    
    public static int assertThat(String testName, int respCode, String value, byte[] respBytes) {
    	numTests++;
    	System.out.println("\nTest "+testName+"\n-----------------------------------");
    	if (respBytes == null) {
    		System.out.println("NO RESPONSE");
    		numFailTests++;
    		return 1;
    	}
    	int rc = test.getResponseCode(respBytes);
    	String v = test.getValue(respBytes);
    	
    	if (respCode == rc && v.equals(value)) { 
    		//TODO : value and value-length could be null, according to document of assignment 3, value length and value are optional 
    		System.out.println("OK expected response code: "+respCode+" got: "+rc+", expected value: "+value+" got: "+v);
    		return 0;
    	} else {
    		System.out.println("FAIL expected response code: "+respCode+" got: "+rc+", expected value: "+value+" got: "+v);
    		numFailTests++;
    		return 1;
    	}
    }
}
