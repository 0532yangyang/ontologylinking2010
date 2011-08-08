package freebase.jointmatch10;

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

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.StringTable;
import javatools.parsers.PlingStemmer;
import javatools.string.StringUtil;
import javatools.webapi.BingApi;
import javatools.webapi.FBSearchEngine;
import javatools.webapi.LuceneSearch;

public class S1_generatecandidate {

	/**
	 * @param args
	 */

	public static void getraw1() throws IOException {
		//LuceneSearch ls = new LuceneSearch(Main.dir_gnid_mid_wid_title_luceneindex);
		HashMap<String, List<String>> argument2types = new HashMap<String, List<String>>();
		//DelimitedWriter dw = new DelimitedWriter(file_ontologyentity2fbentity_byfbsearch);
		Set<String> list = new HashSet<String>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {

				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					list.add(a[0]);
					list.add(a[1]);
					if (!argument2types.containsKey(a[0])) {
						argument2types.put(a[0], new ArrayList<String>());
					}
					argument2types.get(a[0]).add(nr.relation_name + "_arg1" + "_" + a[1]);
					if (!argument2types.containsKey(a[1])) {
						argument2types.put(a[1], new ArrayList<String>());
					}
					argument2types.get(a[1]).add(nr.relation_name + "_arg2" + "_" + a[0]);
				}
				for (String[] a : nr.known_negatives) {
					list.add(a[0]);
					list.add(a[1]);
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(Main.dir + "/fbsearch.raw1");
		for (String a : list) {
			List<String> res = FBSearchEngine.query2(a, 10);
			for (String enid : res) {
				dw.write(a, enid);
			}
		}
		dw.close();
	}

	public static void getraw2(int topk) throws IOException {
		HashMap<String, String[]> map_enurl2others = Tool.get_enurl2others();
		HashMap<String, String[]> map_mid2others = Tool.get_mid2others();
		HashMap<String, String> map_notabletype = Tool.get_notabletype();
		HashCount<String> exists = new HashCount<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch1);
			DelimitedWriter dw = new DelimitedWriter(Main.file_fbsearch2);
			String[] l;
			while ((l = dr.read()) != null) {
				String name = l[0];
				String idstr = l[1];
				if (idstr.startsWith("/en/")) {
					String[] ab = map_enurl2others.get(idstr.replace("/en/", ""));
					if (ab != null && ab.length > 1) {
						idstr = ab[1];
					} else {
						continue;
					}
				} else if (idstr.startsWith("/m/")) {
				} else {
					continue;
				}
				if (idstr != null) {
					String mid = idstr;
					String[] s = map_mid2others.get(mid);
					String type = map_notabletype.get(mid);
					String wid = "";
					String namealias = "";
					if (type == null || s == null) {
						continue;
					}
					wid = s[3];
					namealias = s[4];
					if (exists.see(name) < topk) {
						exists.add(name);
						dw.write(name, mid, wid, type, namealias, s[2]);
					}
				}
			}
			dw.close();
		}

	}

//	public static void subsetWikiSenWikilink(String file_wksen_subset, String file_wklink_subset) throws IOException {
//		HashSet<Integer> usedwid = new HashSet<Integer>();
//		{
//			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
//			String[] l;
//			while ((l = dr.read()) != null) {
//				usedwid.add(Integer.parseInt(l[2]));
//			}
//			dr.close();
//		}
//		{
//			DelimitedReader dr = new DelimitedReader(Main.file_globalsentences + ".tokens");
//			DelimitedWriter dw = new DelimitedWriter(file_wksen_subset + ".temp");
//			String[] l;
//			while ((l = dr.read()) != null) {
//				int wid = Integer.parseInt(l[1]);
//				if (usedwid.contains(wid)) {
//					dw.write(l);
//				}
//			}
//			dr.close();
//			dw.close();
//		}
//		{
//			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelinkraw);
//			DelimitedWriter dw = new DelimitedWriter(file_wklink_subset);
//			String[] l;
//			while ((l = dr.read()) != null) {
//				try {
//					int wid = Integer.parseInt(l[0]);
//					if (usedwid.contains(wid)) {
//						dw.write(l);
//					}
//				} catch (Exception e) {
//
//				}
//			}
//			dr.close();
//			dw.close();
//		}
//		{
//			Sort.sort(file_wksen_subset + ".temp", file_wksen_subset, Main.dir, new Comparator<String[]>() {
//				@Override
//				public int compare(String[] arg0, String[] arg1) {
//					// TODO Auto-generated method stub
//					int wid0 = Integer.parseInt(arg0[1]);
//					int wid1 = Integer.parseInt(arg1[1]);
//					return wid0 - wid1;
//				}
//
//			});
//		}
//	}

	public static void step1() throws IOException {
		HashMap<String, List<String>> argname2midlist = new HashMap<String, List<String>>();
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbsearch3);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
			String[] l;
			while ((l = dr.read()) != null) {
				//				if (argname2midlist.containsKey(l[0]))
				//					continue;
				StringTable.mapKey2SetAdd(argname2midlist, l[0], l[1], true);
			}
			dr.close();
		}
		List<String[]> pairs = new ArrayList<String[]>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					List<String> list1 = argname2midlist.get(a[0]);
					List<String> list2 = argname2midlist.get(a[1]);
					if (list1 != null && list2 != null) {
						for (String m1 : list1) {
							for (String m2 : list2) {
								pairs.add(new String[] { m1, m2, nr.relation_name + "::" + a[0] + "::" + a[1] });
								//								pairs.add(new String[] { m2, m1,
								//										nr.relation_name + "_reverse" + "::" + a[1] + "::" + a[0] });
							}
						}
					}
					if (list1 != null) {
						for (String m1 : list1) {
							pairs.add(new String[] { m1, a[1], nr.relation_name + "::" + a[0] + "::" + a[1] });
						}
					}
					if (list2 != null) {
						for (String m2 : list2) {
							//							pairs.add(new String[] { m2, a[0],
							//									nr.relation_name + "_reverse" + "::" + a[1] + "::" + a[0] });
						}
					}
				}
			}
		}
		StringTable.sortByColumn(pairs, new int[] { 0 });
		for (String[] p : pairs) {
			dw.write(p);
		}
		dw.close();
	}

	public static void step2(String file_raw3, String output) throws IOException {
		List<String[]> pairs = (new DelimitedReader(file_raw3)).readAll();
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
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
						if (b0.equals(s0) && !b[0].equals(b[1])) {//good match~~~
							dw.write(b[2], s[2], b[0], b[1], s[0], s[4]);
						}
					}
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void searchPairsLen2() throws IOException {

	}

	public static void step3_old(String file_fbsearchmatch, String output) throws IOException {
		HashMap<String, String> mid2type = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_mid_wid_type_name_alias);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2type.put(l[0], l[2]);
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		List<String[]> table = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(file_fbsearchmatch);
			String[] l;

			while ((l = dr.read()) != null) {
				String ab[] = l[0].split("::");
				String rel = ab[0];
				String entitypair = ab[1] + "::" + ab[2];
				String fbrel = l[1];
				String type1 = mid2type.containsKey(l[2]) ? mid2type.get(l[2]) : "NA";
				String type2 = mid2type.containsKey(l[3]) ? mid2type.get(l[3]) : "NA";
				table.add(new String[] { rel, fbrel, l[2], l[3], type1, type2 });
			}
			dr.close();
		}
		StringTable.sortUniq(table);
		List<String[]> show = new ArrayList<String[]>();
		{
			HashCount<String> hc = new HashCount<String>();
			HashMap<String, Set<String>> arg1types = new HashMap<String, Set<String>>();
			HashMap<String, Set<String>> arg2types = new HashMap<String, Set<String>>();
			for (String[] t : table) {
				String key = t[0] + "::" + t[1];
				hc.add(key);
				StringTable.mapKey2SetAdd(arg1types, key, t[4]);
				StringTable.mapKey2SetAdd(arg2types, key, t[5]);
			}
			Iterator<Entry<String, Integer>> it = hc.iterator();

			while (it.hasNext()) {
				Entry<String, Integer> e = it.next();
				if (e.getValue() > 1) {
					String key = e.getKey();
					Set<String> arg1s = arg1types.get(key);
					Set<String> arg2s = arg2types.get(key);
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : arg1s)
						sb1.append(s + ",");
					for (String s : arg2s)
						sb2.append(s + ",");
					String[] ab = key.split("::");
					show.add(new String[] { e.getValue() + "", ab[0], ab[1], sb1.toString(), sb2.toString() });
				}
			}
		}
		{
			StringTable.sortByColumn(show, new int[] { 1, 0 }, new boolean[] { false, true });
			for (String[] a : show) {
				int size = Integer.parseInt(a[0]);
				if (size > 1 && !a[1].contains("_reverse")) {
					dw.write(a);
				}
			}
			dw.close();
		}
	}

	static void queryLen1() throws IOException {
		HashMap<String, Integer> relations = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_relationmatch_candidate);
			String[] l;
			while ((l = dr.read()) != null) {
				if (!l[2].contains("||")) {
					relations.put(l[2], Integer.parseInt(l[0]));
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbsql2instances_len1);
		{
			//j1  /m/01086v   /location/hud_foreclosure_area/hhuniv|/measurement_unit/dated_integer/source    /m/0jbk9    /m/07cwfx3   
			DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
			String[] l;
			while ((l = dr.read()) != null) {
				if (relations.containsKey(l[2])) {
					int rid = relations.get(l[2]);
					dw.write(l[1], l[3], rid, l[0]);
				}
			}
			dr.close();
		}
		dw.close();
	}

	static void queryLen2() throws Exception {
		HashMap<String, Integer> relations = new HashMap<String, Integer>();
		HashMap<String, Set<String>> r1_r2 = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_relationmatch_candidate);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[2].contains("||")) {
					String[] ab = l[2].split("\\|\\|");
					StringTable.mapKey2SetAdd(r1_r2, ab[0], ab[1]);
					relations.put(l[2], Integer.parseInt(l[0]));
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbsql2instances_len2);

		MergeReadStr mrs = new MergeReadStr(Main.file_fbvisible2, Main.file_fbvisible, 3, 1);
		MergeReadResStr mrrs;
		while ((mrrs = mrs.read()) != null) {
			for (String[] s1 : mrrs.line1_list) {
				if (r1_r2.containsKey(s1[2])) {
					Set<String> temp = r1_r2.get(s1[2]);
					for (String[] s2 : mrrs.line2_list) {
						if (temp.contains(s2[2])) {
							String r = s1[2] + "||" + s2[2];
							int rid = relations.get(r);
							dw.write(s1[1], s2[1], s2[3], rid);
						}
					}
				}
			}
			//D.p(mrrs.line1_list);
		}
		dw.close();
		//		{
		//			//j1  /m/01086v   /location/hud_foreclosure_area/hhuniv|/measurement_unit/dated_integer/source    /m/0jbk9    /m/07cwfx3   
		//			DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
		//			String []l;
		//			while((l = dr.read())!=null){
		//				if(relations.containsKey(l[2])){
		//					int rid = relations.get(l[2]);
		//					dw.write(l[1],l[3],rid,l[0]);
		//				}
		//			}
		//			dr.close();
		//		}
	}

	//add type constrain???
	static void step4(String file_candidate, String output) throws IOException {
		//fbrelation::type1::type2 --> nell relation
		HashMap<String, Set<String>> fb2nell = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(file_candidate);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellrel = l[1];
				String fbrel = l[2];
				String[] type1 = l[3].split(",");
				String[] type2 = l[4].split(",");
				for (String t1 : type1) {
					for (String t2 : type2) {
						String key = fbrel + "::" + t1 + "::" + t2;
						StringTable.mapKey2SetAdd(fb2nell, key, nellrel);
					}
				}
			}
			dr.close();
		}
		HashMap<String, String[]> mid2others = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_mid_wid_type_name_alias);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2others.put(l[0], l);
			}
		}
		{
			//j1  /m/01086v   /location/hud_foreclosure_area/hhuniv|/measurement_unit/dated_integer/source    /m/0jbk9    /m/07cwfx3    
			DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
			DelimitedWriter dw = new DelimitedWriter(output);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid1 = l[1];
				String mid2 = l[3];
				String rel = l[2];
				if (mid2others.containsKey(mid1) && mid2others.containsKey(mid2)) {
					String[] s1 = mid2others.get(mid1);
					String[] s2 = mid2others.get(mid2);
					try {
						String type1 = s1[2];
						String type2 = s2[2];
						//int wid1 = Integer.parseInt(s1[1]);
						//int wid2 = Integer.parseInt(s2[1]);
						String key = rel + "::" + type1 + "::" + type2;
						if (fb2nell.containsKey(key)) {
							Set<String> nellrel = fb2nell.get(key);
							for (String nr : nellrel) {
								String[] wid1list = s1[1].split("::");
								String[] wid2list = s2[1].split("::");
								for (String wid1 : wid1list) {
									for (String wid2 : wid2list) {
										dw.write(wid1, wid2, s1[3], s2[3], nr);
									}
								}
							}
						}
					} catch (Exception e) {
						D.p(l, s1, s2);
					}
				}

			}
			dr.close();
			dw.close();
		}
	}

	//	/**Get tuples by relations*/
	//	public static void step5(String file_candidate) throws IOException {
	//		HashMap<String, List<String>> candidate = new HashMap<String, List<String>>();
	//		{
	//			DelimitedReader dr = new DelimitedReader(file_candidate);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				if (!candidate.containsKey(l[2])) {
	//					candidate.put(l[2], l[1]);
	//				} else {
	//					System.err.println("One FB relation maps to two Nell Relation! Give up:\t" + l[1] + "\t" + l[2]);
	//				}
	//			}
	//			dr.close();
	//		}
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
	//
	//			dr.close();
	//		}
	//	}

	public static void len2step0(String file_raw3, String file_raw4) throws IOException {
		List<String[]> pairs = (new DelimitedReader(file_raw3)).readAll();
		DelimitedWriter dw = new DelimitedWriter(file_raw4 + ".temp");
		DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
		List<String[]> ss = dr.readBlock(1);
		List<List<String[]>> blocks = StringTable.toblock(pairs, 0);
		for (List<String[]> block : blocks) {
			String mid = block.get(0)[0];
			while (ss != null && ss.size() > 0 && ss.get(0)[1].compareTo(mid) < 0) {
				ss = dr.readBlock(1);
			}

			if (ss != null && ss.size() > 0 && ss.get(0)[1].equals(mid)) {
				for (String[] b : block) {
					//b: /m/0108kc, /m/04jpl, newspaperInCity::Financial Times::London OR /m/0108kc, London, newspaperInCity::Financial Times::London
					//ss: s1, /m/0108kc, /book/periodical/first_issue_date|/book/periodical_publication_date/date, 1888, /m/02npnnk OR
					//j1, /m/0108kc, /book/periodical/format|/book/periodical_format_period/format, /m/0j4sj, /m/02npbhd
					for (String[] s : ss) {
						if (s[3].startsWith("/m/")) {
							if (b[0].equals(s[3]) || b[0].equals(b[1]) || s[3].equals(b[1]))
								continue;
							dw.write(b[0], s[3], b[1], b[2], s[2]);
						}
					}
				}
			}
		}
		dr.close();
		dw.close();
		Sort.sort(file_raw4 + ".temp", file_raw4, Main.dir, new Comparator<String[]>() {

			@Override
			public int compare(String[] arg0, String[] arg1) {
				// TODO Auto-generated method stub
				return arg0[1].compareTo(arg1[1]);
			}

		});
	}

	public static void len2step1(String file_raw4, String output) throws IOException {
		DelimitedReader drraw4 = new DelimitedReader(file_raw4);
		//List<String[]> pairs = (new DelimitedReader(file_raw4)).readAll();
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(Main.file_fbvisible);
		List<String[]> ss = dr.readBlocklimited(1, 1000);
		List<String[]> block;
		int count = 0;
		while ((block = drraw4.readBlocklimited(1, 1000)) != null) {

			String mid = block.get(0)[1];
			while (ss != null && ss.size() > 0 && ss.get(0)[1].compareTo(mid) < 0) {
				ss = dr.readBlocklimited(1, 1000);
			}
			if (ss != null && ss.size() > 0 && ss.get(0)[1].equals(mid)) {
				for (String[] b : block) {

					if (b[3].contains("reverse"))
						continue;
					Set<String> temp = new HashSet<String>();
					for (String[] s : ss) {

						if (b[2].equals(s[3]) && !b[0].equals(b[1]) && !b[1].equals(b[2])) {//good match~~~
							//s:j0, /m/010005, /location/location/containedby, /m/09c7w0
							//b: /m/07b_l, /m/010005, /m/09c7w0, stateLocatedInCountry::Texas::United States, /location/location/contains
							String key = b[4] + b[2] + b[0] + b[1] + b[2];
							if (!temp.contains(key)) {
								dw.write(b[3], b[4] + "||" + s[2], b[0], b[1], b[2]);
								temp.add(key);
								count++;
							}

							//dw.write(b[2], s[2], b[0], b[1], s[0], s[4]);
						}
					}
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void len2step2(String file_match, String file_candidate) throws Exception {
		//StateLocatedInCountry::Ontario::Canada	/location/location/contains	/location/location/containedby	/m/05kr_	/m/03m9z91	/m/0d060g
		DelimitedReader dr = new DelimitedReader(file_match);
		DelimitedWriter dw = new DelimitedWriter(file_candidate);
		List<String[]> table = new ArrayList<String[]>();

		String[] l;
		//stateLocatedInCountry::Texas::United States, /location/location/contains, /location/location/containedby, /m/07b_l, /m/010005, /m/09c7w0
		while ((l = dr.read()) != null) {
			String[] abc = l[0].split("::");
			String nellrel = abc[0];
			String joinedrel = l[1] + "::" + l[2];
			table.add(new String[] { nellrel, joinedrel, abc[1], abc[2], l[4] });

		}
		StringTable.sortUniq(table);
		String[] last = table.get(0);
		List<String> middles = new ArrayList<String>();
		middles.add(last[4]);
		for (int i = 1; i < table.size(); i++) {
			String[] t = table.get(i);
			if (0 == StringTable.compare(t, last, 0, 4)) {
				middles.add(t[4]);
			} else {

				dw.write(last[0], last[1], last[2], last[3], middles);
				last = t;
				middles.clear();
				middles.add(last[4]);
			}
		}
		dw.write(last[0], last[1], last[2], last[3], middles);
		dw.close();
	}

	public static void step3_get_relation_candidate(String file_match_len1, String file_match_len2, String output)
			throws Exception {
		DelimitedWriter dw = new DelimitedWriter(output);
		HashMap<String, Set<String>> map_pair2instance = new HashMap<String, Set<String>>();
		//lenth 1 & length 2
		{
			step3_get_relation_candidate_help(map_pair2instance, file_match_len1);
			step3_get_relation_candidate_help(map_pair2instance, file_match_len2);
		}
		//print
		{
			List<String[]> table = new ArrayList<String[]>();
			for (Entry<String, Set<String>> e : map_pair2instance.entrySet()) {
				String pair = e.getKey();
				Set<String> entities = e.getValue();
				String[] abc = pair.split("::");
				if (entities.size() > 1) {
					table.add(new String[] { abc[0], abc[1], entities.size() + "" });
				}
			}
			StringTable.sortByColumn(table, new int[] { 0, 2 }, new boolean[] { false, true });
			for (String t[] : table) {
				dw.write(varid, t[0], t[1], t[2]);
				varid++;
			}
		}
		dw.close();
	}

	public static void step3_get_relation_candidate_help(HashMap<String, Set<String>> map_pair2instance, String file)
			throws Exception {
		{
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] abc = l[0].split("::");
				if (abc[0].contains("reverse"))
					continue;
				StringTable.mapKey2SetAdd(map_pair2instance, abc[0] + "::" + l[1], abc[1] + "::" + abc[2]);
			}
			dr.close();
		}
	}

	public static void step3_get_entity_candidate(String file_raw2, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(file_raw2);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			dw.write(varid, l[0], l[1]);
			varid++;
		}
		dw.close();
	}

	public static void step3_get_type_candidate(String file_raw2, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(file_raw2);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		HashMap<String, Set<String>> pair_evidence = new HashMap<String, Set<String>>();
		while ((l = dr.read()) != null) {
			String entityname = l[0];
			Set<String> entityclass = Main.no.entity2class.get(entityname);
			if (entityclass.contains("writer")) {
				D.p(entityname, l[3]);
			}
			entityclass.remove("NEG_NA");
			for (String nelltype : entityclass) {
				String fbtype = l[3];
				String evidence = l[0];
				StringTable.mapKey2SetAdd(pair_evidence, nelltype + "::" + fbtype, evidence);
			}
		}
		{
			List<String[]> table = new ArrayList<String[]>();
			for (Entry<String, Set<String>> e : pair_evidence.entrySet()) {
				String pair = e.getKey();
				Set<String> entities = e.getValue();
				String[] abc = pair.split("::");
				if (entities.size() > 1) {
					table.add(new String[] { abc[0], abc[1], entities.size() + "" });
				}
			}
			StringTable.sortByColumn(table, new int[] { 0, 2 }, new boolean[] { false, true });
			for (String t[] : table) {
				dw.write(varid, t[0], t[1], t[2]);
				varid++;
			}
		}

		dw.close();
	}
	static void knownNegativeMatch() throws IOException {
		HashMap<String, Set<String>> nellname_candidates = new HashMap<String, Set<String>>();
		{
			/**load entity candidate*/
			DelimitedReader dr = new DelimitedReader(Main.file_entitymatch_candidate);
			String[] l;
			while ((l = dr.read()) != null) {
				StringTable.mapKey2SetAdd(nellname_candidates, l[1], l[2]);
			}
			dr.close();
		}
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_knownnegative + ".1");
			List<String[]> table = new ArrayList<String[]>();
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] u : nr.known_negatives) {
					Set<String> c1 = nellname_candidates.get(u[0]);
					Set<String> c2 = nellname_candidates.get(u[1]);
					if (c1 == null)
						c1 = new HashSet<String>();
					if (c2 == null)
						c2 = new HashSet<String>();
					c1.add(u[0]);
					c2.add(u[1]);
					for (String a : c1) {
						for (String b : c2) {
							if (a.startsWith("/m/")) {
								table.add(new String[] { a, b, nr.relation_name + "::" + u[0] + "::" + u[1] });
							}
						}
					}
				}
			}
			StringTable.sortByColumn(table, new int[] { 0 });
			for(String []t:table){
				dw.write(t);
			}
			dw.close();
		}
		step2(Main.file_knownnegative + ".1", Main.file_knownnegative + ".match.len1");
		len2step0(Main.file_knownnegative + ".1", Main.file_knownnegative + ".2");
		len2step1(Main.file_knownnegative + ".2", Main.file_knownnegative + ".match.len2");
	}

	public static void main2(String[] args) throws Exception {
		knownNegativeMatch();
	}

	static int varid = 1;

	public static void main(String[] args) throws Exception {

		{
//			/**Time cost from getraw1 to step4 is half hour*/
			getraw1();
			getraw2(3);
			/**step1 is to get A,B pair to match in good format for matching see file_fbsearch3*/
			step1();
			/**step2 get length1 match*/
			step2(Main.file_fbsearch3, Main.file_fbsearchmatch_len1);
			/**extend A,B in file_fbsearch3 into all potential A,C,B*/
			len2step0(Main.file_fbsearch3, Main.file_fbsearch4);
			len2step1(Main.file_fbsearch4, Main.file_fbsearchmatch_len2);

			/**get candidate*/
			step3_get_relation_candidate(Main.file_fbsearchmatch_len1, Main.file_fbsearchmatch_len2,
					Main.file_relationmatch_candidate);
			step3_get_entity_candidate(Main.file_fbsearch2, Main.file_entitymatch_candidate);
			step3_get_type_candidate(Main.file_fbsearch2, Main.file_typematch_candidate);
			/**match known negative seeds*/
			knownNegativeMatch();
//			/**from candidate to instances*/
			QueryFBGraph qfb = new QueryFBGraph();
			qfb.getNewEntitiesWithOntologyMapping(Main.file_relationmatch_candidate, Main.file_fbsql2instances);
		}
	}

}
