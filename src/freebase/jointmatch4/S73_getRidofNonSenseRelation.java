package freebase.jointmatch4;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S73_getRidofNonSenseRelation {

	private static void getRidOfNonSenseRelationClause_subset_wikipedapagelink(String output) throws IOException {
		HashSet<Integer> usedwid = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_patternvariable);
			String[] l;
			while ((l = dr.read()) != null) {
				usedwid.add(Integer.parseInt(l[0]));
				usedwid.add(Integer.parseInt(l[1]));
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			DelimitedWriter dw = new DelimitedWriter(output);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				if (usedwid.contains(wid1) && usedwid.contains(wid2)) {
					dw.write(l);
				}
			}
			dw.close();
		}
	}

	static void getRidOfNonSenseRelationWeight() throws IOException {
		//		HashMap<String, String> candidate = new HashMap<String, String>();
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_relationcandidate);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				String n = l[0];
		//				String m = l[1];
		//				candidate.put(m, getVariableRelation(n, m));
		//			}
		//		}
		String tempfile = Main.dir + "/wikipedia_link.temp_getRidOfNonSenseRelationClause";
		getRidOfNonSenseRelationClause_subset_wikipedapagelink(tempfile);
		HashMap<Long, String> map_samplepair2fbrel = new HashMap<Long, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_patternvariable);
			String[] l;
			while ((l = dr.read()) != null) {
				map_samplepair2fbrel.put(StringTable.intPair2Long(l[0], l[1]), l[5]);
			}
		}
		HashSet<Long> hasLinkBetween = new HashSet<Long>();
		{
			DelimitedReader dr = new DelimitedReader(tempfile);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				long key = StringTable.intPair2Long(wid1, wid2);
				long key_inv = StringTable.intPair2Long(wid2, wid1);
				if (map_samplepair2fbrel.containsKey(key)) {
					hasLinkBetween.add(key);
				}
				if (map_samplepair2fbrel.containsKey(key_inv)) {
					hasLinkBetween.add(key_inv);
				}
			}
		}
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_wikipediapageline);
			HashCount<String> fbrelall = new HashCount<String>();
			HashCount<String> fbrelhaslink = new HashCount<String>();
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_patternvariable);
			String[] l;
			while ((l = dr.read()) != null) {
				String rel = l[5];
				long key = StringTable.intPair2Long(l[0], l[1]);
				fbrelall.add(rel);
				if (hasLinkBetween.contains(key)) {
					fbrelhaslink.add(rel);
				}
			}
			Iterator<Entry<String, Integer>> it = fbrelall.iterator();
			while (it.hasNext()) {
				Entry<String, Integer> e = it.next();
				String fbrel = e.getKey();
				int all = e.getValue();
				int count = fbrelhaslink.see(fbrel);
				double weight = count * 1.0 / all;
				//String variable = candidate.get(fbrel);
				dw.write(fbrel, count, all);
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		getRidOfNonSenseRelationWeight();
	}
}
