package failure_detector.data;

import java.io.Serializable;
import java.net.InetAddress;

public class Node implements Serializable {

	private String ServiceName;
	private String IP;
	private int port;
	
	public String getServiceName() {
		return ServiceName;
	}
	public void setServiceName(String serviceName) {
		ServiceName = serviceName;
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
	
	public String getFullAddress() {
		return IP + ":" + port;
	}
}
