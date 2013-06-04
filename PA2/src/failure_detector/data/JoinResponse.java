package failure_detector.data;

import java.io.Serializable;

public class JoinResponse implements Serializable {

	private Node first;
	private Node second;
	private Node third;
	public Node getFirst() {
		return first;
	}
	public void setFirst(Node first) {
		this.first = first;
	}
	public Node getSecond() {
		return second;
	}
	public void setSecond(Node second) {
		this.second = second;
	}
	public Node getThird() {
		return third;
	}
	public void setThird(Node third) {
		this.third = third;
	}
	
	
}
