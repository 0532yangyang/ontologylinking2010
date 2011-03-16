package nell.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;




public class ManualMatch2 {

	static String[] relation_name = new String[4000];
	static String[][] relation_words = new String[4000][];
	static HashIndex hi = new HashIndex();
	static List<String[]> all = new ArrayList<String[]>();

	public static void loadRelationNames() throws IOException {
		String relationfile = "/projects/pardosa/data14/raphaelh/t/raw/freebase-relationIDs";
		DelimitedReader dr = new DelimitedReader(relationfile);
		String[] line;
		while ((line = dr.read()) != null) {
			int x = Integer.parseInt(line[0]);
			String y = line[1];
			relation_name[x] = y;
			relation_words[x] = relationName2Words(y);
		}
		dr.close();
	}

	private static String[] relationName2Words(String name) {
		String[] words = name.split("/|_");
		return words;
	}

	public static void loadPairs() throws IOException {
		loadRelationNames();

		String pairfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/freebase-relationInstances-fulltext";
		DelimitedReader dr = new DelimitedReader(pairfile);
		String[] line;
		int lid = 0;

		while ((line = dr.read()) != null) {
			all.add(line);
			String w1 = line[0];
			String w2 = line[1];

			String rname = line[2];
			String[] rwords = relationName2Words(rname);
			hi.indexWord(w1, lid);
			hi.indexWord(w2, lid);
			hi.indexWord(rwords, lid);
			lid++;
		}
		dr.close();
	}

	/**
	 * @param args
	 */
	public static List<String> splitStringByCapital(String a) {
		List<String> result = new ArrayList<String>();
		char[] ary = a.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ary.length; i++) {
			if (ary[i] >= 'A' && ary[i] <= 'Z') {
				String w = sb.toString();
				if (w.length() > 0)
					result.add(a);
				sb = new StringBuilder();
			}
			sb.append(ary[i]);
		}
		return result;

	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String outputfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/manualmatch_help2";
		DelimitedWriter dw = new DelimitedWriter(outputfile);
		loadPairs();
		NellOntology no = new NellOntology();
		for (NellRelation nr : no.nellRelationList) {
			if (nr.seedInstances.size() > 0) {
				List<String> rnamewords = splitStringByCapital(nr.relation_name);
				for (String[] seed : nr.seedInstances) {
					List<String> query = new ArrayList<String>();
					query.addAll(rnamewords);
					query.add(seed[0]);
					query.add(seed[1]);
					List<Integer> ids = new ArrayList<Integer>();
					List<Integer> counts = new ArrayList<Integer>();

					hi.queryWord(query, ids, counts);
					dw.write("=====", nr.relation_name, seed[0], seed[1], "=====");
					System.out.println(nr.relation_name + "\t<" + seed[0] + ", " + seed[1] + ">");
					for (int i = 0; i < ids.size(); i++) {
						int did = ids.get(i);
						int count = counts.get(i);
						String[] m = all.get(did);
						if (count >= 2) {
							dw.write(count, m[0], m[1],m[2]);
						}
						// System.out.println(count+"\t"+m[0]+"\t"+m[1]+"\t"+relation_name[mrel]);
					}
				}
			}
		}
		dw.close();
	}

}
