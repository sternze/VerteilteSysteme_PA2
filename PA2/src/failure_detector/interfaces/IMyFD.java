package failure_detector.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import failure_detector.data.Node;

public interface IMyFD extends Remote {

	public String JoinRequest(Node caller) throws RemoteException;
}
