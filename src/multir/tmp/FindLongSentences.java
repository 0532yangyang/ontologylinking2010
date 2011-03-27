package multir.tmp;

import java.io.IOException;
import java.util.Comparator;

import javatools.mydb.Sort;


public class FindLongSentences {

	static String dir = "/projects/pardosa/data14/raphaelh/t";
	
	static String input = dir + "/raw/sentences.tokens";
	static String output = dir + "/long-sentences";
	
	public static void main(String[] args) throws IOException {
		
		Sort.sort(input, output, dir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int n1 = t1[1].split(" ").length;
				int n2 = t2[1].split(" ").length;
				return n2 - n1;
			}
		});
		
	}
	
}
