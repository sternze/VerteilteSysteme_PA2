package failure_detector;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;

import failure_detector.data.Counter;
import failure_detector.data.FD;
import failure_detector.data.PulseResponse;
import failure_detector.interfaces.IMyFD;

public class PulseGenerator extends Thread {
	
	private FD fd;
	private Counter c;
	
	public PulseGenerator(FD fd) {
		this.fd = fd;
		c = new Counter();
	}
	
	@Override
	public void run() {
		System.out.println(new Date() + " started pulse generator thread");
		while (true) {
			if (fd.getPulse() != null && !fd.getPulse().equals(fd.getMe())) {
				if (fd.getPulseContact() == null) {
    				try {
						IMyFD pulseContact =  (IMyFD) Naming.lookup("rmi://" + fd.getPulse().getFullAddress() + "/" + fd.getPulse().getServiceName());
						fd.setPulseContact(pulseContact);
						System.out.println(new Date() + " changed pulsecontact");
						fd.printStatus();
					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
				
				PulseResponse msg = new PulseResponse();
				msg.setFirst(fd.getFirst());
				msg.setSecond(fd.getSecond());
				try {
					fd.getPulseContact().Pulse(msg);
					System.out.println(new Date() + " sent pulse to  " + fd.getPulse().getFullAddress());
					
					if (!fd.getTimeout() && c.getCounter() > 0)
						c.resetCounter();
					
					Thread.sleep(4000);
				} catch (RemoteException e) {
					System.out.println(new Date() + " can't contact pulse : " + fd.getPulse().getFullAddress());
					c.add();
					
					if (c.getCounter() >= 3) {
						fd.setTimeout(true);
						System.out.println(new Date() + " possible timeout detected");
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
