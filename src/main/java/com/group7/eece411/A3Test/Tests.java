package com.group7.eece411.A3Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Vector;

public class Tests {
	public class NodeInfo {
		public NodeInfo(String hn, String p) {
			hostName = hn;
			port = p;
		}
		public String hostName;
		public String port;
	}
	
	Vector<NodeInfo> nodes = new Vector<NodeInfo>();
	
	public byte[] put(String k, String v) throws Exception {
		byte[] response;
		System.out.println("\nPUT key: "+k+", value: "+v);
    	response = send((byte) 0x01, k, v);
    	return response;
	}
	
	public byte[] get(String k) throws Exception {
		byte[] response;
		System.out.println("\nGET key: "+k);
    	response = send((byte) 0x02, k, "");
    	return response;
	}
	
	public byte[] remove(String k) throws Exception {
		byte[] response;
		System.out.println("\nREMOVE key: "+k);
    	response = send((byte) 0x03, k, "");
    	return response;
	}
	
	public Tests(String string) throws IOException {
		InputStream in = Tests.class.getClassLoader().getResourceAsStream(string);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] lineArray = line.split(":");
			if (lineArray.length != 2) {
				System.out.println("Invalid Line Found in hosts.txt");
			} else {
				NodeInfo n = new NodeInfo(lineArray[0], lineArray[1]);
				nodes.add(n);
				System.out.println("Identified participating node "+n.hostName+" on port "+n.port);
			}
		}
	}
	
	private byte[] send(byte command, String key, String value) throws Exception {
		UDPClient client = new UDPClient(4567);
		try {
			// Prep key bytes
			byte[] keyBytes = new byte[32];
			byte[] kb = key.getBytes("UTF-8");
			
			for (int i=0; i<kb.length && i<32; i++) {
				keyBytes[i] = kb[i];
			}
			
			//System.out.println("Key bytes: "+keyBytes.length);
			
			// Prep value bytes
			byte[] valueBytes = value.getBytes("UTF-8");
			//System.out.println("Value bytes: "+valueBytes.length);
			
			// Form message with correct sizes for stuff
			ByteBuffer buffer = ByteBuffer.allocate(1+32+2+valueBytes.length).order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(command).put(keyBytes).putShort((short) valueBytes.length).put(valueBytes);
			
			// Send message
			client.send("127.0.0.1", String.valueOf(7777), new String(buffer.array(), Charset.forName("UTF-8")) );
			
			// Receive message
			client.setTimeout(2000);
			byte[] rcvMsg = client.receive();
			
			byte[] actualMsg = new Header().decodeAndGetMessage(rcvMsg);
			return actualMsg;
		} catch(SocketTimeoutException e) {
			return null;
		} finally {
			client.closeSocket();
		}
	}

	public String getValue(byte[] msg) {
		ByteBuffer bf = ByteBuffer.wrap(msg).order(ByteOrder.LITTLE_ENDIAN);
		byte responseCode = bf.get();
		short valueSize = bf.getShort();
		byte[] rcvValue = new byte[valueSize];
		bf.get(rcvValue, 3, valueSize);
		String value = new String(rcvValue, Charset.forName("UTF-8"));
		
		return value;
	}
	
	public int getResponseCode(byte[] msg) {	
		return msg[0];
	}

}
