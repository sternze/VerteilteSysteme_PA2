package failure_detector;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.TimerTask;

import failure_detector.data.Counter;
import failure_detector.data.FD;
import failure_detector.interfaces.IMyFD;

public class TimeoutTask extends TimerTask {

	private Counter c = new Counter();
	private FD fd;
	
	public TimeoutTask(FD fd) {
		this.fd = fd;
	}
	
	@Override
	public void run() {
		System.out.println(new Date() + " Timout");
		System.out.println(new Date() + " Starting lookup for reconnect");
		
		try {
			IMyFD contact = (IMyFD) Naming.lookup("rmi://" + fd.getSecond().getFullAddress() + "/" + fd.getSecond().getServiceName());
			System.out.println(new Date() + " Node found: " + fd.getSecond().getFullAddress());
			contact.Reconnect(fd.getMe());
			contact = null;
			fd.setTimeout(true);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	

}
