package freebase.jointmatch8;

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
					if(a[0].equals("")){
						D.p("a");
					}
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

	public static void main(String[] args) throws Exception {

		/** filter stanford wikipedia to get subset stanford */
		/** Use freebase engine to get raw nellstring 2 fb enurl */
		getraw1();
		getraw2();
		subsetWikiSenWikilink(Main.file_wksensubset, Main.file_wklinksub);

	}

}
