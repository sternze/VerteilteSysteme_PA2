package key_value_store.data;


public class FingerTableEntry {
	private long start;
	private long intervalUpperBound;
	private long intervalLowerBound;
	private Node node;
	
	public FingerTableEntry() { }
	
	public FingerTableEntry(int position) {
		
	}
	
	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public boolean isInInterval(long val) {
		return (val > intervalLowerBound && val < intervalUpperBound);
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}


	public long getIntervalUpperBound() {
		return intervalUpperBound;
	}


	public void setIntervalUpperBound(long intervalUpperBound) {
		this.intervalUpperBound = intervalUpperBound;
	}


	public long getIntervalLowerBound() {
		return intervalLowerBound;
	}


	public void setIntervalLowerBound(long intervalLowerBound) {
		this.intervalLowerBound = intervalLowerBound;
	}
	
	
	
}
