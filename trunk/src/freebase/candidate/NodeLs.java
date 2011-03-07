package freebase.candidate;

import java.util.ArrayList;
import java.util.List;

public class NodeLs {
	int nid;
	int edge;
	NodeLs next;

	public NodeLs(int id, int edge) {
		this.nid = id;
		this.edge = edge;
	}

	public void linkAnother(int id, int edge) {
		next = new NodeLs(id, edge);
	}

	public List<int[]> getLink() {
		List<int[]> res = new ArrayList<int[]>();
		NodeLs cur = this;
		while (cur != null) {
			res.add(new int[] { cur.nid, cur.edge });
			cur = cur.next;
		}
		return res;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		NodeLs cur = this;
		while (cur != null) {
			sb.append(cur.nid + ":" + cur.edge + " ");
			cur = cur.next;
		}
		return sb.toString();
	}
}