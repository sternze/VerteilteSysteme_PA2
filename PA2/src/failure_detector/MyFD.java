package failure_detector;

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
import java.util.Enumeration;

import failure_detector.data.Node;
import failure_detector.data.Response;
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
	
	private static IMyFD pulseContact = null;
	private static IMyFD contact = null;

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
			
			me = new Node();
			me.setIP(ni.getInetAddresses().nextElement().getHostAddress());
			me.setPort(port);
			me.setServiceName(ServiceName);
			
			first = me;
			second = me;
			third = me;
			
			System.out.println("created me {");
			System.out.println("\t IP: " + me.getIP());
			System.out.println("\t Port: " + me.getPort());
			System.out.println("\t ServiceName: " + me.getServiceName());
			System.out.println("}");
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (args.length >= 1 && !args[1].equals("${NodeIP:Port}")) {
			ConnectionURI = args[1];
						
			if (args.length == 3 && !args[2].equals("${ContactServiceName}"))
				ContactServiceName = args[2];
			
			try {
	        	System.out.println("Starting lookup for join");
	        	contact = (IMyFD) Naming.lookup("rmi://" + ConnectionURI + "/" + ContactServiceName); 
	        	System.out.println("Node found");
	        	
	        	System.out.println("contacting node " + ConnectionURI);
	        	Response response = contact.JoinRequest(me);
	        	
	        	Node contactNode = new Node();
	        	contactNode.setServiceName(ContactServiceName);
	        	contactNode.setIP(ConnectionURI.split(":")[0]);
	        	contactNode.setPort(Integer.parseInt(ConnectionURI.split(":")[1]));
	        	
	        	if (response.getFirst().equals(me) && response.getSecond().equals(contactNode)) {
	        		first = contactNode;
	        		pulse = contactNode;
	        	} else if (response.getFirst().equals(me) && !response.getSecond().equals(contactNode)) {
	        		// da bullshit!!!!!!
	        		first = response.getSecond();
	        		second = contactNode;
	        		pulse = contactNode;
	        	} else {
	        		pulse = contactNode;
	        		first = response.getSecond();
	        	}
	        	
	        	printStatus();
	        	
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
		
		
		Thread generator = new Thread() {
    		public void run() {
    			System.out.println("started pulse generator thread");
    			while (true) {
    				if (pulse != null) {
	    				synchronized (pulse) {
							if (pulseContact == null) {
			    				try {
		    						pulseContact =  (IMyFD) Naming.lookup("rmi://" + pulse.getFullAddress() + "/" + ContactServiceName);
		    						System.out.println("changed pulsecontact");
								} catch (MalformedURLException | RemoteException | NotBoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
							}
								
							synchronized (pulseContact) {
								Response msg = new Response();
								msg.setFirst(first);
								msg.setSecond(second);
								try {
									pulseContact.Pulse(msg);
									System.out.println("sent pulse to " + pulse.getFullAddress());
									
									Thread.sleep(4000);
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			    			}
						}
					}
    			}
    			
    		}
    	};
    	generator.start();
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
		//asdf
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
	public Response JoinRequest(Node caller) throws RemoteException {
		Response response = new Response();
		
		System.out.println("getting join request from " + caller.getFullAddress());
		
		if (third.equals(me) && second.equals(me)) {
			if (first.equals(me)) {
				System.out.println("first join request");
				pulse = caller;
				first = caller;
			} else {
				System.out.println("second join request");
				
				try {
					IMyFD prevFirst = (IMyFD) Naming.lookup("rmi://" + first.getFullAddress() + "/" + first.getServiceName());
					
					prevFirst.ChangePulse(caller);
					
					prevFirst = null;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				second = first;
				first = caller;
			}
		} else {
			try {
				IMyFD prevFirst = (IMyFD) Naming.lookup("rmi://" + first.getFullAddress() + "/" + first.getServiceName());
				
				prevFirst.ChangePulse(caller);
				
				prevFirst = null;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			third = second;
			second = first;
			first = caller;
		}
		
		response.setFirst(first);
		response.setSecond(second);
		
		printStatus();
		
		return response;
	}

	@Override
	public void Pulse(Response msg) throws RemoteException {
		System.out.println("got pulse");
		
		if (msg.getFirst().equals(me)) {
			// only two nodes
		} else if (msg.getSecond().equals(me) && third.equals(me) && !msg.getFirst().equals(pulse) && !msg.getFirst().equals(second)) {
			pulse = msg.getFirst();
			second = msg.getFirst();
			printStatus();
		} else {
			second = msg.getFirst();
			third = msg.getSecond();
			printStatus();
		}
	}

	private static void printStatus() {
		System.out.println("my status (" + me.getFullAddress() +") {");
		System.out.println("\t first: " + first.getFullAddress());
		System.out.println("\t second: " + second.getFullAddress());
		System.out.println("\t third: " + third.getFullAddress());
		System.out.println("\t pulse: " + pulse.getFullAddress());
		System.out.println("}");
	}

	@Override
	public void ChangePulse(Node newPulse) throws RemoteException {
		System.out.println("puls changed to " + newPulse.getFullAddress());
		/*synchronized (pulse) {
			synchronized (third) {
				synchronized (pulseContact) {*/
					if (third.equals(me))
						third = newPulse;
					
					pulse = newPulse;
					pulseContact = null;
					
					printStatus();
				/*}
			}
		}		*/
	}
}
