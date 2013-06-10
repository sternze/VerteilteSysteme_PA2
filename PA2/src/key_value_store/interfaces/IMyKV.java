package key_value_store.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import key_value_store.data.Node;


public interface IMyKV extends Remote {
	public Node findSuccessor(long l) throws RemoteException;
	public Node findPredecessor(long searchID) throws RemoteException;
	public Node getClosestPrecedingFinger(long searchID) throws RemoteException;
	public void updateFingerTable(Node newNode, int index) throws RemoteException;
}
