package multir.util.binary;

public interface SortComponentsFactory {
	public Comparator createComparator();
	public Segmenter createSegmenter();
}