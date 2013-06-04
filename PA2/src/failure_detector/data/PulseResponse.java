package failure_detector.data;

import java.io.Serializable;

public class Response implements Serializable {
	
	private Node first;
	private Node second;
	private boolean forward;
	
	public Response() {
		forward = false;
	}
	
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
	public boolean isForward() {
		return forward;
	}
	public void setForward(boolean forward) {
		this.forward = forward;
	}
	
	
}
