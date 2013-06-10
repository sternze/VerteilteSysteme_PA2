package key_value_store.data;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Node {
	private long ChordIdentifier;
	private String IP;
	private int port;
	private String ServiceName;
	private Node successor;
	private Node predecessor;
	private int keysize;
	
	public Node() { }
	
	public Node(String ip, int Port, int keysize) {
		this.ChordIdentifier = getChordID(ip + "" + port);
		this.IP = ip;
		this.port = Port;
		this.keysize = keysize;
	}
	
	private long getChordID(String IpAndPort) {
		long chordID = 0;
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			byte[] bla = md.digest(IpAndPort.getBytes());
			
			chordID = ByteBuffer.wrap(bla).getLong() % (long)Math.pow(2, keysize);
			
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
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
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