package freebase.jointmatch9;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class Evaluation {

	static void calc_groud_truth() {
		int x = 0;
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] a : nr.seedInstances) {
				D.p(x, nr.relation_name, a[0], a[1]);
				x++;
			}
		}
	}

	static void calc_groud_truth2() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_jointclause + ".1");
		List<String[]> all = dr.readAll();
		{
			Set<String> s = new HashSet<String>();
			for (String[] l : all) {
				if (l[0].equals("relation_instance")) {
					String key = l[6].split("::")[0] + "\t" + l[7].split("::")[0] + "\t" + l[8].split("::")[0];
					s.add(key);
				}
			}
			D.p("true relation instances", s.size());
		}
		{
			Set<String> s = new HashSet<String>();
			for (String[] l : all) {
				if (l[0].equals("static_type")) {
					String key = l[4].split("::")[0];
					s.add(key);
				}
			}
			D.p("types", s.size());
		}

		{
			Set<String> s = new HashSet<String>();
			for (String[] l : all) {
				if (l[0].equals("static_entity")) {
					String key = l[4].split("::")[0];
					s.add(key);
				}
			}
			D.p("entity", s.size());
		}

		{
			Set<String> s = new HashSet<String>();
			for (String[] l : all) {
				if (l[0].equals("type_instance")) {
					String key = l[5].split("::")[0] + "\t" + l[6].split("::")[0];
					s.add(key);
				}
			}
			D.p("type instance", s.size());
		}
	}

	static void prOfOntologyMapping() throws IOException {
		HashMap<String, Set<String>> pair2Rel = new HashMap<String, Set<String>>();
		//HashMap<String,Set<String>>silverPair2Rel = new HashMap<String,Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gold_sql2instance);
			String[] l;
			while ((l = dr.read()) != null) {
				StringTable.mapKey2SetAdd(pair2Rel, l[0] + "\t" + l[1], "g:" + l[3]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gold_sql2instance);
			String[] l;
			while ((l = dr.read()) != null) {
				StringTable.mapKey2SetAdd(pair2Rel, l[0] + "\t" + l[1], "s:" + l[3]);
			}
		}
		{
			HashMap<String, int[]> pr = new HashMap<String, int[]>();
			DelimitedWriter dw = new DelimitedWriter(Main.file_gold_sql2instance + ".pr1");
			DelimitedWriter dw2 = new DelimitedWriter(Main.file_gold_sql2instance + ".pr2");
			for (Entry<String, Set<String>> e : pair2Rel.entrySet()) {
				StringBuilder sb = new StringBuilder();
				String key = e.getKey();
				String[] abkey = key.split("\t");
				Set<String> value = e.getValue();
				String silver = "";
				String gold = "";
				for (String a : value) {
					sb.append(a + " ");
					if (a.startsWith("s:")) {
						silver = a.replace("s:", "");
					}
					if (a.startsWith("g:")) {
						gold = a.replace("g:", "");
					}
				}
				if (silver.equals(gold)) {
					updatePR(pr, silver, 0);
				} else {
					updatePR(pr, silver, 1);
					updatePR(pr, gold, 2);
				}
				dw.write(abkey[0], abkey[1], sb.toString());
			}
			dw.close();
			for (Entry<String, int[]> pr0 : pr.entrySet()) {
				int[] v = pr0.getValue();
				dw2.write(pr0.getKey(), v[0], v[1], v[2]);
			}
			dw2.close();
		}
	}

	private static void updatePR(HashMap<String, int[]> pr, String rel, int x) {
		if (!pr.containsKey(rel)) {
			pr.put(rel, new int[3]);
		}
		pr.get(rel)[x]++;
	}

	static void averageSeed() throws IOException {
		int up = 0;
		int down = 0;
		for (NellRelation nr : Main.no.nellRelationList) {
			List<String[]> list = nr.seedInstances;
			if (list.size() > 0) {
				down++;
				up += list.size();
				D.p(list.size());
			}
		}
		D.p(up * 1.0 / down);

	}

	static void seedVsExtended() throws IOException {
		DelimitedReader dr = new DelimitedReader( "o:/unix/projects/pardosa/s5/clzhang/ontologylink/jointmatch11/pipeline/wikidata/factpair_mentions");
		String[] l;
		int num_extended = 0, num_seed = 0;

		while ((l = dr.read()) != null) {
			
				if (l[11].equals("s")) {
					num_seed++;
				}
				num_extended++;
			
		}
		D.p(num_seed, num_extended);
	}

	public static void main(String[] args) throws IOException {
		//calc_groud_truth2();
		//prOfOntologyMapping();
		//averageSeed();\
		seedVsExtended();
	}
}
