package multir.util;

public class DynamicLongArray {

	static final int INITIAL_SIZE = 100;
	
	private long[] data;
	private int ptr = 0;
	
	public DynamicLongArray() {
		data = new long[INITIAL_SIZE];
	}
	
	public DynamicLongArray(int initialSize) {
		data = new long[initialSize];
	}
	
	public void set(int pos, long val) {
		ensureLength(ptr = pos+1);
		data[pos] = val;
	}
	
	public long get(int pos) {
		return data[pos];
	}
	
	public void append(long val) {
		ensureLength(ptr+1);
		data[ptr++] = val;
	}
	
	private void ensureLength(int l) {
		if (data.length < l) {
			// determine new length
			int nlength = data.length;
			while (nlength < l) nlength *= 2;
			
			long[] ndata = new long[nlength];
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
	
	public long[] getData() {
		return data;
	}	
}