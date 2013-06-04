package failure_detector;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;

import failure_detector.data.FD;
import failure_detector.data.JoinResponse;
import failure_detector.data.Node;
import failure_detector.interfaces.IMyFD;

public class JoinThread extends Thread {

	private String connectionURI;
	private String serviceName;
	private FD fd;
	
	public JoinThread(String connectionURI, String serviceName, FD fd) {
		this.connectionURI = connectionURI;
		this.serviceName = serviceName;
		this.fd = fd;
	}
	
	@Override
	public void run() {
		try {
        	System.out.println(new Date() + " Starting lookup for join");
			IMyFD contact = (IMyFD) Naming.lookup("rmi://" + connectionURI + "/" + serviceName); 
        	System.out.println(new Date() + " Node found: " + connectionURI);
        	
        	System.out.println(new Date() + " contacting node " + connectionURI);
        	JoinResponse response = contact.JoinRequest(fd.getMe());
        	
        	contact = null;
        	
        	Node contactNode = new Node();
        	contactNode.setServiceName(serviceName);
        	contactNode.setIP(connectionURI.split(":")[0]);
        	contactNode.setPort(Integer.parseInt(connectionURI.split(":")[1]));
        	
			fd.setFirst(response.getFirst());
        	fd.setSecond(response.getSecond());
        	fd.setThird(response.getThird());
        	fd.setPulse(contactNode);
        	fd.setPulseContact(null);
        	
        	fd.printStatus();
        	
        } catch (NotBoundException e) {
        	System.out.println(new Date() + " Node was not found in registry");
        	System.exit(0);
        } catch (java.net.MalformedURLException e) { 
        	System.out.println(new Date() + " URL error: " + e); 
        	System.exit(0); 
        } catch (RemoteException e) { 
        	System.out.println(new Date() + " Time error: " + e); 
        	System.exit(0); 
        }
	}

}
