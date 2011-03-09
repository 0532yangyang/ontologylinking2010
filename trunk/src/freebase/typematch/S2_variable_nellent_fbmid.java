package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.webapi.FBSearchEngine;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class S2_variable_nellent_fbmid {

	/**
	 * @param args
	 */
	private static String[] doit(String arg1, String arg2) {
		List<String> ares = FBSearchEngine.query2(arg1, 10);
		List<String> bres = FBSearchEngine.query2(arg2, 10);
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

	public static void getraw() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.fout_fbsearchresult_raw);
			NellOntology no = new NellOntology();
			for (NellRelation nr : no.nellRelationList) {
				try {
					if (nr.seedInstances == null || nr.seedInstances.size() == 0)
						continue;
					for (String[] a : nr.seedInstances) {
						String[] res = doit(a[0], a[1]);
						dw.write("+1", a[0], a[1], nr.relation_name, res[0], res[1]);
					}
					for (String[] a : nr.known_negatives) {
						String[] res = doit(a[0], a[1]);
						dw.write("-1", a[0], a[1], nr.relation_name, res[0], res[1]);
					}
				} catch (Exception e) {
					System.err.println(nr.relation_name);
				}

			}
			dw.close();
		} catch (Exception e) {

		}
	}

	static HashMap<String, String> enurl2mid = new HashMap<String, String>();
	static HashMap<String, List<Integer>> mid2artid = new HashMap<String, List<Integer>>();

	public static void getClean() {

		try {
			DelimitedReader dr = new DelimitedReader(Main.fin_mid_enurl);
			String[] line;
			while ((line = dr.read()) != null) {
				String mid = line[0];
				String enurl = line[1];
				if (enurl2mid.containsKey(enurl))
					D.p("Duplicate enurl " + enurl);
				enurl2mid.put(enurl, mid);
			}

			dr = new DelimitedReader(Main.fout_mid_artid);
			while ((line = dr.read()) != null) {
				String mid = line[0];
				int artid = Integer.parseInt(line[1]);
				if (!mid2artid.containsKey(mid)) {
					mid2artid.put(mid, new ArrayList<Integer>());
				}
				mid2artid.get(mid).add(artid);
			}

			dr = new DelimitedReader(Main.fout_fbsearchresult_raw);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_fbsearchresult_clean + ".temp");
			while ((line = dr.read()) != null) {
				if (line.length < 5)
					continue;
				String label = line[0];
				String arg1 = line[1];
				String arg2 = line[2];
				String relation = line[3];
				String[] arg1enid = line[4].split(";");
				String[] arg2enid = line[5].split(";");
				for (String a1 : arg1enid) {
					doit(dw, a1, arg1, arg2, relation, label, "arg1");
				}
				for (String a2 : arg2enid) {
					doit(dw, a2, arg2, arg1, relation, label, "arg2");
				}
				// D.p(arg1enid.length,arg2enid.length);
				// break;
			}
			dr.close();
			dw.close();
			{
				Sort.sort(Main.fout_fbsearchresult_clean + ".temp", Main.fout_fbsearchresult_clean, Main.dir,
						new Comparator<String[]>() {

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
		} catch (Exception e) {

		}
	}

	private static void doit(DelimitedWriter dw, String a, String arg1, String arg2, String relation, String label,
			String arg1OrArg2) throws IOException {
		if (!a.startsWith("/en")) {
			return;
		}
		a = a.replace("/en/", "");
		String mid = enurl2mid.get(a);
		if (mid == null) {
			return;
		}
		List<Integer> list_artid = mid2artid.get(mid);
		if (list_artid == null) {
			return;
		}

		String enid = a;
		String argname = arg1;
		String otherarg = arg2;
		for (int artid : list_artid) {
			dw.write(enid, mid, artid, argname, otherarg, relation, label, arg1OrArg2);
		}

	}

	static void filter_wp_stanford() {
//		try {
//			HashSet<Integer>usedArtId = new HashSet<Integer>();
//			{
//				DelimitedReader dr = new DelimitedReader(Main.fout_fbsearchresult_clean);
//				String[] line;
//				while ((line = dr.read()) != null) {
//					int artid = Integer.parseInt(line[2]);
//					usedArtId.add(artid);
//				}
//				dr.close();
//			}
//			D.p(usedArtId);
//			DelimitedReader dr = new DelimitedReader(Main.fin_wp_stanford);
//			DelimitedWriter dw = new DelimitedWriter(Main.fin_wp_stanford_subset+".temp");
//			String[] line;
//			while ((line = dr.read()) != null) {
//				int artid = Integer.parseInt(line[1]);
//				if(usedArtId.contains(artid)){
//					dw.write(line);
//				}
//			}
//			dw.close();
//			dr.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		try{
			Sort.sort(Main.fin_wp_stanford_subset+".temp", Main.fin_wp_stanford_subset, Main.dir, new Comparator<String[]>(){

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return Integer.parseInt(o1[1])-Integer.parseInt(o2[1]);
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/** Use freebase engine to get raw nellstring 2 fb enurl */
		// getraw();

		/**
		 * Using a lot of files to get good looking, nellstring 2 mid &
		 * wikipedia id
		 */
		// getClean();

		/** filter stanford wikipedia to get subset stanford */
		filter_wp_stanford();
		
		/***/

	}

}
