package key_value_store.data;

import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;

import failure_detector.data.JoinResponse;
import failure_detector.interfaces.IMyFD;

import key_value_store.data.Node;
import key_value_store.interfaces.IMyKV;

import key_value_store.MyKV;

public class KV {
	private Node me;
	private FingerTable fingerTable;
	
	public KV(Node myNode, int keySize) {
		fingerTable = new FingerTable(keySize);
		me = myNode;
	}

	public Node getMe() {
		return me;
	}

	public void setMe(Node me) {
		this.me = me;
	}
	
	public Node getClosestPrecedingFinger(long searchID) {
		for(int i = fingerTable.length(); i > 0; i--) {
			Node tmpNode = fingerTable.get(i).getNode();
			if(tmpNode.getChordIdentifier() > me.getChordIdentifier() && tmpNode.getChordIdentifier() < searchID) {
				return tmpNode;
			}
		}		
		return me;
	}

	public Node findPredecessor(long searchId, String serviceName) {
		Node pred = me;
		while(searchId <= pred.getChordIdentifier() || searchId > pred.getSuccessor().getChordIdentifier()) {
			System.out.println(new Date() + " Getting closest preceding finger of node " + pred.getChordIdentifier());
			try {
				IMyKV contact = (IMyKV) Naming.lookup("rmi://" + pred.getIP() + ":" + pred.getPort() + "/" + serviceName);
	        	System.out.println(new Date() + " Node found: " + pred.getIP() + ":" + pred.getPort());
	        	
	        	System.out.println(new Date() + " contacting node " + pred.getIP() + ":" + pred.getPort());
	        	contact.getClosestPrecedingFinger(searchId);
	        	contact = null;
			} catch (MalformedURLException | RemoteException
					| NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pred;
	}
	
	public Node findSuccessor(int searchId, String serviceName) {
		Node n = findPredecessor(searchId, serviceName);
		return n.getSuccessor();
	}

	public void Join(Node nodeToJoinTo, String serviceName) {
		if(nodeToJoinTo != null) {
			initFingerTable(nodeToJoinTo, serviceName);
			updateOthers(serviceName);
		} else {
			// I'm the only node in the network.
			for(int i = 0; i < fingerTable.length(); i++) {
				FingerTableEntry fte = new FingerTableEntry();
				fte.setNode(me);
				fingerTable.setFingerTableEntry(fte, i);
			}
			
			me.setPredecessor(me);
		}
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
        	
        	successor.setNode(contact.findSuccessor(successor.getStart()));
    		
    		me.setPredecessor(successor.getNode().getPredecessor());
    		successor.getNode().setPredecessor(me);
    		
    		for(int i = 0; i < fingerTable.length()-1; i++) {
    			if(fingerTable.get(i+1).getStart() >= me.getChordIdentifier()
    					&& fingerTable.get(i+1).getStart() < fingerTable.get(i).getNode().getChordIdentifier()) {
    				fingerTable.get(i+1).setNode(fingerTable.get(i).getNode());
    			} else {
    				fingerTable.get(i+1).setNode(contact.findSuccessor(fingerTable.get(i+1).getStart()));
    			}
    		}
        	
        	contact = null;
		} catch (MalformedURLException | RemoteException
				| NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void updateFingerTable(Node newNode, int index, String serviceName) {
		if(newNode.getChordIdentifier() >= me.getChordIdentifier()
				&& newNode.getChordIdentifier() < fingerTable.get(index).getNode().getChordIdentifier()) {
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
	
	
}
