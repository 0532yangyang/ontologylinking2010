package freebase.preprocess;

import java.util.List;

import multir.util.delimited.DelimitedWriter;

import javatools.filehandlers.MergeRead;
import javatools.filehandlers.MergeReadRes;

public class FBLength2Relation {

	/**
	 * @param args
	 */
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/length123";
	static String file_graphsbarg1 = dir + "/fbGraph2.sortbyarg1";
	static String file_graphsbarg2 = dir + "/fbGraph2.sortbyarg2";
	static String file_length2graph = dir + "/fblen2graph";

	public static void fblength2graph() throws Exception {
		DelimitedWriter dw = new DelimitedWriter(file_length2graph);
		MergeRead mr = new MergeRead(file_graphsbarg2, file_graphsbarg1, 1, 0);
		MergeReadRes mrr;
		while ((mrr = mr.read()) != null) {
			List<String[]> r1 = mrr.line1_list;
			List<String[]> r2 = mrr.line2_list;
			for (String[] a : r1)
				for (String[] b : r2) {
					// some filters
					if (a[0].equals(a[1]) || a[0].equals(b[1])) {
						continue;
					}
					// write the middle node at the column 4
					dw.write(a[0], b[1], a[2] + "|" + b[2], a[1]);

				}
		}
		dw.close();
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		fblength2graph();
	}

}
