package freebase.jointmatch;

import java.util.Arrays;
import java.util.Comparator;

public class GraphEdge {
	int relationId;
	int size;
	int nitem;
	int[][] pairs_sorta;
	int[][] pairs_sortb;

	public GraphEdge(int size) {
		this.size = size;
		this.nitem = 0;
		// [2] means: arg1 argb
		pairs_sorta = new int[size][2];
		pairs_sortb = new int[size][2];
	}

	public void add(int arg1, int arg2) {
		pairs_sorta[nitem][0] = arg1;
		pairs_sorta[nitem][1] = arg2;
		pairs_sortb[nitem][0] = arg1;
		pairs_sortb[nitem][1] = arg2;
		nitem++;
	}

	public void sort() {
		Arrays.sort(pairs_sorta, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				// TODO Auto-generated method stub
				return o1[0] - o2[0];
			}
		});
		Arrays.sort(pairs_sortb, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				// TODO Auto-generated method stub
				return o1[1] - o2[1];
			}
		});
	}
}
