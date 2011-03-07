package multir.tmp;

import java.io.IOException;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class Tmp {

	static String dir = "/projects/pardosa/data16/raphaelh/t";

	static String input = dir + "/run";
	static String output = input + ".counts";
	
	public static void main(String[] args) throws IOException {
		merge(dir + "/run3-", 0, 8, output);
	}
	
	private static void merge(String inPrefix, int from, int to, String out) throws IOException {
		
		DelimitedWriter w = new DelimitedWriter(out, 256*1024*1024);
		DelimitedReader[] r = new DelimitedReader[to - from];
		for (int i=from; i < to; i++)
			r[i-from] = new DelimitedReader(inPrefix + i, 64*1024*1024);
		String[][] t = new String[to - from][];
		int nonNull = 0;
		for (int i=0; i < t.length; i++) {
			t[i] = r[i].read();
			if (t[i] != null) nonNull++;
		}
		String curKey = "";
		int curCount = 0;
		while (nonNull > 0) {
			int minIndex = -1;
			for (int i=0; i < t.length; i++) {
				if (t[i] == null) continue;
				if (minIndex < 0 || t[i][0].compareTo(t[minIndex][0]) < 0) minIndex = i;
			}
			if (t[minIndex][0].equals(curKey)) {
				curCount ++; //+= Integer.parseInt(t[minIndex][1]);
			} else {
				if (curCount > 0) {
					w.write(curKey, curCount);
				}
				curKey = t[minIndex][0];
				curCount = 1; //Integer.parseInt(t[minIndex][1]);
			}
			t[minIndex] = r[minIndex].read();
			if (t[minIndex] == null) nonNull--;
		}
		if (curCount > 0) {
			w.write(curKey, curCount);
		}
		w.close();
		for (int i=0; i < r.length; i++)
			r[i].close();
	}
	
}
