package freebase.candidate;

public class OneBf {
	int nid;
	int edge;
	int prev;
	int depth;

	OneBf(int nid, int edge, int prev, int depth) {
		this.nid = nid;
		this.prev = prev;
		this.depth = depth;
		this.edge = edge;
	}
}