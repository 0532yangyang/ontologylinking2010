package freebase.preprocess;

import java.util.ArrayList;

import percept.util.delimited.DelimitedWriter;

import javatools.administrative.D;
import javatools.filehandlers.MergeReadRes;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;

public class MidEnurlWikiurlWikiid {

	public static void join() throws Exception {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String file1 = dir + "/mid2WikiId.sbmid";
		String file2 = dir + "/mid2enurl.sort";
		String output = dir+"/enid_mid_wid_wtitle";
		DelimitedWriter dw = new DelimitedWriter(output);
		MergeReadStr mrs = new MergeReadStr(file1, file2, 0, 0);
		MergeReadResStr mrr;
		while ((mrr = mrs.read()) != null) {
			ArrayList<String[]> line1 = mrr.line1_list;
			ArrayList<String[]> line2 = mrr.line2_list;
			for (String[] l1 : line1)
				for (String[] l2 : line2) {
					dw.write(l2[1],l1[0],l1[1],l1[2]);
				//	D.p(l1);
				}
		}
		dw.close();

	}

	public static void main(String[] args) throws Exception {
		join();
	}

}
