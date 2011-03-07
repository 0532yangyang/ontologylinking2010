package multir.util.binary;

public interface Segmenter {
	public int recordLength(byte[] buffer, int start);
}
