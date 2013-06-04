package failure_detector.data;

import java.util.Date;

import failure_detector.interfaces.IMyFD;

public class FD {
	
	private Node me;
	private Node first = null;
	private Node second = null;
	private Node third = null;
	private Node pulse = null;
	private boolean timeout = false;
	
	private IMyFD pulseContact = null;
	
	public synchronized Node getMe() {
		return me;
	}
	public synchronized void setMe(Node me) {
		this.me = me;
	}
	public synchronized Node getFirst() {
		return first;
	}
	public synchronized void setFirst(Node first) {
		this.first = first;
	}
	public synchronized Node getSecond() {
		return second;
	}
	public synchronized void setSecond(Node second) {
		this.second = second;
	}
	public synchronized Node getThird() {
		return third;
	}
	public synchronized void setThird(Node third) {
		this.third = third;
	}
	public synchronized Node getPulse() {
		return pulse;
	}
	public synchronized void setPulse(Node pulse) {
		this.pulse = pulse;
	}
	public synchronized IMyFD getPulseContact() {
		return pulseContact;
	}
	public synchronized void setPulseContact(IMyFD pulseContact) {
		this.pulseContact = pulseContact;
	}
	
	public synchronized boolean getTimeout() {
		return timeout;
	}
	
	public synchronized void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}
	
	public void printStatus() {
		System.out.println(new Date() + " my status (" + this.getMe().getFullAddress() +") {");
		System.out.println(new Date() + " \t first: " + this.getFirst().getFullAddress());
		System.out.println(new Date() + " \t second: " + this.getSecond().getFullAddress());
		System.out.println(new Date() + " \t third: " + this.getThird().getFullAddress());
		System.out.println(new Date() + " \t pulse: " + this.getPulse().getFullAddress());
		System.out.println(new Date() + " }");
	}
	
}
