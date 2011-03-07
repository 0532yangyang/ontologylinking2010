package multir.util.binary;


public interface Comparator {
	public void setArg1(byte[] b1, int p1);
	public void setArg2(byte[] b2, int p2);
	public int compare();
}
