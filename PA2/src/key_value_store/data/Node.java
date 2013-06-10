package key_value_store.data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node implements Serializable {
	private long ChordIdentifier;
	private String IP;
	private int port;
	private String ServiceName;
	private Node successor;
	private Node predecessor;
	private int keysize;
	
	public Node(int keysize) {
		port = 0;
		IP = null;
		this.keysize = keysize;
	}
	
	public Node(String ip, int Port, int keysize) {
		this.keysize = keysize;
		this.ChordIdentifier = getChordID(ip + ":" + Port);
		this.IP = ip;
		this.port = Port;
	}
	
	private long getChordID(String IpAndPort) {
		long chordID = 0;
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			byte[] bla = md.digest(IpAndPort.getBytes());
			
			long number = ByteBuffer.wrap(bla).getLong();
			
			long mod = (long)Math.pow(2, keysize);
			
			chordID = number % mod;
			if(chordID < 0) {
				chordID += (long)Math.pow(2, keysize);
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		System.out.println("Key calculated and set");
		
		return chordID;
	}

	public long getChordIdentifier() {
		return ChordIdentifier;
	}
	
	public void setChordIdentifier(long chordIdentifier) {
		ChordIdentifier = chordIdentifier;
	}
	
	public String getIP() {
		return IP;
	}
	
	public void setIP(String iP) {
		IP = iP;
		
		if(this.port != 0) {
			this.ChordIdentifier = getChordID(this.IP + ":" + this.port);
		}
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;

		if(this.IP != null) {
			this.ChordIdentifier = getChordID(this.IP + ":" + this.port);
		}
	}

	public String getServiceName() {
		return ServiceName;
	}

	public void setServiceName(String serviceName) {
		ServiceName = serviceName;
	}

	public Node getSuccessor() {
		return successor;
	}

	public void setSuccessor(Node successor) {
		this.successor = successor;
	}

	public Node getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(Node predecessor) {
		this.predecessor = predecessor;
	}
	
	
}