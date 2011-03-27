package multir.tmp;

import java.io.IOException;
import java.util.Comparator;

import javatools.mydb.Sort;


public class Fix {

	static String dir = "/projects/pardosa/data14/raphaelh/t/";
	
	public static void main(String[] args) throws IOException{
		
		Comparator<String[]> comp = new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				return Fix.compare(t1, t2);
			}
		};
		Sort.computeMergedRun(dir + args[0], dir + args[1], dir + args[2], comp);
		
	}
	private static int compare(String a1, String a2, String b1, String b2) {
		int c1 = a1.compareTo(b1);
		if (c1 != 0) return c1;
		int c2 = a2.compareTo(b2);
		return c2;
	}
	
	private static int compare(String[] a, String[] b) {
		return compare(a[0], a[1], b[0], b[1]);
	}

}
