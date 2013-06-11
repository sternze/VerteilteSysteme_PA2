package key_value_store.data;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;

import key_value_store.interfaces.IMyKV;

public class KV {
	private Node me;
	private FingerTable fingerTable;
	private int keySize;
	
	public KV(Node myNode, int keySize) {
		fingerTable = new FingerTable(keySize);
		me = myNode;
		this.keySize = keySize;
		create();
	}

	public Node getMe() {
		return me;
	}

	public void setMe(Node me) {
		this.me = me;
	}
	
	public Node getClosestPrecedingFinger(long searchID) {
		for(int i = fingerTable.length()-1; i >= 0; i--) {
			Node tmpNode = fingerTable.get(i).getNode();
			if(tmpNode != null && (tmpNode.getChordIdentifier() == me.getChordIdentifier() || tmpNode.getChordIdentifier() == searchID)) {
				return tmpNode;
			}
		}		
		return me;
	}

	public Node findPredecessor(long searchId, String serviceName) {
		Node pred = me;
		
		while(searchId > pred.getChordIdentifier() && searchId <= pred.getSuccessor().getChordIdentifier()) {
			System.out.println(new Date() + " (" + searchId + ", " + pred.getChordIdentifier() + ", " + pred.getSuccessor().getChordIdentifier() + ")");
			System.out.println(new Date() + " Getting closest preceding finger of node " + pred.getChordIdentifier());
			try {
				IMyKV contact = (IMyKV) Naming.lookup("rmi://" + pred.getIP() + ":" + pred.getPort() + "/" + serviceName);
	        	System.out.println(new Date() + " Node found: " + pred.getIP() + ":" + pred.getPort());
	        	
	        	System.out.println(new Date() + " contacting node " + pred.getIP() + ":" + pred.getPort());
	        	pred = contact.getClosestPrecedingFinger(searchId);
	        	
	        	System.out.println(new Date() + " got (" + pred.getChordIdentifier() + ")");
	        	contact = null;
	        	
			} catch (MalformedURLException | RemoteException
					| NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pred;
	}
	
	public Node findSuccessor(long searchId, String serviceName) {
		Node n = findPredecessor(searchId, serviceName);
		return n.getSuccessor();
	}

	public void Join(Node nodeToJoinTo, String serviceName) {
		System.out.println("I'm the only one in the network.");
		FingerTableEntry fte = new FingerTableEntry();
		fte.setNode(me);
		fte.setStart(calculateStart(me, 0));
		fingerTable.setSuccessor(fte);
		me.setPredecessor(null);
		
		
		if(nodeToJoinTo != null) {
			System.out.println("joining node " + nodeToJoinTo.getChordIdentifier());
			initFingerTable(nodeToJoinTo, serviceName);
			updateOthers(serviceName);
		}
		System.out.println("join finished");
	}

	private long calculateStart(Node me2, int I_th_Entry) {
		return (long)((me2.getChordIdentifier() + (long)Math.pow(2, I_th_Entry)) % Math.pow(2, keySize));
	}

	private void updateOthers(String serviceName) {
		for(int i = 0; i < fingerTable.length(); i++) {
			Node pred = findPredecessor((me.getChordIdentifier() - (long)(Math.pow(2, i-1))) % (long)(Math.pow(2, fingerTable.length())), serviceName);
			try {
				IMyKV contact = (IMyKV) Naming.lookup("rmi://" + pred.getIP() + ":" + pred.getPort() + "/" + serviceName);
	        	System.out.println(new Date() + " Node found: " + pred.getIP() + ":" + pred.getPort());
	        	
	        	System.out.println(new Date() + " contacting node " + pred.getIP() + ":" + pred.getPort());
	        	contact.updateFingerTable(me, i);
	        	contact = null;
			} catch (MalformedURLException | RemoteException
					| NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initFingerTable(Node nodeToJoinTo, String serviceName) {
    	FingerTableEntry successor = fingerTable.getSuccessor();
		try {
			IMyKV contact = (IMyKV) Naming.lookup("rmi://" + nodeToJoinTo.getIP() + ":" + nodeToJoinTo.getPort() + "/" + serviceName);
        	System.out.println(new Date() + " Node found: " + nodeToJoinTo.getIP() + ":" + nodeToJoinTo.getPort());

        	System.out.println(new Date() + " contacting node " + nodeToJoinTo.getIP() + ":" + nodeToJoinTo.getPort());
        	
        	try {
    			Thread.sleep(2000);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        	Node succ = contact.findSuccessor(successor.getStart());
        	successor.setNode(succ);
        	
        	System.out.println("got successor " + succ.getChordIdentifier());
    		
    		me.setPredecessor(successor.getNode().getPredecessor());
    		successor.getNode().setPredecessor(me);
    		successor.setStart(calculateStart(succ, 0));
    		
    		for(int i = 0; i < fingerTable.length()-1; i++) {
    			if(fingerTable.get(i+1).getStart() >= me.getChordIdentifier()
    					&& fingerTable.get(i+1).getStart() < fingerTable.get(i).getNode().getChordIdentifier()) {
    				fingerTable.get(i+1).setNode(fingerTable.get(i).getNode());
    			} else {
    				Node contactSucc = contact.findSuccessor(fingerTable.get(i+1).getStart());
    				fingerTable.get(i+1).setNode(contactSucc);
    			}
				fingerTable.get(i+1).setStart(calculateStart(fingerTable.get(i+1).getNode(), i+1));
				fingerTable.get(i+1).setIntervalLowerBound(fingerTable.get(i).getStart());
				fingerTable.get(i+1).setIntervalUpperBound(fingerTable.get(i+1).getStart());
    		}
        	
        	contact = null;
		} catch (MalformedURLException | RemoteException
				| NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void updateFingerTable(Node newNode, int index, String serviceName) {
		if(newNode.getChordIdentifier() >= me.getChordIdentifier() && newNode.getChordIdentifier() < fingerTable.get(index).getNode().getChordIdentifier()) {
			FingerTableEntry fte = new FingerTableEntry();
			fte.setNode(newNode);			
			fingerTable.setFingerTableEntry(fte, index);
			
			Node pred = me.getPredecessor();
			
			try {
				IMyKV contact = (IMyKV) Naming.lookup("rmi://" + pred.getIP() + ":" + pred.getPort() + "/" + serviceName);
	        	System.out.println(new Date() + " Node found: " + pred.getIP() + ":" + pred.getPort());
	        	
	        	System.out.println(new Date() + " contacting node " + pred.getIP() + ":" + pred.getPort());
	        	contact.updateFingerTable(newNode, index);
	        	contact = null;
			} catch (MalformedURLException | RemoteException
					| NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void create() {
		me.setPredecessor(null);
		me.setSuccessor(me);
	}
	
	
}
