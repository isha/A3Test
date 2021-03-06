package com.group7.eece411.A3Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Random;
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
	
	  public Object[] put(String k, String v) throws Exception {
		      return send((byte) 0x01, k, v);
		  }
		  
		  public Object[] put(String k, int v_size, String v) throws Exception {
		      return send((byte) 0x01, k, v, v_size);
		  }
		  
		  public Object[] testOutOfSpaceTooManyKeys() throws Exception {
		    SecureRandom random = new SecureRandom();
		    String key;
		    Object obj[] = null;
		    
		    for(int i = 0; i < 100000; i++) {
		      key = new BigInteger(130, random).toString(32);
		      obj = send((byte) 0x01, key, "1");
		    }
		    return obj;
		  }

		  public Object[] get(String k) throws Exception {
		      return send((byte) 0x02, k, "");
		  }
		  

		public Object[] get(String k, Header header) throws Exception {
			return sendWithHeader((byte) 0x02, k, "", header);
		}
		  
		  public Object[] remove(String k) throws Exception {
		      return send((byte) 0x03, k, "");
		  }
	public Tests(String string) throws IOException {
		InputStream in = Tests.class.getClassLoader().getResourceAsStream(string);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] lineArray = line.split(":");
			if (lineArray.length < 2) {
				System.out.println("Invalid Line Found in hosts.txt");
			} else {
				NodeInfo n = new NodeInfo(lineArray[0], lineArray[1]);
				nodes.add(n);
				System.out.println("Identified participating node "+n.hostName+" on port "+n.port);
			}
		}
	}
	
	public Object[] send(byte command, String key, String value) throws Exception {
		return send(command, key, value, -1);
	}
	
	public Object[] send(byte command, String key, String value, int vSize) throws Exception {
		UDPClient client = new UDPClient(4567);
		Header h;
		try {
			// Prep key bytes
			byte[] keyBytes = new byte[32];
			byte[] kb = key.getBytes("UTF-8");
			
			for (int i=0; i<kb.length && i<32; i++) {
				keyBytes[i] = kb[i];
			}
			
			// Prep value bytes
			byte[] valueBytes = value.getBytes("UTF-8");
			
			// Value size to be included in the message can be specified. If -1 then use the size of the value array
			if (vSize == -1) {
				vSize = valueBytes.length;
			}
			
			// Form message with correct sizes for stuff
			ByteBuffer buffer = ByteBuffer.allocate(1+32+2+valueBytes.length).order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(command).put(keyBytes).putShort((short) vSize).put(valueBytes);
			
			// Pick random node to send request to
			Random rand = new Random(); 
			NodeInfo n = nodes.get(rand.nextInt(nodes.size()));
			
			// Send message
			h = client.send(n.hostName, n.port, new String(buffer.array(), Charset.forName("UTF-8")) );
			
			// Receive message
			client.setTimeout(2000);
			byte[] rcvMsg = client.receive();
			
			byte[] actualMsg = new Header().decodeAndGetMessage(rcvMsg);
			return new Object[]{actualMsg, h};
		} catch(SocketTimeoutException e) {
			return null;
		} finally {
			client.closeSocket();
		}
	}
	
	public Object[] sendWithHeader(byte command, String key, String value, Header header) throws Exception {
		UDPClient client = new UDPClient(4567);
		int vSize = -1;
		try {
			// Prep key bytes
			byte[] keyBytes = new byte[32];
			byte[] kb = key.getBytes("UTF-8");
			
			for (int i=0; i<kb.length && i<32; i++) {
				keyBytes[i] = kb[i];
			}
			
			// Prep value bytes
			byte[] valueBytes = value.getBytes("UTF-8");
			
			// Value size to be included in the message can be specified. If -1 then use the size of the value array
			if (vSize == -1) {
				vSize = valueBytes.length;
			}
			
			// Form message with correct sizes for stuff
			ByteBuffer buffer = ByteBuffer.allocate(1+32+2+valueBytes.length).order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(command).put(keyBytes).putShort((short) vSize).put(valueBytes);
			
			// Pick random node to send request to
			Random rand = new Random(); 
			NodeInfo n = nodes.get(rand.nextInt(nodes.size()));
			
			// Send message
			client.send(n.hostName, n.port, new String(buffer.array(), Charset.forName("UTF-8")), header.getUniqueID());
			
			// Receive message
			client.setTimeout(2000);
			byte[] rcvMsg = client.receive();
			
			byte[] actualMsg = new Header().decodeAndGetMessage(rcvMsg);
			return new Object[]{actualMsg, header};
		} catch(SocketTimeoutException e) {
			return null;
		} finally {
			client.closeSocket();
		}
	}

	public String getValue(byte[] msg) {
		ByteBuffer bf = ByteBuffer.wrap(msg).order(ByteOrder.LITTLE_ENDIAN);
		byte responseCode = bf.get();
		if(bf.hasRemaining()) {
			short valueSize = bf.getShort();
			byte[] rcvValue = new byte[valueSize];
			System.out.println("value size : "+valueSize);
			bf.get(rcvValue, 0, valueSize);
			String value = new String(rcvValue, Charset.forName("UTF-8"));
			return value;
		}
		return "";
	}
	
	public int getResponseCode(byte[] msg) {	
		return msg[0];
	}


}
