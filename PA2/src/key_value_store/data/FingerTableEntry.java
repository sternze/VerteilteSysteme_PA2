package key_value_store.data;


public class FingerTableEntry {
	private int start;
	private int intervalUpperBound;
	private int intervalLowerBound;
	private Node node;
	
	public FingerTableEntry() { }
	
	public FingerTableEntry(int position) {
		
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public boolean isInInterval(int val) {
		return (val > intervalLowerBound && val < intervalUpperBound);
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}


	public int getIntervalUpperBound() {
		return intervalUpperBound;
	}


	public void setIntervalUpperBound(int intervalUpperBound) {
		this.intervalUpperBound = intervalUpperBound;
	}


	public int getIntervalLowerBound() {
		return intervalLowerBound;
	}


	public void setIntervalLowerBound(int intervalLowerBound) {
		this.intervalLowerBound = intervalLowerBound;
	}
	
	
	
}
