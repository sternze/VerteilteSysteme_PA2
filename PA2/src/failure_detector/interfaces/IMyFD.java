package failure_detector.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import failure_detector.data.Node;
import failure_detector.data.Response;

public interface IMyFD extends Remote {

	public Response JoinRequest(Node caller) throws RemoteException;
	public void Pulse(Response msg) throws RemoteException;
	public void ChangePulse(Node newPulse) throws RemoteException;
}
