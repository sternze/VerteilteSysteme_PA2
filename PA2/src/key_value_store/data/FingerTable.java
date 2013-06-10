package key_value_store.data;

import java.util.ArrayList;
import java.util.List;

public class FingerTable {
	FingerTableEntry[] entries;
	
	public FingerTable(int keysize) {
		this.entries = new FingerTableEntry[keysize];
		for(int i = 0; i < this.entries.length; i++) {
			this.entries[i] = null;
		}
	}

	public void setSuccessor(FingerTableEntry fte) {
		entries[0] = fte;
	}

	public FingerTableEntry getSuccessor() {
		return entries[0];
	}
	
	public FingerTableEntry get(int i) {
		return entries[0];
	}
	
	public void setFingerTableEntry(FingerTableEntry fte, int index) {
		entries[index] = fte;
	}

	public int length() {
		return entries.length;
	}
	
}
