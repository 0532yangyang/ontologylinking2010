package multir.tmp;

import java.io.IOException;
import java.util.Comparator;

import multir.util.delimited.Sort;

public class FilterFeatures {

	static String dir = "/projects/pardosa/data14/raphaelh/t/ftexp"; 
	
	public static void main(String[] args) throws IOException {
		String f = dir + "/fts1.counts";
		Sort.sort(f, f + ".sorted", dir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int c1 = Integer.parseInt(t1[1]);
				int c2 = Integer.parseInt(t2[1]);
				return c2 - c1;
			}
		});
	}
}
