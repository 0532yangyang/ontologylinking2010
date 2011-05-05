package multir.tmp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;

import multir.learning.data.MILDocument;
import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;

public class PairSizes {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	static String exp = "/ftexp3";

	public static void main(String[] args) throws IOException {
	
		{
			DelimitedWriter w = new DelimitedWriter(dir + exp + "/pairSizes");
			MILDocument doc = new MILDocument();
			
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					new FileInputStream(dir + exp + "/train")));
			
			while (doc.read(dis)) {
				w.write(doc.numMentions, doc.arg1, doc.arg2);
			}
			dis.close();
			w.close();
		}
		
		Sort.sort(dir + exp + "/pairSizes", dir + exp + "/pairSizes.sorted",dir + exp, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				return Integer.parseInt(t2[0]) - Integer.parseInt(t1[0]);
			}
		});
	}
}
