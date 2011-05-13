package freebase.jointmatch4;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import multir.util.delimited.Sort;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class Another {
	static void step1_getnytWid() throws IOException {

		//String temp1 = file_trainpb+".tmp1allentity";
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(Main.file_myidnyt05pb)));
		String output = Main.file_myidnyt05pb + ".debug";
		DelimitedWriter dw = new DelimitedWriter(output);
		Relation r = null;
		int count = 0;
		Builder relBuilder = null;
		int size = 0;
		int nasize = 0;
		//DelimitedWriter dw = new DelimitedWriter(Main.file_allentitypair);
		List<Relation> narelations = new ArrayList<Relation>();
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			//if(count>10000)break;
			String arg1 = r.getSourceGuid();
			String arg2 = r.getDestGuid();
			/**replace the type id with relation id in S0_reid_nyt0507*/
			String myid = r.getRelType();
			List<RelationMentionRef> list = r.getMentionList();
			for (RelationMentionRef rmf : list) {
				D.p(rmf.toString());
			}
		}
		D.p("success", size);
		dw.close();
	}

	static void temp_grab_raphael_20110428() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance);
			DelimitedWriter dw = new DelimitedWriter(Main.pdir + "/tmp1/sql2instance.subset");
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[2].equals("acquired")
						&& l[3].equals("/organization/organization/companies_acquired|/business/acquisition/company_acquired|")
						|| l[2].equals("actorStarredInMovie")
						&& l[3].equals("/film/actor/film|/film/performance/film|")) {
					dw.write(l);
				}
			}
			dr.close();
			dw.close();
		}
	}

	static void isDuplicate_notablefor() throws IOException {
		/**great news! no duplicate*/
		HashSet<String> set = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				if (set.contains(mid)) {
					D.p("duplicate", mid);
				}
				set.add(mid);
			}
			dr.close();
		}
	}

	static void analyzeWidpairPatterns() throws IOException {
		{
			List<String[]> towrite = new ArrayList<String[]>();
			HashMap<String, HashSet<Long>> map = new HashMap<String, HashSet<Long>>();
			DelimitedReader dr = new DelimitedReader(Main.file_widpair_patterns);
			String[] l;
			while ((l = dr.read()) != null) {
				int w1 = Integer.parseInt(l[0]);
				int w2 = Integer.parseInt(l[1]);
				long value = w1 * 10 ^ 10 + w2;
				String key = l[5];
				if (!map.containsKey(key)) {
					map.put(key, new HashSet<Long>());

				}
				map.get(key).add(value);
			}
			//Iterator<Entry<String,HashSet<Long>>>it = map.entrySet().iterator();
			for (Entry<String, HashSet<Long>> e : map.entrySet()) {
				D.p(e.getKey(), e.getValue().size());
				towrite.add(new String[] { e.getKey(), e.getValue().size() + "" });
			}
			StringTable.sortByIntColumn(towrite, new int[] { 1 });
			DelimitedWriter dw = new DelimitedWriter(Main.file_widpair_patterns + ".debug0");
			for (String[] w : towrite) {
				dw.write(w);
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws Exception {
		//step1_getnytWid();
		//temp_grab_raphael_20110428();
		//isDuplicate_notablefor();
		analyzeWidpairPatterns();

	}
}
