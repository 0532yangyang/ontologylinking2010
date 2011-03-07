package multir.util;

public class DynamicIntArray {

	static final int INITIAL_SIZE = 100;
	
	private int[] data;
	private int ptr = 0;
	
	public DynamicIntArray() {
		data = new int[INITIAL_SIZE];
	}
	
	public DynamicIntArray(int initialSize) {
		data = new int[initialSize];
	}
	
	public void set(int pos, int val) {
		ensureLength(pos+1);
		data[pos] = val;
	}
	
	public int get(int pos) {
		return data[pos];
	}
	
	public void append(int val) {
		ensureLength(ptr+1);
		data[ptr++] = val;
	}
	
	private void ensureLength(int l) {
		if (data.length < l) {
			// determine new length
			int nlength = data.length;
			while (nlength < l) nlength *= 2;
			
			int[] ndata = new int[nlength];
			System.arraycopy(data, 0, ndata, 0, ptr);
			data = ndata;
		}
	}
	
	public int length() {
		return ptr;
	}
	
	public void clear() {
		ptr = 0;
	}
	
	public int[] getData() {
		return data;
	}
}