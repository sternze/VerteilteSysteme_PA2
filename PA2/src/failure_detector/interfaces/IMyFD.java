package failure_detector.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import failure_detector.data.JoinResponse;
import failure_detector.data.Node;
import failure_detector.data.PulseResponse;

public interface IMyFD extends Remote {

	public JoinResponse JoinRequest(Node caller) throws RemoteException;
	public void Pulse(PulseResponse msg) throws RemoteException;
	public void ChangePulse(Node newPulse) throws RemoteException;
	public void Reconnect(Node caller) throws RemoteException;
}
