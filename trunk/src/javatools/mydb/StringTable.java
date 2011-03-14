package javatools.mydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javatools.administrative.D;

import multir.util.HashCount;

public class StringTable {
	/**
	 * select some columns are then uniq -c put the size at column 0
	 */
	public static List<String[]> squeeze(List<String[]> table, int[] selectedColumns) {
		List<String[]> result = new ArrayList<String[]>();
		HashCount<String> hc = new HashCount<String>();
		for (String[] a : table) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < selectedColumns.length; i++) {
				sb.append(a[selectedColumns[i]]).append("\t");
			}
			hc.add(sb.toString().trim());
		}
		List<String[]> temp = hc.getAll();
		for (String[] b : temp) {
			String[] x = new String[selectedColumns.length + 1];
			x[0] = b[1];
			String[] c = b[0].split("\t");
			for (int i = 1; i < x.length; i++) {
				x[i] = c[i - 1];
			}
			result.add(x);
		}
		return result;
	}

	public static void sortByColumn(List<String[]> table, final int[] columns) {
		Collections.sort(table, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				for (int i = 0; i < columns.length; i++) {
					int cmp = o1[columns[i]].compareTo(o2[columns[i]]);
					if (cmp != 0) {
						return cmp;
					}
				}
				return 0;
			}
		});
	}

	public static void sortByIntColumn(List<String[]> table, final int[] columns) {
		Collections.sort(table, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				for (int i = 0; i < columns.length; i++) {
					int cmp = Integer.parseInt(o2[columns[i]]) - Integer.parseInt(o1[columns[i]]);
					if (cmp != 0) {
						return cmp;
					}
				}
				return 0;
			}
		});
	}

	public static void sortByColumn(List<String[]> table, final int[] columns, final boolean[] isValue) {
		Collections.sort(table, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				for (int i = 0; i < columns.length; i++) {
					int cmp = 0;
					if (isValue[i]) {
						double temp = Double.parseDouble(o2[columns[i]]) - Double.parseDouble(o1[columns[i]]);
						if (temp > 0)
							cmp = 1;
						else if (temp < 0)
							cmp = -1;
						else
							cmp = 0;
					} else {
						cmp = o1[columns[i]].compareTo(o2[columns[i]]);
					}
					if (cmp != 0) {
						return cmp;
					}
				}
				return 0;
			}
		});
	}
}
