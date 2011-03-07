package freebase.candidate;

import java.util.List;

class Node {
	int id;
	NodeLs head;
	NodeLs tail;

	public Node(int id) {
		this.id = id;
	}

	public void addEdge(int to, int edgeid) {
		if (head == null) {
			head = new NodeLs(to, edgeid);
			tail = head;
		} else {
			tail.linkAnother(to, edgeid);
			tail = tail.next;
		}
	}

	public List<int[]> getLink() {
		if (head != null)
			return head.getLink();
		return null;
	}

	public String toString() {
		return id + "\t" + head.toString();
	}
}