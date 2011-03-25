package javatools.mydb;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedWriter;

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

	/** Small to big */
	public static void sortByIntColumn(List<String[]> table, final int[] columns) {
		Collections.sort(table, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				for (int i = 0; i < columns.length; i++) {
					int cmp = Integer.parseInt(o1[columns[i]]) - Integer.parseInt(o2[columns[i]]);
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

	public static void sortByColumn(List<String[]> table, final int[] columns, final boolean[] isValue,
			final boolean[] isReverse) {
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
					if (isReverse[i])
						cmp = -1 * cmp;
					if (cmp != 0) {
						return cmp;
					}
				}
				return 0;
			}
		});
	}

	public static void sortUniq(List<String[]> table) {
		int[] columns = new int[table.get(0).length];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = i;
		}
		sortByColumn(table, columns);
		Iterator<String[]> it = table.iterator();
		String[] last = it.next();
		while (it.hasNext()) {
			String[] now = it.next();
			if (strArrCmp(now, last) == 0) {
				it.remove();
			} else {
				last = now;
			}
		}
	}

	public static int strArrCmp(String[] a1, String[] a2) {
		/** If a1 length is not equal to a2 length, it will throws exception */
		for (int i = 0; i < Math.max(a1.length, a2.length); i++) {
			int x = a1[i].compareTo(a2[i]);
			if (x != 0)
				return x;
		}

		return 0;
	}

	public static void print(List<String[]> table, String file, final int MaxColumnWidth) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file);
		int numColumns = table.get(0).length;
		int[] size = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			List<Integer> len = new ArrayList<Integer>();
			for (String[] a : table) {
				len.add(a[i].length());
			}
			Collections.sort(len);
			int sp = Math.min((int) (len.size() * 0.8), len.size() - 1);
			size[i] = Math.min(MaxColumnWidth, len.get(sp));
		}
		// for (String[] a : table) {
		// for (int i = 0; i < numColumns; i++) {
		// size[i] += a[i].length();
		// }
		// }
		// for(int i=0;i<numColumns;i++){
		// size[i] = (int) (size[i]*2.0/table.size());
		// }
		StringBuilder sb = new StringBuilder();
		for (String[] a : table) {
			for (int i = 0; i < numColumns; i++) {
				int l0 = a[i].length();
				String x = "";
				if (l0 > size[i]) {
					x = a[i].substring(0, size[i]);
				} else {
					x = a[i];
				}
				for (int k = 0; k < size[i] - x.length(); k++) {
					sb.append(" ");
				}
				sb.append(x).append(" | ");
			}
			dw.write(sb.toString());
			sb = new StringBuilder();
		}
		dw.close();
	}

	public static List<String[]> selectTopKofBlock(List<String[]>table, int index, int topk)throws IOException{
		List<String[]>result = new ArrayList<String[]>();
		String last = "";
		int lastcount = 0;
		for(String []a:table){
			String key = a[index];
			if(key.equals(last)){
				if(lastcount <topk){
					result.add(a);
					lastcount++;
				}
			}else{
				//new one!
				last = key;
				lastcount = 1;
				result.add(a);
			}
		}
		return result;
	}
}
