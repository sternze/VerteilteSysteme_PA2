package failure_detector;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

import failure_detector.data.Node;
import failure_detector.interfaces.IMyFD;

public class MyFD extends UnicastRemoteObject implements IMyFD {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int MIN_PORT_NUMBER = 8000;
	private static final int MAX_PORT_NUMBER = 10000;
	private static final String DEFAULT_SERVICE_NAME = "PA2_MyFD";
	private static int port;
	private static String ServiceName = "";
	private static String ConnectionURI = "";
	private static String ContactServiceName = DEFAULT_SERVICE_NAME; 
	
	private static Node me;
	private static Node first = null;
	private static Node second = null;
	private static Node third = null;
	private static Node pulse = null;

	protected MyFD() throws RemoteException {
		System.out.println("Initializing Node");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length >= 1 && !args[0].equals("${ServiceName}")) {
			ServiceName = args[0];
		} else {
			ServiceName = DEFAULT_SERVICE_NAME;
		}
		
		Registry reg = establishRegistry(); 

		try {
			MyFD myFD = new MyFD();
			reg.rebind(ServiceName, myFD);       // Bind object
			
			System.out.println("Registered with registry");
		} catch (RemoteException e) {
			System.out.println("Error: " + e);
		} // try
		
		NetworkInterface ni;
		try {
			ni = NetworkInterface.getByName("net4");
			
			me.setIP(ni.getInetAddresses().nextElement().getHostAddress());
			me.setPort(port);
			me.setServiceName(ServiceName);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (args.length >= 1 && !args[1].equals("${NodeIP:Port}")) {
			ConnectionURI = args[1];
			
			IMyFD contact = null;
			
			if (args.length == 3 && !args[2].equals("${ContactServiceName}"))
				ContactServiceName = args[2];
			
			try {
	        	System.out.println("Starting Lookup");
	        	contact = (IMyFD) Naming.lookup("rmi://" + ConnectionURI + "/" + ContactServiceName); 
	        	System.out.println("Node found");
	        	
	        	System.out.println("contacting node");
	        	contact.JoinRequest(me);
	        	
	        } catch (NotBoundException e) {
	        	System.out.println("Node was not found in registry");
	        	System.exit(0);
	        } catch (java.net.MalformedURLException e) { 
	        	System.out.println("URL error: " + e); 
	        	System.exit(0); 
	        } catch (RemoteException e) { 
	        	System.out.println("Time error: " + e); 
	        	System.exit(0); 
	        }
			
		} else {
			System.out.println("I'm the first one");
		}
	}
	
	/**
	 * Establishes the Remote Registry
	 * @return established Registry
	 */
	private static Registry establishRegistry()  {
		Registry reg = null;
		
		try {
			for (port = MIN_PORT_NUMBER; port <= MAX_PORT_NUMBER && !IsPortAvailable(port); port++);
			
			System.out.println("using port " + port);
			
			reg = LocateRegistry.createRegistry(port);
		} catch (IllegalArgumentException ae) {
			System.out.println("No ports avilable");
			System.exit(0);
		} catch (RemoteException e) {
			try {
				reg = LocateRegistry.getRegistry(); 
			} catch (RemoteException e2) {
				System.out.println("Registry could not be established" + e);
				System.exit(0);
			} // try-catch-e2 
		} // try-catch-e
		
		System.out.println("Registry established"); 
		
		return reg;
	}
	
	/**
	 * Checks to see if a specific port is available. (implementation from the Apache "camel" project)
	 *
	 * @param port the port to check for availability
	 */
	private static boolean IsPortAvailable(int port) {
	    if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}
	
	private String getIPAdressOfNic(String NIC_Name) {
		String IP = null;
		try {
			NetworkInterface ni = NetworkInterface.getByName(NIC_Name);
			
		    Enumeration<InetAddress> addresses = ni.getInetAddresses();
		    while (addresses.hasMoreElements()){
		        InetAddress current_addr = addresses.nextElement();
		        if (current_addr.isLoopbackAddress() || !(current_addr instanceof Inet4Address))
		        	continue;
		        IP = current_addr.getHostAddress();
		    }
		} catch(Exception ex) { }
		
		return IP;
	}
	
	private String getIPAdressOfAllNics() {
		//fuck you
		String IP = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
			    NetworkInterface current = interfaces.nextElement();
			    System.out.println(current);
			    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			    Enumeration<InetAddress> addresses = current.getInetAddresses();
			    while (addresses.hasMoreElements()){
			        InetAddress current_addr = addresses.nextElement();
			        if (current_addr.isLoopbackAddress() || !(current_addr instanceof Inet4Address))
			        	continue;
			        IP = current_addr.getHostAddress();
			    }
			}
		} catch(Exception ex) { }
		
		return IP;
	}

	@Override
	public String JoinRequest(Node caller) throws RemoteException {

		return null;
	}

}
