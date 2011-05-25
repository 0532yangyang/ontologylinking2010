package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S9X {

	/**Each line of the matching result is
	 * nellrelation; fbrelation; arg1type; arg2type
	 * @throws IOException */
	public static void extendWithTypeConstrain(String file_matchingresult, String file_sql2instance, String output)
			throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		HashMap<Integer, String> map_notabletype = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[1]);
				map_notabletype.put(wid, l[2]);
			}
		}
		HashMap<String, String> allowedfbreltypesign2nellrel = new HashMap<String, String>();
		{
			List<String[]> matchingresult = (new DelimitedReader(file_matchingresult)).readAll();
			for (String l[] : matchingresult) {
				String key = l[1] + "\t" + l[2] + "\t" + l[3];
				allowedfbreltypesign2nellrel.put(key, l[0]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_sql2instance);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				String type1 = "";
				String type2 = "";
				String nellr = l[2];
				String fbr = l[3];

				if (map_notabletype.containsKey(wid1) && map_notabletype.containsKey(wid2)) {
					type1 = map_notabletype.get(wid1);
					type2 = map_notabletype.get(wid2);
				} else {
					continue;
				}
				String tolook = fbr + "\t" + type1 + "\t" + type2;
				if (allowedfbreltypesign2nellrel.containsKey(tolook)) {
					String newnellr = allowedfbreltypesign2nellrel.get(tolook);
					if (newnellr.equals(nellr)) {
						dw.write(l);
					}
				}
			}
		}
		dw.close();
	}

	public static void filterByWikiLink(String raw_extendedpairs, String filtered_extendedpairs) throws IOException {
		HashMap<Long, String[]> store = new HashMap<Long, String[]>();
		DelimitedWriter dw = new DelimitedWriter(filtered_extendedpairs);
		{
			DelimitedReader dr = new DelimitedReader(raw_extendedpairs);
			String[] l;
			while ((l = dr.read()) != null) {
				long key = StringTable.intPair2Long(l[0], l[1]);
				store.put(key, l);
			}
			dr.close();
		}

		{
			HashSet<Long> appeared = new HashSet<Long>();
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			String[] l;
			while ((l = dr.read()) != null) {
				long key1 = StringTable.intPair2Long(l[0], l[1]);
				long key2 = StringTable.intPair2Long(l[1], l[0]);
				if (appeared.contains(key1) || appeared.contains(key2)) {
					continue;
				}
				if (store.containsKey(key1)) {
					String[] w = store.get(key1);
					appeared.add(key1);
					appeared.add(key2);
					dw.write(w);
				}
				if (store.containsKey(key2)) {
					String[] w = store.get(key2);
					appeared.add(key1);
					appeared.add(key2);
					dw.write(w);
				}
			}
			dr.close();
		}
		dw.close();
	}

	/**Sample 10 of each 
	 * @throws IOException */
	public static void sampleForLabel(String file_extendedpair) throws IOException {
		String tempfile = file_extendedpair + ".shuffle";
		StringTable.shuffleLargeFile(file_extendedpair, Main.dir, tempfile);
		HashMap<Integer, String> wid2name = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] names = l[3].split(" ");
				if (names != null && names.length > 0) {
					wid2name.put(Integer.parseInt(l[2]), names[0]);
				}
			}
		}
		HashCount<String> hc = new HashCount<String>();
		List<String[]> towrite = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(tempfile);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);

				String look = l[2] + "=====>" + l[3];
				if (hc.see(look) < 10 && wid2name.containsKey(wid1) && wid2name.containsKey(wid2)) {
					String n1 = wid2name.get(wid1);
					String n2 = wid2name.get(wid2);
					towrite.add(new String[] { wid1 + "", wid2 + "", n1, n2, look });
					hc.add(look);
				}
			}
			dr.close();
		}
		{
			String writefile = file_extendedpair + ".forManualLabel";
			DelimitedWriter dw = new DelimitedWriter(writefile);
			StringTable.sortByColumn(towrite, new int[] { 4 });
			List<List<String[]>> blocks = StringTable.toblock(towrite, 4);
			for (List<String[]> b : blocks) {
				dw.write(b.get(0)[4]);
				for (String[] l : b) {
					dw.write("      ", l[0], l[1], l[2], l[3]);
				}
			}
			dw.close();
		}
	}

	static void getSeedPairs() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_seedwidpairs);
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] s : nr.seedInstances) {
				dw.write(s[0], s[1], nr.relation_name);
			}
		}
		dw.close();
	}

	public static void collectInstanceForGoldMapping() throws Exception {

		List<String[]> mapping = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_goldmapping);
			String[] l;
			while ((l = dr.read()) != null) {
				mapping.add(new String[] { l[0], l[1] });
			}
			dr.close();
		}
		{
			QueryFBGraph qfb = new QueryFBGraph();
			qfb.getNewEntitiesWithOntologyMapping(mapping, Main.file_sql2instance_gold);
			extendWithTypeConstrain(Main.file_goldmapping, Main.file_sql2instance_gold, Main.file_extendwidpairs_gold);
			filterByWikiLink(Main.file_extendwidpairs_gold, Main.file_extendwidpairs_gold_filter);
			sampleForLabel(Main.file_extendwidpairs_gold_filter);
		}
	}

	public static void main(String[] args) throws Exception {
		{
			/**Get seed pairs, easy*/
			getSeedPairs();
		}
		{
			/**collect instances for my predictions, slow*/
			extendWithTypeConstrain(Main.file_predict_vote_newontology, Main.file_sql2instance_shuffle,
					Main.file_extendedwidpairs);
			filterByWikiLink(Main.file_extendedwidpairs, Main.file_extendedwidpairs_filter);
			sampleForLabel(Main.file_extendedwidpairs_filter);
		}
		{
			/**Collect instances for gold, slow*/
			collectInstanceForGoldMapping();
		}
	}
}
