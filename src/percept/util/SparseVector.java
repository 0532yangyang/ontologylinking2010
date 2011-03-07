package percept.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SparseVector {

	public int[] ids;		// sorted
	public double[] vals;
	public int num;         // the arrays might not actually be full

	public SparseVector() {
		this.ids = new int[0];
		this.vals = new double[0];
		this.num = 0;
	}
	
	public SparseVector(int[] ids, double[] vals, int num) {
		this.ids = ids;
		this.vals = vals;
		this.num = num;
	}
	
	public void reset() {
		num = 0;
	}
	
	public SparseVector copy() {
		SparseVector n = new SparseVector(new int[num], new double[num], num);
		System.arraycopy(ids, 0, n.ids, 0, num);
		System.arraycopy(vals, 0, n.vals, 0, num);
		return n;
	}
	
	public void scale(float factor) {
		for (int i=0; i < num; i++)
			vals[i] *= factor;
	}
	 
	public double dotProduct(SparseVector v) {
		//return dotProduct(this, v);
		
		int i = 0, j = 0;
		
		double sum = 0;
		while (i < num && j < v.num) {
			if (ids[i] < v.ids[j])
				i++;
			else if (ids[i] > v.ids[j])
				j++;
			else {
				sum += vals[i]*v.vals[j];
				i++; j++;
			}
		}
		return sum;

	}
	
	public void addSparse(SparseVector v, float factor) {
		SparseVector n = sum(this, v, factor);
		this.ids = n.ids;
		this.num = n.num;
		this.vals = n.vals;
	}
	
	//public void sum(SparseVector v, float factor) {
	//}
	
	public static SparseVector scale(SparseVector v, float factor) {
		SparseVector n = v.copy();
		n.scale(factor);
		return n;
	}
	
	public static SparseVector sum(SparseVector v1, SparseVector v2) {
		return sum(v1, v2, 1.0f);
	}
	
	public static SparseVector sum(SparseVector v1, SparseVector v2, float factor) {
		int cn = v1.num + v2.num;
		SparseVector n = new SparseVector(new int[cn], new double[cn], 0);
		
		int i = 0, j = 0, k = 0;
		while (i < v1.num && j < v2.num) {
			if (v1.ids[i] < v2.ids[j]) {
				n.ids[k] = v1.ids[i];
				n.vals[k++] = v1.vals[i++];				
			} else if (v1.ids[i] > v2.ids[j]) {
				n.ids[k] = v2.ids[j];
				n.vals[k++] = factor * v2.vals[j++];
			} else {
				n.ids[k] = v1.ids[i];
				n.vals[k++] = v1.vals[i++] + factor * v2.vals[j++];
			}
		}
		while (i < v1.num) {
			n.ids[k] = v1.ids[i];
			n.vals[k++] = v1.vals[i++];
		}
		while (j < v2.num) {
			n.ids[k] = v2.ids[j];
			n.vals[k++] = factor * v2.vals[j++];
		}
		n.num = k;
		return n;
	}
	
	public static double dotProduct(SparseVector v1, SparseVector v2) {
		return v1.dotProduct(v2);
	}
	
	public void serialize(OutputStream os) 
		throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(this.num);
		for (int i=0; i < this.num; i++) {
			dos.writeInt(this.ids[i]);
			dos.writeDouble(this.vals[i]);
		}
	}
	
	public void deserialize(InputStream is)
		throws IOException {
		DataInputStream dis = new DataInputStream(is);
		this.num = dis.readInt();
		this.ids = new int[this.num];
		this.vals = new double[this.num];
		for (int i=0; i < this.num; i++) {
			this.ids[i] = dis.readInt();
			this.vals[i] = dis.readDouble();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < num; i++) {
			if (i > 0) sb.append(" ");
			sb.append(ids[i]);
		}
		return sb.toString();
	}
}
