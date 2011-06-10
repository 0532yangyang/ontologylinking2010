package freebase.preprocess3;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.webapi.LuceneSearch;

public class VisibleSearch {
	/**lucene search didn't work very well, let me do the job myself*/
	public static void luceneSearch() {
		LuceneSearch ls = new LuceneSearch(Main.file_visible + ".luceneIndex");
		List<String[]> list = ls.search("\"/m/01kmd4\" \"/m/0jmk7\"", 1000);
		for (String[] l : list) {
			D.p(l[0]);
		}
	}

	public static void searchPairs(List<String[]> pairs, String file_visible, String output) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		StringTable.sortByColumn(pairs, new int[] { 0 });
		DelimitedReader dr = new DelimitedReader(file_visible);
		List<String[]> ss = dr.readBlock(1);
		List<List<String[]>> blocks = StringTable.toblock(pairs, 0);
		for (List<String[]> block : blocks) {
			String mid = block.get(0)[0];
			while (ss != null && ss.size() > 0 && ss.get(0)[1].compareTo(mid) < 0) {
				ss = dr.readBlock(1);
			}
			if (ss != null && ss.size() > 0 && ss.get(0)[1].equals(mid)) {
				for (String[] b : block) {
					String b0 = b[1];
					for (String[] s : ss) {
						String s0 = s[3];
						if (b0.equals(s0)) {//good match~~~
							dw.write(b[2], s[2], b[0], b[1], s[0], s[4]);
						}
					}
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void test(){
		
	}
	public static void main(String[] args) {
		
	}
}
