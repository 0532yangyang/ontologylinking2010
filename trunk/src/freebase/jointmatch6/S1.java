package freebase.jointmatch6;

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
import javatools.mydb.StringTable;
import javatools.parsers.PlingStemmer;
import javatools.string.StringUtil;
import javatools.webapi.BingApi;
import javatools.webapi.FBSearchEngine;
import javatools.webapi.LuceneSearch;

public class S1 {

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

	public static void getraw2() throws IOException {
		HashMap<String, String[]> map_enurl2others = Tool.get_enurl2others();
		HashMap<String, String[]> map_mid2others = Tool.get_mid2others();
		HashMap<String, String> map_notabletype = Tool.get_notabletype();
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

					dw.write(name, mid, wid, type, namealias, s[2]);
				}
			}
			dw.close();
		}

	}

	public static void subsetWikiSenWikilink(String file_wksen_subset, String file_wklink_subset) throws IOException {
		HashSet<Integer> usedwid = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
			String[] l;
			while ((l = dr.read()) != null) {
				usedwid.add(Integer.parseInt(l[2]));
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_globalsentences + ".tokens");
			DelimitedWriter dw = new DelimitedWriter(file_wksen_subset + ".temp");
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[1]);
				if (usedwid.contains(wid)) {
					dw.write(l);
				}
			}
			dr.close();
			dw.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelinkraw);
			DelimitedWriter dw = new DelimitedWriter(file_wklink_subset);
			String[] l;
			while ((l = dr.read()) != null) {
				try {
					int wid = Integer.parseInt(l[0]);
					if (usedwid.contains(wid)) {
						dw.write(l);
					}
				} catch (Exception e) {

				}
			}
			dr.close();
			dw.close();
		}
		{
			Sort.sort(file_wksen_subset + ".temp", file_wksen_subset, Main.dir, new Comparator<String[]>() {
				@Override
				public int compare(String[] arg0, String[] arg1) {
					// TODO Auto-generated method stub
					int wid0 = Integer.parseInt(arg0[1]);
					int wid1 = Integer.parseInt(arg1[1]);
					return wid0 - wid1;
				}

			});
		}
	}

	public static void step1() throws IOException {
		HashMap<String, List<String>> argname2midlist = new HashMap<String, List<String>>();
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbsearch3);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
			String[] l;
			while ((l = dr.read()) != null) {
				if (argname2midlist.containsKey(l[0]))
					continue;
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
								pairs.add(new String[] { m2, m1,
										nr.relation_name + "_reverse" + "::" + a[1] + "::" + a[0] });
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
							pairs.add(new String[] { m2, a[0],
									nr.relation_name + "_reverse" + "::" + a[1] + "::" + a[0] });
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

	public static void step3(String file_fbsearchmatch, String output) throws IOException {
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
								String []wid1list = s1[1].split("::");
								String []wid2list = s2[1].split("::");
								for(String wid1:wid1list){
									for(String wid2:wid2list){
										dw.write(wid1, wid2, s1[3], s2[3], nr);		
									}
								}
							}
						}
					} catch (Exception e) {
						D.p(l,s1,s2);
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

	public static void main(String[] args) throws Exception {

		/** filter stanford wikipedia to get subset stanford */
		/** Use freebase engine to get raw nellstring 2 fb enurl */

		{
			getraw1();
			getraw2();
			step1();
			step2(Main.file_fbsearch3, Main.file_fbsearchmatch);
			step3(Main.file_fbsearchmatch, Main.file_fbsearchcandidate);
			step4(Main.file_fbsearchcandidate, Main.file_fbsql2instances);
		}
		//		subsetWikiSenWikilink(Main.file_wksensubset, Main.file_wklinksub);
	}

}
