package multir.tmp;

import java.io.IOException;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class TmpFindLongFts {

	static String dir = "/projects/pardosa/data16/raphaelh/t";

	static String input = dir + "/ftsSpan1.100.counts";
	static String output = dir + "/longfts";
	
	public static void main(String[] args) throws IOException {
		
		DelimitedWriter w = new DelimitedWriter(output);
		DelimitedReader r = new DelimitedReader(input);
		String[] t = null;
		while ((t = r.read())!= null) {
			if (t[0].split(" ").length > 12)
				w.write(t[0]);
		}
		r.close();
		w.close();
		
		
	}
	

}
