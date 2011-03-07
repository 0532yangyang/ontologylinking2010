package multir.util.binary;

import java.nio.ByteBuffer;

public interface SortComparator {
	//public int compare(ByteBuffer b1, int p1, ByteBuffer b2, int p2);
	
	public void setArg1(ByteBuffer b1, int p1);
	public void setArg2(ByteBuffer b2, int p2);
	public int compare();
}