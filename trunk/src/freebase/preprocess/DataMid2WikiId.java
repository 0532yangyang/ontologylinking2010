package freebase.preprocess;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

public class DataMid2WikiId {

	static String output1 = "/projects/pardosa/s5/clzhang/ontologylink/mid2Wikiurl_v2";
	static String output2 = "/projects/pardosa/s5/clzhang/ontologylink/mid2WikiId_v2";

	/**
	 * @param args
	 */
	public static void step1_filterquad() throws Exception {
		String file = "/projects/pardosa/s5/clzhang/ontologylink/freebase-datadump-quadruples.tsv";
		DelimitedWriter dw = new DelimitedWriter(output1);
		DelimitedReader dr = new DelimitedReader(file);
		String[] line;
		while ((line = dr.read()) != null) {
			if (line.length >= 4 && line[2].equals("/wikipedia/en") && line[1].equals("/type/object/key")
					&& !line[3].contains("$")) {
				dw.write(line[0], line[3]);
			}
		}
		dr.close();
		dw.close();
	}

	static HashMap<String, Integer> wikiTitle2id = new HashMap<String, Integer>(10000000);
	static HashMap<String, Integer> mid2wikiid = new HashMap<String, Integer>(10000000);
	static HashMap<String, String> mid2wikititle = new HashMap<String, String>(5000000);

	public static void step2_join() throws Exception {
		// load Wiki Article name 2 article id
		{
			String file = "/projects/pardosa/s2/raphaelh/data/all/pagetitles";
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			while ((line = dr.read()) != null) {
				int id = Integer.parseInt(line[0]);
				String str = line[1].toLowerCase().replace(" ", "_");
				wikiTitle2id.put(str, id);
			}
			dr.close();
			D.p("load Page title finished");
		}
		{
			String file = output1;
			String output = output2;
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			while ((line = dr.read()) != null) {
				String mid = line[0];
				String wikititle = line[1];
				mid2wikititle.put(mid, wikititle);
				Integer wikiarticleId = wikiTitle2id.get(wikititle);
				if (wikiarticleId != null) {
					dw.write(mid, wikiarticleId, wikititle);
					// mid2wikiid.put(mid,wikiarticleId);
					// wikiarticleId = -1;
				}

			}
			dr.close();
			dw.close();
			D.p("write wiki id file finished");
		}

		// lookup table
		{
			// String file =
			// "/projects/pardosa/s5/clzhang/ontologylink/fbname_mid_zclid";
			// String output =
			// "/projects/pardosa/s5/clzhang/ontologylink/lookupTable_MidZidWidWkTitle";
			// DelimitedWriter dw = new DelimitedWriter(output);
			// DelimitedReader dr = new DelimitedReader(file);
			// String[] line;
			// int null1 = 0,null2 = 0;
			// while ((line = dr.read()) != null) {
			// String mid = line[0];
			// String zid = line[1];
			// //look up for Wid
			// Integer wid = mid2wikiid.get(mid);
			//
			// //look up for WkTitle
			// String wktitle = mid2wikititle.get(mid);
			// if(wid == null){
			// null1++;
			// wid = -1;
			// }
			// if(wktitle == null){
			// null2++;
			// wktitle = "null";
			// }
			// dw.write(mid,zid,wid,wktitle);
			// }
			// dr.close();
			// dw.close();
			// D.p(null1,null2);
		}
	}

	public static void step3_sort() throws IOException {
		{
			String dir = "/projects/pardosa/s5/clzhang/ontologylink";
			String input = output2;
			String output = output2 + ".sbmid";
			Sort.sort(input, output, dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[0].compareTo(o2[0]);
				}

			});
		}
	}

	public static void step4_getEnid() throws Exception {
		String file = "/projects/pardosa/s5/clzhang/ontologylink/freebasedump.sort";
		String output = "/projects/pardosa/s5/clzhang/ontologylink/mid_en_url";
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(file);
		String[] line;
		while ((line = dr.read()) != null) {
			if (line.length >= 4 && line[2].equals("/en") && line[1].equals("/type/object/key")
					&& !line[3].contains("$")) {
				dw.write(line[0], line[3]);
			}
		}
		dr.close();
		dw.close();
	}

	public static void temp_enidduplicate() throws Exception {

		String input = "/projects/pardosa/s5/clzhang/ontologylink/mid_en_url";
		HashSet<String> set = new HashSet<String>();
		DelimitedReader dr = new DelimitedReader(input);
		String[] line;
		while ((line = dr.read()) != null) {
			if (set.contains(line[1])) {
				D.p(line[1]);
			}
			set.add(line[1]);
		}
		dr.close();
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		 step1_filterquad();
		 step2_join();
		 step3_sort();
		//step4_getEnid();
		//temp_enidduplicate();
	}

}
