package multir.util;

public class DynamicDoubleArray {

	static final int INITIAL_SIZE = 100;
	
	private double[] data;
	private int ptr = 0;
	
	public DynamicDoubleArray() {
		data = new double[INITIAL_SIZE];
	}

	public DynamicDoubleArray(int initialSize) {
		data = new double[initialSize];
	}

	public void set(int pos, double val) {
		ensureLength(ptr = pos+1);
		data[pos] = val;
	}
	
	public void append(double val) {
		ensureLength(ptr+1);
		data[ptr++] = val;
	}
	
	private void ensureLength(int l) {
		if (data.length < l) {
			// determine new length
			int nlength = data.length;
			while (nlength < l) nlength *= 2;
			
			double[] ndata = new double[nlength];
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
	
	public double[] getData() {
		return data;
	}	
}