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
		System.out.println(new Date() + " Timeout detected!");
		System.out.println(new Date() + " Starting lookup for reconnect");
		
		try {
			IMyFD contact = (IMyFD) Naming.lookup("rmi://" + fd.getSecond().getFullAddress() + "/" + fd.getSecond().getServiceName());
			System.out.println(new Date() + " Node found: " + fd.getSecond().getFullAddress());
			contact.ChangePulse(fd.getMe());
			contact = null;
			
			synchronized (fd) {
				fd.setFirst(fd.getSecond());
				fd.setSecond(fd.getThird());
				fd.setThird(fd.getMe());
			}
			
			fd.setTimeout(true);
		} catch (RemoteException re) {
			try {
				IMyFD contact = (IMyFD) Naming.lookup("rmi://" + fd.getThird().getFullAddress() + "/" + fd.getThird().getServiceName());
				System.out.println(new Date() + " Node found: " + fd.getThird().getFullAddress());
				contact.ChangePulse(fd.getMe());
				contact = null;
				
				synchronized (fd) {
					fd.setFirst(fd.getSecond());
					fd.setSecond(fd.getThird());
					fd.setThird(fd.getMe());
				}
				
				fd.setTimeout(true);
			} catch (RemoteException re2) {
				System.out.println("cant recover: more than two nodes crashed");
			} catch (MalformedURLException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} catch (MalformedURLException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	

}
