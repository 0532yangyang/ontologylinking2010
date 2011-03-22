package freebase.candidate;

public class OneBf {
	public int nid;
	public int edge;
	public int prev;
	public int depth;

	public OneBf(int nid, int edge, int prev, int depth) {
		this.nid = nid;
		this.prev = prev;
		this.depth = depth;
		this.edge = edge;
	}
}