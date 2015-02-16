package com.group7.eece411.A3Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	byte[] response;
    	
    	System.out.println("Testing PUT yolo-batman:yolo-batman-value");
    	response = send((byte) 0x01, "yolo-batman", "yolo-batman-value");
    	System.out.println("Expected RESP 0x00, Got "+response[0]);
    	
    	System.out.println("\n\nTesting GET yolo-batman");
    	response = send((byte) 0x02, "yolo-batman", "");
    	System.out.println("Expected RESP 0x00, Got "+response[0]);
    	System.out.println("Expected VAL yolo-batman-value, Got "+getValue(response));
    }
    
	private static byte[] send(byte command, String key, String value) throws Exception {
		// Prep key bytes
		byte[] keyBytes = new byte[32];
		keyBytes = key.getBytes("UTF-8");
		
		// Prep value bytes
		byte[] valueBytes = value.getBytes("UTF-8");
		
		// Form message with correct sizes for stuff
		ByteBuffer buffer = ByteBuffer.allocate(1+32+2+valueBytes.length).order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(command).put(keyBytes).putShort((short) valueBytes.length).put(valueBytes);
		
		// Send message
		UDPClient client = new UDPClient(4567);
		client.send("127.0.0.1", String.valueOf(7777), new String(buffer.array(), Charset.forName("UTF-8")) );
		
		// Receive message
		client.setTimeout(2000);
		byte[] rcvMsg = client.receive();
		
		byte[] actualMsg = new Header().decodeAndGetMessage(rcvMsg);
		return actualMsg;
	}

	private static String getValue(byte[] msg) {
		ByteBuffer bf = ByteBuffer.wrap(msg).order(ByteOrder.LITTLE_ENDIAN);
		byte responseCode = bf.get();
		short valueSize = bf.getShort();
		byte[] rcvValue = new byte[valueSize];
		bf.get(rcvValue, 3, valueSize);
		String value = new String(rcvValue, Charset.forName("UTF-8"));
		
		return value;
	}
}
