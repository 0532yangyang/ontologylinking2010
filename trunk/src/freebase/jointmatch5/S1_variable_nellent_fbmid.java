package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.parsers.PlingStemmer;
import javatools.string.StringUtil;
import javatools.webapi.BingApi;
import javatools.webapi.FBSearchEngine;
import javatools.webapi.LuceneSearch;

public class S1_variable_nellent_fbmid {

	/**
	 * @param args
	 */
	static HashMap<String, List<String>> store = new HashMap<String, List<String>>();

	private static String[] doitraw(String arg1, String arg2) {
		List<String> ares, bres;
		if (store.containsKey(arg1)) {
			ares = store.get(arg1);
		} else {
			ares = FBSearchEngine.query2(arg1, 10);
			store.put(arg1, ares);
		}
		if (store.containsKey(arg2)) {
			bres = store.get(arg2);
		} else {
			bres = FBSearchEngine.query2(arg2, 10);
			store.put(arg2, bres);
		}
		StringBuilder sba = new StringBuilder();
		StringBuilder sbb = new StringBuilder();
		for (String t1 : ares) {
			sba.append(t1 + ";");
		}
		for (String t2 : bres) {
			sbb.append(t2 + ";");
		}
		return new String[] { sba.toString(), sbb.toString() };
	}

	private static boolean doit(HashMap<String, List<String[]>> map_enid2others, DelimitedWriter dw, String a,
			String arg1, String arg2, String relation, String label, String arg1OrArg2) throws IOException {
		if (!a.startsWith("/en")) {
			return false;
		}
		a = a.replace("/en/", "");
		List<String[]> enid2otherlines = map_enid2others.get(a);
		/**926     /m/01003_       135776  ??? Krum Krum,_Texas Denton_County_/_Krum_city*/
		if (enid2otherlines == null || enid2otherlines.size() == 0) {
			return false;
		}
		String mid = enid2otherlines.get(0)[1];
		List<Integer> list_artid = new ArrayList<Integer>();
		for (String[] l : enid2otherlines) {
			list_artid.add(Integer.parseInt(l[2]));
		}
		String enid = a;
		String argname = arg1;
		String otherarg = arg2;
		for (int artid : list_artid) {
			dw.write(enid, mid, artid, argname, otherarg, relation, label, arg1OrArg2);
		}
		return true;
	}

	public static void getraw(String file_ontologyentity2fbentity_byfbsearch) {
		try {
			DelimitedWriter dw = new DelimitedWriter(file_ontologyentity2fbentity_byfbsearch);

			for (NellRelation nr : Main.no.nellRelationList) {
				try {
					if (nr.seedInstances == null || nr.seedInstances.size() == 0)
						continue;
					for (String[] a : nr.seedInstances) {
						String[] res = doitraw(a[0], a[1]);
						dw.write("+1", a[0], a[1], nr.relation_name, res[0], res[1]);
					}
					for (String[] a : nr.known_negatives) {
						String[] res = doitraw(a[0], a[1]);
						dw.write("-1", a[0], a[1], nr.relation_name, res[0], res[1]);
					}
				} catch (Exception e) {
					System.err.println(nr.relation_name);
				}

			}
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getRawLucene(String file_ontologyentity2fbentity_bylucene) throws IOException {
		LuceneSearch ls = new LuceneSearch(Main.dir_gnid_mid_wid_title_luceneindex);
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
					String pairquery = a[0] + " " + a[1];
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
		HashMap<Integer, Integer> indegree = new HashMap<Integer, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink_indegree);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[0]);
				int count = Integer.parseInt(l[1]);
				indegree.put(wid, count);
			}
		}
		HashMap<Integer, String> notabletype = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				notabletype.put(Integer.parseInt(l[1]), l[2]);
			}
		}
		DelimitedWriter dw = new DelimitedWriter(file_ontologyentity2fbentity_bylucene);
		for (String a : list) {
			List<String[]> res = ls.search(a, 1000);
			List<String[]> tosort = new ArrayList<String[]>();
			for (String[] l : res) {
				String[] ab = l[0].split("\t");
				String mid = ab[0];
				int wid = Integer.parseInt(ab[1]);
				String names = l[1];
				int sharewords = StringUtil.numOfShareWords(a.toLowerCase(), names.toLowerCase());
				int in = 0;
				if (indegree.containsKey(wid)) {
					in = indegree.get(wid);
				}
				String type = null;
				if (notabletype.containsKey(wid)) {
					type = notabletype.get(wid);
				}
				if (type == null)
					continue;
				tosort.add(new String[] { mid, wid + "", l[1], sharewords + "", in + "", type });
			}
			StringTable.sortByColumn(tosort, new int[] { 3, 4 }, new boolean[] { true, true });
			if (tosort.size() == 0) {
				continue;
			}
			int topWordMatch = Integer.parseInt(tosort.get(0)[3]);
			HashSet<String> appearedType = new HashSet<String>();
			int canddiateNum = 0;
			for (int i = 0; i < tosort.size(); i++) {
				String[] l = tosort.get(i);
				String type = l[5];
				if (appearedType.contains(type)) {
					continue;
				}
				int wordmatch = Integer.parseInt(l[3]);
				String[] w = new String[l.length + 2];
				w[0] = a;
				List<String> appears = argument2types.get(a);
				for (int k = 0; k < l.length; k++)
					w[k + 1] = l[k];
				if (appears != null) {
					w[w.length - 1] = appears.toString();
				} else {
					w[w.length - 1] = "null";
				}
				dw.write(w);
				canddiateNum++;
				appearedType.add(type);
				if (topWordMatch > 1 && canddiateNum > 5)
					break;
				if (topWordMatch == 1 && canddiateNum > 10)
					break;
			}
			//D.p(tosort);
		}
		dw.close();
	}

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
					dw.write(name, mid, wid, type, namealias);
				}
			}
			dw.close();
		}

	}

	public static void getraw3(int topk, String output) throws IOException {
		HashMap<String, List<String[]>> map_name2others = new HashMap<String, List<String[]>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
			String[] l;
			while ((l = dr.read()) != null) {
				if (!map_name2others.containsKey(l[0])) {
					map_name2others.put(l[0], new ArrayList<String[]>());
				}
				if (map_name2others.get(l[0]).size() >= topk) {
					continue;
				}
				map_name2others.get(l[0]).add(l);

			}
			dr.close();
		}
		{
			//alabama	/m/0gyh	303	Alabama	Montgomery	stateHasCapital	+1	arg1
			DelimitedWriter dw = new DelimitedWriter(output);

			for (NellRelation nr : Main.no.nellRelationList) {

				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					{
						//fix a[1]:
						//Volkswagen	/m/07_zt	32652	/organization/organization	Volkswagen_Group Volkswagen_AG 
						List<String[]> res0 = map_name2others.get(a[0]);
						List<String[]> res1 = map_name2others.get(a[1]);
						if (res0 == null || res1 == null) {
							D.p("missing pos", nr.relation_name, a[0], a[1]);
							continue;
						}
						for (String[] r : res0) {
							dw.write("", r[1], r[2], a[0], a[1], nr.relation_name, "+1", "arg1");
						}
						for (String[] r : res1) {
							dw.write("", r[1], r[2], a[1], a[0], nr.relation_name, "+1", "arg2");
						}
					}
				}
				for (String[] a : nr.known_negatives) {
					List<String[]> res0 = map_name2others.get(a[0]);
					List<String[]> res1 = map_name2others.get(a[1]);
					if (res0 == null || res1 == null) {
						D.p("missing neg", nr.relation_name, a[0], a[1]);
						continue;
					}
					for (String[] r : res0) {
						dw.write("", r[1], r[2], a[0], a[1], nr.relation_name, "-1", "arg1");
					}
					for (String[] r : res1) {
						dw.write("", r[1], r[2], a[1], a[0], nr.relation_name, "-1", "arg2");
					}
				}
			}
			dw.close();
		}
	}

	private static void getClean(String input_gnid_mid_enurl_wid_title,
			String input_ontologyentity2fbentity_byfbsearch, String output_enid_mid_wid_argname_otherarg_relation_label)
			throws IOException {
		HashMap<String, List<String[]>> map_enid2others = new HashMap<String, List<String[]>>();
		{
			/**926     /m/01003_       135776  ??? Krum Krum,_Texas Denton_County_/_Krum_city*/
			DelimitedReader dr = new DelimitedReader(input_gnid_mid_enurl_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String enid = l[3];
				if (!map_enid2others.containsKey(enid)) {
					map_enid2others.put(enid, new ArrayList<String[]>());
				}
				map_enid2others.get(enid).add(l);
			}
		}
		{
			DelimitedWriter dw = new DelimitedWriter(output_enid_mid_wid_argname_otherarg_relation_label + ".temp");
			DelimitedReader dr = new DelimitedReader(input_ontologyentity2fbentity_byfbsearch);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l.length < 5)
					continue;
				String label = l[0];
				String arg1 = l[1];
				//				if (arg1.equals("Harvey Araton")) {
				//					D.p(arg1);
				//				}
				String arg2 = l[2];
				String relation = l[3];
				String[] arg1enid = l[4].split(";");
				String[] arg2enid = l[5].split(";");
				for (String a1 : arg1enid) {
					doit(map_enid2others, dw, a1, arg1, arg2, relation, label, "arg1");
				}
				for (String a2 : arg2enid) {
					doit(map_enid2others, dw, a2, arg2, arg1, relation, label, "arg2");
				}
				// D.p(arg1enid.length,arg2enid.length);
				// break;
			}
			dr.close();
			dw.close();
		}
		{
			Sort.sort(output_enid_mid_wid_argname_otherarg_relation_label + ".temp",
					output_enid_mid_wid_argname_otherarg_relation_label, Main.dir, new Comparator<String[]>() {

						@Override
						public int compare(String[] o1, String[] o2) {
							// TODO Auto-generated method stub
							int widindex = 2;
							// look at function doit() dw.write(....);
							int wid1 = Integer.parseInt(o1[widindex]);
							int wid2 = Integer.parseInt(o2[widindex]);
							return wid1 - wid2;
						}

					});
		}
	}

	private static void getCleanLucene_notworking(String file_ontologyentity2fbentity_bylucene,
			String output_enid_mid_wid_argname_otherarg_relation_label) throws IOException {
		/**output: alabama(enid)	/m/0gyh	303	Alabama	Montgomery	stateHasCapital	+1	arg1*/
		DelimitedWriter dw = new DelimitedWriter(output_enid_mid_wid_argname_otherarg_relation_label);
		HashMap<String, List<String[]>> map_nellstr_lines = new HashMap<String, List<String[]>>();
		{
			List<String[]> all = (new DelimitedReader(file_ontologyentity2fbentity_bylucene)).readAll();
			for (String[] l : all) {
				String nellstr = l[0];
				if (!map_nellstr_lines.containsKey(nellstr)) {
					map_nellstr_lines.put(nellstr, new ArrayList<String[]>());
				}
				map_nellstr_lines.get(nellstr).add(l);
			}
		}
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				try {
					if (nr.seedInstances == null || nr.seedInstances.size() == 0)
						continue;
					for (String[] a : nr.seedInstances) {
						String seed1 = a[0];
						String seed2 = a[1];
						List<String[]> res1 = map_nellstr_lines.get(seed1);
						List<String[]> res2 = map_nellstr_lines.get(seed2);
						if (res1 != null && res2 != null) {
							for (String[] l1 : res1) {
								for (String[] l2 : res2) {
									dw.write(nr.relation_name, "+1", l1[0], l2[0], l1[1], l2[1], l1[2], l2[2], l1[6],
											l2[6]);
								}
							}
						}
					}
					for (String[] a : nr.known_negatives) {
						String seed1 = a[0];
						String seed2 = a[1];
						List<String[]> res1 = map_nellstr_lines.get(seed1);
						List<String[]> res2 = map_nellstr_lines.get(seed2);
						if (res1 != null && res2 != null) {
							for (String[] l1 : res1) {
								for (String[] l2 : res2) {
									dw.write(nr.relation_name, "-1", l1[0], l2[0], l1[1], l2[1], l1[2], l2[2], l1[6],
											l2[6]);
								}
							}
						}
					}
				} catch (Exception e) {
					System.err.println(nr.relation_name);
				}

			}
		}
		dw.close();
	}

	//	private static void getCleanLucene() throws IOException {
	//		/**output: alabama(enid)	/m/0gyh	303	Alabama	Montgomery	stateHasCapital	+1	arg1*/
	//		DelimitedWriter dw = new DelimitedWriter(Main.file_seedluceneclean);
	//		DelimitedWriter dw_enid = new DelimitedWriter(Main.file_enid_mid_wid_argname_otherarg_relation_label);
	//		DelimitedWriter dw_top1 = new DelimitedWriter(Main.file_enid_mid_wid_argname_otherarg_relation_label_top1);
	//		HashMap<String, List<String[]>> map_nellstr_lines = new HashMap<String, List<String[]>>();
	//		{
	//			List<String[]> all = (new DelimitedReader(Main.file_ontologyentity2fbentity_bylucene)).readAll();
	//			for (String[] l : all) {
	//				String nellstr = l[0];
	//				if (!map_nellstr_lines.containsKey(nellstr)) {
	//					map_nellstr_lines.put(nellstr, new ArrayList<String[]>());
	//				}
	//				map_nellstr_lines.get(nellstr).add(l);
	//			}
	//		}
	//		{
	//			for (NellRelation nr : Main.no.nellRelationList) {
	//
	//				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
	//					continue;
	//				for (String[] a : nr.seedInstances) {
	//					String seed1 = a[0];
	//					if (nr.relation_name.equals("radioStationInCity")) {
	//						//D.p("a");
	//					}
	//					String seed2 = a[1];
	//					List<String[]> res1 = map_nellstr_lines.get(seed1);
	//					List<String[]> res2 = map_nellstr_lines.get(seed2);
	//					//driver	/m/01ynp5	345491	Phyllis Diller Phyllis Ada Driver 	1	181	/tv/tv_actor
	//					if (res1 != null && res2 != null) {
	//						for (String[] l1 : res1) {
	//							for (String[] l2 : res2) {
	//								dw.write(nr.relation_name, "+1", l1[0], l2[0], l1[1], l2[1], l1[2], l2[2], l1[6], l2[6]);
	//							}
	//						}
	//					} else {
	//						continue;
	//					}
	//					for (int i = 0; i < res1.size(); i++) {
	//						String[] l1 = res1.get(i);
	//						dw_enid.write("", l1[1], l1[2], seed1, seed2, nr.relation_name, "+1", "arg1");
	//						if (i == 0) {
	//							dw_top1.write("", l1[1], l1[2], seed1, seed2, nr.relation_name, "+1", "arg1");
	//						}
	//					}
	//					for (int i = 0; i < res2.size(); i++) {
	//						String[] l2 = res2.get(i);
	//						dw_enid.write("", l2[1], l2[2], seed2, seed1, nr.relation_name, "+1", "arg2");
	//						if (i == 0) {
	//							dw_top1.write("", l2[1], l2[2], seed2, seed1, nr.relation_name, "+1", "arg2");
	//						}
	//					}
	//
	//				}
	//				for (String[] a : nr.known_negatives) {
	//					String seed1 = a[0];
	//					String seed2 = a[1];
	//					List<String[]> res1 = map_nellstr_lines.get(seed1);
	//					List<String[]> res2 = map_nellstr_lines.get(seed2);
	//					//driver	/m/01ynp5	345491	Phyllis Diller Phyllis Ada Driver 	1	181	/tv/tv_actor
	//					if (res1 != null && res2 != null) {
	//						for (String[] l1 : res1) {
	//							for (String[] l2 : res2) {
	//								dw.write(nr.relation_name, "+1", l1[0], l2[0], l1[1], l2[1], l1[2], l2[2], l1[6], l2[6]);
	//							}
	//						}
	//					} else {
	//						continue;
	//					}
	//					for (int i = 0; i < res1.size(); i++) {
	//						String[] l1 = res1.get(i);
	//						dw_enid.write("", l1[1], l1[2], seed1, seed2, nr.relation_name, "-1", "arg1");
	//						if (i == 0) {
	//							dw_top1.write("", l1[1], l1[2], seed1, seed2, nr.relation_name, "-1", "arg1");
	//						}
	//					}
	//					for (int i = 0; i < res2.size(); i++) {
	//						String[] l2 = res2.get(i);
	//						dw_enid.write("", l2[1], l2[2], seed2, seed1, nr.relation_name, "-1", "arg2");
	//						if (i == 0) {
	//							dw_top1.write("", l2[1], l2[2], seed2, seed1, nr.relation_name, "-1", "arg2");
	//						}
	//					}
	//
	//				}
	//
	//			}
	//		}
	//		dw_enid.close();
	//		dw.close();
	//		dw_top1.close();
	//	}

	private static void getCleanTop1(String input_gnid_mid_enurl_wid_title,
			String input_ontologyentity2fbentity_byfbsearch,
			String output_enid_mid_wid_argname_otherarg_relation_label_top1) throws IOException {
		HashMap<String, List<String[]>> map_enid2others = new HashMap<String, List<String[]>>();
		{
			/**926     /m/01003_       135776  ??? Krum Krum,_Texas Denton_County_/_Krum_city*/
			DelimitedReader dr = new DelimitedReader(input_gnid_mid_enurl_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String enid = l[3];
				if (!map_enid2others.containsKey(enid)) {
					map_enid2others.put(enid, new ArrayList<String[]>());
				}
				map_enid2others.get(enid).add(l);
			}
		}
		{
			DelimitedWriter dw = new DelimitedWriter(output_enid_mid_wid_argname_otherarg_relation_label_top1 + ".temp");
			DelimitedReader dr = new DelimitedReader(input_ontologyentity2fbentity_byfbsearch);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l.length < 5)
					continue;
				String label = l[0];
				String arg1 = l[1];
				//				if (arg1.equals("Harvey Araton")) {
				//					D.p(arg1);
				//				}
				String arg2 = l[2];
				String relation = l[3];
				String[] arg1enid = l[4].split(";");
				String[] arg2enid = l[5].split(";");
				for (String a1 : arg1enid) {
					if (doit(map_enid2others, dw, a1, arg1, arg2, relation, label, "arg1")) {
						break;
					}
				}
				for (String a2 : arg2enid) {
					if (doit(map_enid2others, dw, a2, arg2, arg1, relation, label, "arg2")) {
						break;
					}
				}
				// D.p(arg1enid.length,arg2enid.length);
				// break;
			}
			dr.close();
			dw.close();
		}
		{
			Sort.sort(output_enid_mid_wid_argname_otherarg_relation_label_top1 + ".temp",
					output_enid_mid_wid_argname_otherarg_relation_label_top1, Main.dir, new Comparator<String[]>() {

						@Override
						public int compare(String[] o1, String[] o2) {
							// TODO Auto-generated method stub
							int widindex = 2;
							// look at function doit() dw.write(....);
							int wid1 = Integer.parseInt(o1[widindex]);
							int wid2 = Integer.parseInt(o2[widindex]);
							return wid1 - wid2;
						}

					});
		}

	}

	static void filter_wp_stanford(String input_enid_mid_wid_argname_otherarg_relation_label,
			String output_wp_stanford_subset) throws IOException {

		HashSet<Integer> usedArtId = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(input_enid_mid_wid_argname_otherarg_relation_label);
			String[] line;
			while ((line = dr.read()) != null) {
				int artid = Integer.parseInt(line[2]);
				usedArtId.add(artid);
			}
			dr.close();
		}
		D.p(usedArtId);
		DelimitedReader dr = new DelimitedReader(Main.file_wp_stanford);
		DelimitedWriter dw = new DelimitedWriter(output_wp_stanford_subset + ".temp");
		String[] line;
		while ((line = dr.read()) != null) {
			int artid = Integer.parseInt(line[1]);
			if (usedArtId.contains(artid)) {
				dw.write(line);
			}
		}
		dw.close();
		dr.close();

		Sort.sort(output_wp_stanford_subset + ".temp", output_wp_stanford_subset, Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return Integer.parseInt(o1[1]) - Integer.parseInt(o2[1]);
			}
		});

	}

	//
	//	static void filter_wp_stanford_lucene() throws IOException {
	//
	//		HashSet<Integer> usedArtId = new HashSet<Integer>();
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_seedluceneclean);
	//			String[] line;
	//			while ((line = dr.read()) != null) {
	//				int artid1 = Integer.parseInt(line[6]);
	//				int artid2 = Integer.parseInt(line[7]);
	//				usedArtId.add(artid1);
	//				usedArtId.add(artid2);
	//			}
	//			dr.close();
	//		}
	//		D.p(usedArtId);
	//		DelimitedReader dr = new DelimitedReader(Main.file_wp_stanford);
	//		DelimitedWriter dw = new DelimitedWriter(Main.file_wp_stanford_subset + ".temp");
	//		String[] line;
	//		while ((line = dr.read()) != null) {
	//			int artid = Integer.parseInt(line[1]);
	//			if (usedArtId.contains(artid)) {
	//				dw.write(line);
	//			}
	//		}
	//		dw.close();
	//		dr.close();
	//		Sort.sort(Main.file_wp_stanford_subset + ".temp", Main.file_wp_stanford_subset, Main.dir,
	//				new Comparator<String[]>() {
	//					@Override
	//					public int compare(String[] o1, String[] o2) {
	//						// TODO Auto-generated method stub
	//						return Integer.parseInt(o1[1]) - Integer.parseInt(o2[1]);
	//					}
	//				});
	//	}

	public static void getCandidateNellstringMid(String file_enid_mid_wid_argname_otherarg_relation_label,
			String file_candidatemapping_nellstring_mid) {
		try {
			HashMap<String, String> map_notabletype = new HashMap<String, String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
				String[] l;
				while ((l = dr.read()) != null) {
					String mid = l[0];
					map_notabletype.put(mid, l[2]);
				}
			}
			List<String[]> raw = (new DelimitedReader(file_enid_mid_wid_argname_otherarg_relation_label)).readAll();
			DelimitedWriter dw = new DelimitedWriter(file_candidatemapping_nellstring_mid);
			HashSet<String> appear = new HashSet<String>();
			List<String[]> towrite = new ArrayList<String[]>();
			for (String[] line : raw) {
				String mid = line[1];
				String nellstring = line[3].toLowerCase();
				String key = mid + "\t" + nellstring;
				if (!appear.contains(key) && map_notabletype.containsKey(mid)) {
					appear.add(key);
					towrite.add(new String[] { mid, nellstring });
				}
			}
			Collections.sort(towrite, new Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[1].compareTo(o2[1]);
				}
			});
			for (String[] l : towrite) {
				dw.write(l);
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	public static void getCandidateNellstringMidLucene() throws IOException {
	//		{
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_candidatemapping_nellstring_mid);
	//			DelimitedReader dr = new DelimitedReader(Main.file_ontologyentity2fbentity_bylucene);
	//			String[] l;
	//			//Germany	/m/0fqgj3	6108409	Germany 	1	121105	/amusement_parks/ride
	//			while ((l = dr.read()) != null) {
	//				dw.write(l[1], l[0]);
	//			}
	//			dr.close();
	//			dw.close();
	//		}
	//	}

	//	public static void getWeightEntitynameCosineLucene() throws IOException {
	//		{
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_nellstring_mid_cosine);
	//			DelimitedReader dr = new DelimitedReader(Main.file_ontologyentity2fbentity_bylucene);
	//			String[] l;
	//			//Germany	/m/0fqgj3	6108409	Germany 	1	121105	/amusement_parks/ride
	//			while ((l = dr.read()) != null) {
	//				dw.write(l[1], l[0].toLowerCase(), l[4]);
	//			}
	//			dr.close();
	//			dw.close();
	//		}
	//	}

	public static void getWeightEntitynameCosine(String file_gnid_mid_enurl_wid_title_type_clean,
			String file_candidatemapping_nellstring_mid, String file_weight_nellstring_mid_cosine, double smooth) {
		// create a subset of mid_fbnamealias

		try {

			List<String[]> candidates = (new DelimitedReader(file_candidatemapping_nellstring_mid)).readAll();

			HashMap<String, String> map_mid_names = new HashMap<String, String>();
			{
				DelimitedReader dr = new DelimitedReader(file_gnid_mid_enurl_wid_title_type_clean);
				String[] l;
				while ((l = dr.read()) != null) {
					String mid = l[1];
					String names = l[4];
					map_mid_names.put(mid, names);
				}
				dr.close();
			}
			{
				DelimitedWriter dw = new DelimitedWriter(file_weight_nellstring_mid_cosine);
				for (String[] c : candidates) {
					String mid = c[0];
					if (mid.equals("/m/071_lf")) {
						D.p("a");
					}
					String nellstring = c[1];
					String names_raw = map_mid_names.get(mid);
					String names[] = names_raw.split(" ");
					double similarity = 0;
					String nell0 = PlingStemmer.stem(nellstring);
					for (String n : names) {
						n = n.replaceAll("_", " ");
						int[] par_return = new int[2];
						String n0 = PlingStemmer.stem(n);
						int share = StringUtil.numOfShareWords(n0, nell0, par_return);
						double s = Math.min(share * 1.0 / par_return[0], share * 1.0 / par_return[1]);
						if (s > similarity)
							similarity = s;
					}
					dw.write(mid, nellstring, similarity + smooth);
				}
				dw.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	public static void subsetfin_freebase_type_sortMid(String file_enid_mid_wid_argname_otherarg_relation_label,
	//			String file_freebase_type_sortMid_subset) {
	////		try {
	////			DelimitedReader dr = new DelimitedReader(file_enid_mid_wid_argname_otherarg_relation_label);
	////			DelimitedWriter dw = new DelimitedWriter(file_freebase_type_sortMid_subset);
	////			String[] line;
	////			HashSet<String> usedMid = new HashSet<String>();
	////			while ((line = dr.read()) != null) {
	////				String mid = line[1];
	////				usedMid.add(mid);
	////			}
	////			dr.close();
	////
	////			dr = new DelimitedReader(Main.fin_freebase_type_sortMid);
	////			while ((line = dr.read()) != null) {
	////				String mid = line[0];
	////				if (usedMid.contains(mid)) {
	////					dw.write(line);
	////				}
	////			}
	////			dr.close();
	////			dw.close();
	////		} catch (Exception e) {
	////			e.printStackTrace();
	////		}
	//	}

	public static void main(String[] args) throws Exception {
		//getRawCompareToFBSearch();
		//getRawLucene(Main.file_ontologyentity2fbentity_bylucene);
		//getCleanLucene();

		/** filter stanford wikipedia to get subset stanford */
		/** Use freebase engine to get raw nellstring 2 fb enurl */
		getraw1();
		getraw2();
		getraw3(2, Main.file_enid_mid_wid_argname_otherarg_relation_label);
		getraw3(1, Main.file_enid_mid_wid_argname_otherarg_relation_label_top1);
				/**
				 * Using a lot of files to get good looking, nellstring 2 mid &
				 * wikipedia id
				 */
		filter_wp_stanford(Main.file_enid_mid_wid_argname_otherarg_relation_label, Main.file_wp_stanford_subset);

		/** get all candidate <nellstring, mid> */
		getCandidateNellstringMid(Main.file_enid_mid_wid_argname_otherarg_relation_label,
				Main.file_candidatemapping_nellstring_mid);

		/** for every pair of <nellstring, mid>, get a similarity score for it */
		getWeightEntitynameCosine(Main.file_gnid_mid_enurl_wid_title_type_clean,
				Main.file_candidatemapping_nellstring_mid, Main.file_weight_nellstring_mid_cosine,
				Main.string_sim_smooth);

		{
			/**not working very well*/
			//		filter_wp_stanford_lucene();
			//		getCandidateNellstringMidLucene();
			//		getWeightEntitynameCosineLucene();
		}
		/** Use freebase engine to get raw nellstring 2 fb enurl Old version stuff*/
		//		/**
		//		 * Using a lot of files to get good looking, nellstring 2 mid &
		//		 * wikipedia id
		//		 */
		//		getraw(Main.file_ontologyentity2fbentity_byfbsearch);
		//		getClean(Main.file_gnid_mid_enurl_wid_title, Main.file_ontologyentity2fbentity_byfbsearch,
		//				Main.file_enid_mid_wid_argname_otherarg_relation_label);
		//
		//		getCleanTop1(Main.file_gnid_mid_enurl_wid_title, Main.file_ontologyentity2fbentity_byfbsearch,
		//				Main.file_enid_mid_wid_argname_otherarg_relation_label_top1);

		/**
		 * Get a subset of fbtype infomation, the whole set is in
		 * /projects/pardosa/s5/clzhang/ontologylink/fb_mid_type_argname
		 * */

	}

}
