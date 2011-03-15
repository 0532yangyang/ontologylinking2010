package freebase.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeRead;
import javatools.filehandlers.MergeReadRes;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;

import multir.util.delimited.Sort;

public class FBlength0Relation {

	/**
	 * @param args
	 */

	static String[] relation_name = new String[20000];
	static String edgefile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges";
	static HashSet<String> bad = new HashSet<String>();

	public static void loadEdges() throws Exception {
		DelimitedReader dr = new DelimitedReader(edgefile);
		String[] line;
		while ((line = dr.read()) != null) {
			relation_name[Integer.parseInt(line[1])] = line[0];
		}
		for (int i = 0; i < relation_name.length; i++) {
			if (relation_name[i] == null)
				continue;
			if (relation_name[i].startsWith("/type") || relation_name[i].startsWith("/freebase")
					|| relation_name[i].startsWith("/common") || relation_name[i].startsWith("/base")
					|| relation_name[i].startsWith("/user")) {
				bad.add(relation_name[i]);

			}
		}
	}

	public static void step1() throws Exception {
		// TODO Auto-generated method stub
		loadEdges();
		String file = "/projects/pardosa/s5/clzhang/ontologylink/freebase-datadump-quadruples.tsv";
		String output = "/projects/pardosa/s5/clzhang/ontologylink/length123/length0";
		DelimitedReader dr = new DelimitedReader(file);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] line;
		while ((line = dr.read()) != null) {
			// if(!line[1].equals("/type/object/name") &&
			// !line[1].equals("/common/topic/alias") &&
			// line[2].equals("/lang/en")){
			// D.p(line);
			// }
			if (line[1].startsWith("/type") || line[1].startsWith("/freebase") || line[1].startsWith("/common")
					|| line[1].startsWith("/base") || line[1].startsWith("/user")) {
				continue;
			}
			if (line[2].equals("/lang/en")) {
				dw.write(line[0], line[1], line[3]);
			}
		}
		dr.close();
		dw.close();
	}

	/** Generate length0 -> name */
	static Map<String, StringBuilder> mid2namealias = new HashMap<String, StringBuilder>(20000000);

	public static void step2_old() throws Exception {
		// load name
		{
			String file = "/projects/pardosa/s5/clzhang/ontologylink/fbnamealias";
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			while ((line = dr.read()) != null) {
				if (!mid2namealias.containsKey(line[0])) {
					mid2namealias.put(line[0], new StringBuilder());
				}
				mid2namealias.get(line[0]).append(line[1].replaceAll(" ", "_"));
			}
			dr.close();
		}
		{
			String input = "/projects/pardosa/s5/clzhang/ontologylink/length123/length0";
			String output = "/projects/pardosa/s5/clzhang/ontologylink/length123/length0.name";
			DelimitedReader dr = new DelimitedReader(input);
			DelimitedWriter dw = new DelimitedWriter(output);
			String[] line;
			int miss = 0;
			while ((line = dr.read()) != null) {
				StringBuilder nameOfArg1 = mid2namealias.get(line[0]);
				if (nameOfArg1 == null || nameOfArg1.length() == 0) {
					miss++;
					continue;
				}

				dw.write(line[0], line[1], nameOfArg1.toString(), line[2]);
			}
			dr.close();
			dw.close();
			D.p("Missing", miss);
		}
	}

	public static void step2_sort() throws Exception {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink/length123";
		String file = "/projects/pardosa/s5/clzhang/ontologylink/length123/fbnamealias";
		Sort.sort(file, file + ".sort", dir, new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[0].compareTo(o2[0]);
			}

		});
	}

	public static void step2() throws Exception {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink/length123";
		String file1sort = dir + "/fbnamealias.sort";
		String file2 = dir + "/length0";
		String file2sort = dir + "/length0.sort";
		String output = dir + "/length0.name";
		{
			 Sort.sort(file2,file2sort,dir,new Comparator<String[]>() {
			
			 @Override
			 public int compare(String[] o1, String[] o2) {
			 // TODO Auto-generated method stub
			 return o1[0].compareTo(o2[0]);
			 }
			
			 });
		}
		{
			MergeReadStr mr = new MergeReadStr(file1sort, file2sort, 0, 0);
			MergeReadResStr mrr;
			DelimitedWriter dw = new DelimitedWriter(output);
			while ((mrr = mr.read()) != null) {
				List<String[]> names = mrr.line1_list;
				List<String[]> relations = mrr.line2_list;
				for(String []r: relations){
					for(String []n: names){
						dw.write(r[0],r[1],n[1],r[2]);
					}
				}
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws Exception {
		step2();
	}

}
