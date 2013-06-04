package failure_detector.data;

public class Counter {
	
	private int counter = 0;
	
	public synchronized void add() {
		this.counter++;
	}
	
	public synchronized void resetCounter() {
		this.counter = 0;
	}
	
	public synchronized int getCounter() {
		return this.counter;
	}
}
