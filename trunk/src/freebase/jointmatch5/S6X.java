package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S6X {

	// input: seedbfspathes_show_group
	// output:weight_similarity_nellrel_fbrel
	public static void step1_surfacestring_similarity() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_merge);
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_similarity_nellrel_fbrel);
			String[] l;
			while ((l = dr.read()) != null) {
				List<String> t1 = StringUtil.tokenize(l[0].replace("_inverse", ""), new char[] { '_' });
				List<String> t2 = StringUtil.tokenize(l[1], new char[] { ' ', '_', '/', '|', '@' });
				int share = StringUtil.numOfShareWords(t1, t2, new boolean[] { true, true, true });
				// double score = share * 1.0 / t1.size();
				dw.write(l[0], l[1], share, t1.size(), t2.size());
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void step2_share_instance() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_merge);
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_sharepair_nellrel_fbrel);
			String[] l;
			while ((l = dr.read()) != null) {
				dw.write(l[0], l[1], l[2]);
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static HashMap<String, String> nellstr2mid = new HashMap<String, String>();
	static HashMap<String, Integer> mid2wid = new HashMap<String, Integer>();
	static List<String> relationnames = new ArrayList<String>();

	public static void step3_negativepairs() throws Exception {
		negativepairs_part1();
		HashMap<String, Set<String>> seedevi = getSeedNegativeMap();
		HashMap<String, Set<String>> defaultevi = getDefaultNegativeMap();
		negativepairs_part2(seedevi, Main.file_weight_seednegativepair_nellrel_fbrel);
		//negativepairs_part2(defaultevi, Main.file_weight_defaultnegativepair_nellrel_fbrel);

	}

	public static void negativepairs_part1() throws Exception {
		{
			List<String[]> nellstrMidPred = (new DelimitedReader(
					Main.file_enid_mid_wid_argname_otherarg_relation_label_top1)).readAll();
			for (String[] a : nellstrMidPred) {
				nellstr2mid.put(a[3], a[1]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				String mid = l[1];
				mid2wid.put(mid, wid);
			}
			dr.close();
		}

		for (NellRelation nr : Main.no.nellRelationList) {
			relationnames.add(nr.relation_name);
		}
	}

	public static void negativepairs_part2(HashMap<String, Set<String>> evidence, String output) throws Exception {
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
		List<String[]> list_negative = new ArrayList<String[]>();
		{
			/** check all queryresult.name */
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_mergerel);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				String possible_nellrel = l[2].replace("_inverse", "");
				// String fbrel = l[4];
				String keytolook1 = wid1 + "\t" + wid2;
				String keytolook2 = wid2 + "\t" + wid1;
				Set<String> seednegrel1 = evidence.get(keytolook1);
				Set<String> seednegrel2 = evidence.get(keytolook2);
				if (seednegrel1 != null && seednegrel1.contains(possible_nellrel) || seednegrel2 != null
						&& seednegrel2.contains(possible_nellrel)) {
					list_negative.add(new String[] { l[0], l[1], l[2], l[3] });
				}
			}
			dr.close();
		}
		{
			StringTable.sortUniq(list_negative);
			for (String[] w : list_negative) {
				int wid1 = Integer.parseInt(w[0]);
				int wid2 = Integer.parseInt(w[1]);
				String type1 = map_notabletype.get(wid1);
				String type2 = map_notabletype.get(wid2);
				dw.write(wid1, wid2, type1, type2, w[2], w[3]);
			}
		}
		{
			//StringTable.sortByColumn(list_negative, new int[] { 2, 3, 0});
			//			HashCount<String> hcseed = new HashCount<String>();
			//			for (String[] a : list_negative) {
			//				String key = a[2] + "\t" + a[3];
			//				hcseed.add(key);
			//			}
			//			Iterator<Entry<String, Integer>> itseed = hcseed.iterator();
			//			while (itseed.hasNext()) {
			//				Entry<String, Integer> e = itseed.next();
			//				String[] ab = e.getKey().split("\t");
			//				dw.write(ab[0], ab[1], e.getValue());
			//			}
		}
		dw.close();
	}

	private static HashMap<String, Set<String>> getSeedNegativeMap() {
		HashMap<String, Set<String>> seed_negative = new HashMap<String, Set<String>>();
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] a : nr.known_negatives) {
				String mid1 = nellstr2mid.get(a[0]);
				String mid2 = nellstr2mid.get(a[1]);
				if (mid2wid.containsKey(mid1) && mid2wid.containsKey(mid2)) {
					int wid1 = mid2wid.get(mid1);
					int wid2 = mid2wid.get(mid2);
					String key = wid1 + "\t" + wid2;
					if (!seed_negative.containsKey(key)) {
						seed_negative.put(key, new HashSet<String>());
					}
					seed_negative.get(key).add(nr.relation_name);
				}
			}
		}
		return seed_negative;
	}

	private static HashMap<String, Set<String>> getDefaultNegativeMap() {
		HashMap<String, Set<String>> default_negative = new HashMap<String, Set<String>>();
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] a : nr.seedInstances) {
				try {
					String mid1 = nellstr2mid.get(a[0]);
					String mid2 = nellstr2mid.get(a[1]);
					int wid1 = mid2wid.get(mid1);
					int wid2 = mid2wid.get(mid2);

					String key = wid1 + "\t" + wid2;
					if (!default_negative.containsKey(key)) {
						default_negative.put(key, new HashSet<String>());
					}
					for (String r : relationnames) {
						if (!r.equals(nr.relation_name)) {
							default_negative.get(key).add(r);
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return default_negative;
	}

	//	public static void step4_getRidOfNonSenseRelationClause() throws IOException {
	//		String tempfile = Main.dir + "/wikipedia_link.temp_getRidOfNonSenseRelationClause";
	//		getRidOfNonSenseRelationClause_subset_wikipedapagelink(tempfile);
	//		HashMap<Long, String> map_samplepair2fbrel = new HashMap<Long, String>();
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_patternvariable);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				map_samplepair2fbrel.put(StringTable.intPair2Long(l[0], l[1]), l[5]);
	//			}
	//		}
	//		HashSet<Long> hasLinkBetween = new HashSet<Long>();
	//		{
	//			DelimitedReader dr = new DelimitedReader(tempfile);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				int wid1 = Integer.parseInt(l[0]);
	//				int wid2 = Integer.parseInt(l[1]);
	//				long key = StringTable.intPair2Long(wid1, wid2);
	//				long key_inv = StringTable.intPair2Long(wid2, wid1);
	//				if (map_samplepair2fbrel.containsKey(key)) {
	//					hasLinkBetween.add(key);
	//				}
	//				if (map_samplepair2fbrel.containsKey(key_inv)) {
	//					hasLinkBetween.add(key_inv);
	//				}
	//			}
	//		}
	//		{
	//			HashCount<String> fbrelall = new HashCount<String>();
	//			HashCount<String> fbrelhaslink = new HashCount<String>();
	//			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_patternvariable);
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_wikipediapageline);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				String rel = l[5];
	//				long key = StringTable.intPair2Long(l[0], l[1]);
	//				fbrelall.add(rel);
	//				if (hasLinkBetween.contains(key)) {
	//					fbrelhaslink.add(rel);
	//				}
	//			}
	//			Iterator<Entry<String, Integer>> it = fbrelall.iterator();
	//			while (it.hasNext()) {
	//				Entry<String, Integer> e = it.next();
	//				String fbrel = e.getKey();
	//				int all = e.getValue();
	//				int count = fbrelhaslink.see(fbrel);
	//				dw.write(fbrel, count, all);
	//			}
	//			dw.close();
	//		}
	//	}
	//
	//	private static void getRidOfNonSenseRelationClause_subset_wikipedapagelink(String output) throws IOException {
	//		HashSet<Integer> usedwid = new HashSet<Integer>();
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_patternvariable);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				usedwid.add(Integer.parseInt(l[0]));
	//				usedwid.add(Integer.parseInt(l[1]));
	//			}
	//		}
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
	//			DelimitedWriter dw = new DelimitedWriter(output);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				int wid1 = Integer.parseInt(l[0]);
	//				int wid2 = Integer.parseInt(l[1]);
	//				if (usedwid.contains(wid1) && usedwid.contains(wid2)) {
	//					dw.write(l);
	//				}
	//			}
	//			dw.close();
	//		}
	//	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		{
			/** surface string similarity */
			step1_surfacestring_similarity();

			/** number of instances p=<a1,a2> satisfying I(p, fr) & I(p, nr) */
			step2_share_instance();

			/**
			 * negative evidence: seed: negative seed instances; default: pair
			 * belonging to R\r relation is the default negative pair for relation r
			 * p=<a1,a2>, I know NI(p,nr) AND I(p,fr) --> \neg M(fr,nr)
			 */
			step3_negativepairs();

		}
	}

}
