package key_value_store;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import key_value_store.data.KV;
import key_value_store.data.Node;
import key_value_store.interfaces.IMyKV;

public class MyKV extends UnicastRemoteObject implements IMyKV {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1567123570875873387L;
	
	private static final int MIN_PORT_NUMBER = 8000;
	private static final int MAX_PORT_NUMBER = 10000;
	private static final String DEFAULT_SERVICE_NAME = "PA2_MyKV";
	private static final long TIMEOUT = 10000;
	public static final int KEYLENGTH = 60;
	

	private static KV myKV;
	
	
	private static int port;
	private static String ServiceName = "";
	private static String ConnectionURI = "";
	private static String ContactServiceName = DEFAULT_SERVICE_NAME; 
	


	protected MyKV() throws RemoteException {
		System.out.println(new Date() + " Initializing Node");
	}
	
	
	
	public static void main(String[] args) {
		
		if(args.length >= 1 && !args[0].equals("${ServiceName}")) {
			ServiceName = args[0];
		} else {
			ServiceName = DEFAULT_SERVICE_NAME;
		}
		
		Registry reg = establishRegistry(); 

		try {
			MyKV myKV = new MyKV();
			reg.rebind(ServiceName, myKV);       // Bind object
			
			System.out.println(new Date() + " Registered with registry");
		} catch (RemoteException e) {
			System.out.println(new Date() + " Error: " + e);
		} // try
		
		
		NetworkInterface ni;
		try {
			ni = NetworkInterface.getByName("net4");
			
			Node me = new Node(KEYLENGTH);
			
//			String address = ni.getInetAddresses().nextElement().getHostAddress();
//			String address1 = ni.getInetAddresses().nextElement().getHostAddress();
			me.setIP(getIPAdressOfNic("net4"));
			me.setPort(port);
			me.setServiceName(ServiceName);
			
			System.out.println(new Date() + " created me {");
			System.out.println(new Date() + " \t IP: " + me.getIP());
			System.out.println(new Date() + " \t Port: " + me.getPort());
			System.out.println(new Date() + " \t ServiceName: " + me.getServiceName());
			System.out.println(new Date() + " \t ChordIdentifier: " + me.getChordIdentifier());
			System.out.println(new Date() + " }");
			
			myKV = new KV(me, KEYLENGTH);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (args.length >= 1 && !args[1].equals("${NodeIP:Port}")) {
			ConnectionURI = args[1];
						
			Join(new Node(ConnectionURI.split(":")[0], Integer.parseInt(ConnectionURI.split(":")[1]), KEYLENGTH));      	
			
		} else {
			Join(null);
		}
	}
	
	private static String getIPAdressOfNic(String NIC_Name) {
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
	
	
	/**
	 * Establishes the Remote Registry
	 * @return established Registry
	 */
	private static Registry establishRegistry()  {
		Registry reg = null;
		
		try {
			for (port = MIN_PORT_NUMBER; port <= MAX_PORT_NUMBER && !IsPortAvailable(port); port++);
			
			System.out.println(new Date() + " using port " + port);
			
			reg = LocateRegistry.createRegistry(port);
		} catch (IllegalArgumentException ae) {
			System.out.println(new Date() + " No ports avilable");
			System.exit(0);
		} catch (RemoteException e) {
			try {
				reg = LocateRegistry.getRegistry(); 
			} catch (RemoteException e2) {
				System.out.println(new Date() + " Registry could not be established" + e);
				System.exit(0);
			} // try-catch-e2 
		} // try-catch-e
		
		System.out.println(new Date() + " Registry established"); 
		
		return reg;
	}
	
	private static void Join(Node nodeToJoinTo) {
		myKV.Join(nodeToJoinTo, ServiceName);
	}
	
	
	public Node findSuccessor(long searchID) {
		return myKV.findSuccessor(searchID, ServiceName);
	}

	public Node getClosestPrecedingFinger(long searchID) {
		return myKV.getClosestPrecedingFinger(searchID);
	}

	public Node findPredecessor(long searchID) {
		return myKV.findPredecessor(searchID, ServiceName);
	}
	

	/**
	 * Checks to see if a specific port is available. (implementation from the Apache "camel" project)
	 *
	 * @param port the port to check for availability
	 */
	public static boolean IsPortAvailable(int port) {
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



	@Override
	public void updateFingerTable(Node newNode, int index) throws RemoteException {
		myKV.updateFingerTable(newNode, index, ServiceName);
		
	}





}
