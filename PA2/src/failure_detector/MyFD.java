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
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import failure_detector.data.FD;
import failure_detector.data.JoinResponse;
import failure_detector.data.Node;
import failure_detector.data.PulseResponse;
import failure_detector.interfaces.IMyFD;

public class MyFD extends UnicastRemoteObject implements IMyFD {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int MIN_PORT_NUMBER = 8000;
	private static final int MAX_PORT_NUMBER = 10000;
	private static final String DEFAULT_SERVICE_NAME = "PA2_MyFD";
	private static final long TIMEOUT = 10000;
	
	private static int port;
	private static String ServiceName = "";
	private static String ConnectionURI = "";
	private static String ContactServiceName = DEFAULT_SERVICE_NAME; 
	
	private static FD fd = null;

	private static Timer timer = null;
	private static TimerTask timeoutTask = null;
	
	protected MyFD() throws RemoteException {
		System.out.println(new Date() + " Initializing Node");
		fd = new FD();
		timer = new Timer();
		timeoutTask = new TimeoutTask(fd);
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
			
			System.out.println(new Date() + " Registered with registry");
		} catch (RemoteException e) {
			System.out.println(new Date() + " Error: " + e);
		} // try
		
		NetworkInterface ni;
		try {
			ni = NetworkInterface.getByName("net4");
			
			Node me = new Node();
			me.setIP(ni.getInetAddresses().nextElement().getHostAddress());
			me.setPort(port);
			me.setServiceName(ServiceName);
			
			fd.setMe(me);
			fd.setFirst(me);
			fd.setSecond(me);
			fd.setThird(me);
			fd.setPulse(me);
			
			System.out.println(new Date() + " created me {");
			System.out.println(new Date() + " \t IP: " + fd.getMe().getIP());
			System.out.println(new Date() + " \t Port: " + fd.getMe().getPort());
			System.out.println(new Date() + " \t ServiceName: " + fd.getMe().getServiceName());
			System.out.println(new Date() + " }");
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (args.length >= 1 && !args[1].equals("${NodeIP:Port}")) {
			ConnectionURI = args[1];
						
			if (args.length == 3 && !args[2].equals("${ContactServiceName}"))
				ContactServiceName = args[2];
					
			try {
				JoinThread jt = new JoinThread(ConnectionURI, ServiceName, fd);
				jt.start();
				jt.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			timer.schedule(timeoutTask, TIMEOUT);
			
		} else {
			System.out.println(new Date() + " I'm the first one");
		}
		
		Thread generator = new PulseGenerator(fd);
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
	public JoinResponse JoinRequest(Node caller) throws RemoteException {
		JoinResponse response = new JoinResponse();
		
		System.out.println(new Date() + " getting join request from " + caller.getFullAddress());
		
		response.setFirst(fd.getFirst());
		response.setSecond(fd.getSecond());
		response.setThird(fd.getThird());
		
		if (fd.getThird().equals(fd.getMe()) && fd.getSecond().equals(fd.getMe())) {
			if (fd.getFirst().equals(fd.getMe())) {
				System.out.println(new Date() + " first join request");
				
				synchronized (fd) {
					fd.setPulse(caller);
					fd.setFirst(caller);
				}
				
				response.setSecond(caller);
				response.setThird(caller);
				
				timer.schedule(timeoutTask, TIMEOUT);
			} else {
				System.out.println(new Date() + " second join request");
				changePulse(fd.getFirst(), caller);
				
				synchronized (fd) {
					fd.setSecond(fd.getFirst());
					fd.setFirst(caller);
				}
				
				response.setThird(caller);
			}
		} else {
			changePulse(fd.getFirst(), caller);
			
			synchronized (fd) {
				fd.setThird(fd.getSecond());
				fd.setSecond(fd.getFirst());
				fd.setFirst(caller);
			}
			
		}
		
		fd.printStatus();
		
		return response;
	}
	
	public static void changePulse(Node node, Node to) throws RemoteException {
		try {
			IMyFD prevFirst = (IMyFD) Naming.lookup("rmi://" + node.getFullAddress() + "/" + node.getServiceName());
			
			prevFirst.ChangePulse(to);
			
			prevFirst = null;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void Pulse(PulseResponse msg) throws RemoteException {
//		System.out.println(new Date() + " got pulse (" + msg.getFirst().getFullAddress() + ", " + msg.getSecond().getFullAddress() + ")");
		
		timeoutTask.cancel();
		timeoutTask = new TimeoutTask(fd);
		timer.schedule(timeoutTask, TIMEOUT);
		
		if (msg.getFirst().equals(fd.getMe())) {
			//only two nodes
		} else {
			synchronized (fd) {
				fd.setSecond(msg.getFirst());
				fd.setThird(msg.getSecond());
			}
		}
		
		
//		fd.printStatus();
	}

	
	
	@Override
	public void ChangePulse(Node newPulse) throws RemoteException {
		System.out.println(new Date() + " puls changed to " + newPulse.getFullAddress());
		
		synchronized (fd) {		
			if (fd.getSecond().equals(fd.getMe())) {
				fd.setSecond(newPulse);
			}
				
			if (fd.getThird().equals(fd.getMe())) {
				fd.setThird(newPulse);
			}
				
			fd.setPulse(newPulse);
			fd.setPulseContact(null);
		}
		
		fd.printStatus();
	}

	@Override
	public void Reconnect(Node caller) throws RemoteException {
		System.out.println(new Date() + " got reconnect request from " + caller.getFullAddress());
		
		if (fd.getTimeout()) {
			JoinThread jt = new JoinThread(caller.getFullAddress(), caller.getServiceName(), fd);
			jt.start();
		}
	}
}
