package multir.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

import multir.util.FileOperations;

public class WordCount {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	static String input = dir + "/fts/ftsSpan1.10.gz";
	static String output = input + ".counts";
	
	static int MAP_SIZE = 2000000;
	
	public static void main(String[] args) throws IOException {
		
		// initial runs
		int runs = 0;
		int stage = 0;
		HashMap<String,Integer> hm = new HashMap<String,Integer>();
		{
			DelimitedReader r = new DelimitedReader(input, "utf-8", true);
			String[] t = null;
			while ((t = r.read())!= null) {
				if (hm.size() > MAP_SIZE) {
					writeMap(hm, output + stage + "-" + runs++);
					hm.clear();
					System.gc();
				}
				Integer i = hm.get(t[0]);
				if (i==null) i=0;
				i++;
				hm.put(t[0], i);
			}
			writeMap(hm, output + stage + "-" + runs++);
			r.close();
		}
		/*
		// merge runs
		while (runs > 1)
		{
			stage++;
			
			// begin merging
			for (int i=0; i < runs/2; i++) {
				String r1 = output + (stage-1) + "-" + (2*i);
				String r2 = output + (stage-1) + "-" + (2*i + 1);
				
				merge(r1, r2,
						output + stage + "-" + i);
				new File(r1).delete();
				new File(r2).delete();
			}
			if (runs % 2 == 1) {   // 2*(runs/2) equals runs-1
				new File(output + (stage-1) + "-" + 2*(runs/2)).renameTo(
						new File(output + stage + "-" + (runs/2)));
			}
			runs = (runs % 2 == 0) ? runs/2 : runs/2 + 1;
		}

		FileOperations.move(output + stage + "-0", output);
		*/
		
		//int stage = 0;
		//int runs = 151;
		merge(output + stage + "-", 0, runs, output);
	}
	
	private static void merge(String in1, String in2, String out) throws IOException {
		DelimitedWriter w = new DelimitedWriter(out);
		DelimitedReader r1 = new DelimitedReader(in1);
		DelimitedReader r2 = new DelimitedReader(in2);
		String[] t1 = r1.read(), t2 = r2.read();		
		while (t1 != null && t2 != null) {
			int c = t1[0].compareTo(t2[0]);
			if (c < 0) t1 = r1.read();
			else if (c > 0) t2 = r2.read();
			else {
				w.write(t1[0], (Integer.parseInt(t1[1]) + Integer.parseInt(t2[1])));
			}
		}
		r1.close();
		r2.close();
		w.close();
	}
	
	private static void merge(String inPrefix, int from, int to, String out) throws IOException {
		DelimitedWriter w = new DelimitedWriter(out, 128*1024*1024);
		DelimitedReader[] r = new DelimitedReader[to - from];
		for (int i=from; i < to; i++)
			r[i-from] = new DelimitedReader(inPrefix + i);
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
				curCount += Integer.parseInt(t[minIndex][1]);
			} else {
				if (curCount > 0) {
					w.write(curKey, curCount);
				}
				curKey = t[minIndex][0];
				curCount = Integer.parseInt(t[minIndex][1]);
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
	
	
	private static void writeMap(HashMap<String,Integer> hm, String file) throws IOException {
		List<Map.Entry<String,Integer>> m = new ArrayList<Map.Entry<String,Integer>>(hm.entrySet());
		Collections.sort(m, new Comparator<Map.Entry<String,Integer>>() {
			public int compare(Map.Entry<String,Integer> e1, Map.Entry<String,Integer> e2) {
				return e1.getKey().compareTo(e2.getKey()); } });
		DelimitedWriter w = new DelimitedWriter(file);
		for (Map.Entry<String,Integer> e : m)
			w.write(e.getKey(), e.getValue());
		w.close();					
	}
}
