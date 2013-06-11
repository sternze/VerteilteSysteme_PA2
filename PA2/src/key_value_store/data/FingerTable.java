package key_value_store.data;

import java.util.ArrayList;
import java.util.List;

public class FingerTable {
	//private FingerTableEntry[] entries;
	private List<FingerTableEntry> entries;
	
	public FingerTable(int keysize) {
		this.entries = new ArrayList<FingerTableEntry>();
	}

	public void setSuccessor(FingerTableEntry fte) {
		if(entries.size() == 0) {
			entries.add(fte);
		} else {
			entries.set(0, fte);
		}
	}

	public FingerTableEntry getSuccessor() {
		return entries.get(0);
	}
	
	public FingerTableEntry get(int i) {
		return entries.get(i);
	}
	
	public void setFingerTableEntry(FingerTableEntry fte, int index) {
		if(index < entries.size()) {
			entries.set(index, fte);
		} else {
			entries.add(fte);
		}
	}

	public int length() {
		return entries.size();
	}
	
}
