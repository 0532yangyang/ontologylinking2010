package freebase.jointmatch4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.parsers.PlingStemmer;
import javatools.string.StringUtil;
import javatools.webapi.FBSearchEngine;


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

	public static void getCandidateNellstringMid(String file_enid_mid_wid_argname_otherarg_relation_label,
			String file_candidatemapping_nellstring_mid) {
		try {
			List<String[]> raw = (new DelimitedReader(file_enid_mid_wid_argname_otherarg_relation_label)).readAll();
			DelimitedWriter dw = new DelimitedWriter(file_candidatemapping_nellstring_mid);
			HashSet<String> appear = new HashSet<String>();
			List<String[]> towrite = new ArrayList<String[]>();
			for (String[] line : raw) {
				String mid = line[1];
				String nellstring = line[3].toLowerCase();
				String key = mid + "\t" + nellstring;
				if (!appear.contains(key)) {
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

	public static void getWeightEntitynameCosine(String file_gnid_mid_enurl_wid_title,
			String file_candidatemapping_nellstring_mid, String file_weight_nellstring_mid_cosine, double smooth) {
		// create a subset of mid_fbnamealias

		try {

			List<String[]> candidates = (new DelimitedReader(file_candidatemapping_nellstring_mid)).readAll();

			HashMap<String, String> map_mid_names = new HashMap<String, String>();
			{
				DelimitedReader dr = new DelimitedReader(file_gnid_mid_enurl_wid_title);
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

		/** Use freebase engine to get raw nellstring 2 fb enurl */
		getraw(Main.file_ontologyentity2fbentity_byfbsearch);

		/**
		 * Using a lot of files to get good looking, nellstring 2 mid &
		 * wikipedia id
		 */
		getClean(Main.file_gnid_mid_enurl_wid_title, Main.file_ontologyentity2fbentity_byfbsearch,
				Main.file_enid_mid_wid_argname_otherarg_relation_label);

		getCleanTop1(Main.file_gnid_mid_enurl_wid_title, Main.file_ontologyentity2fbentity_byfbsearch,
				Main.file_enid_mid_wid_argname_otherarg_relation_label_top1);

		/** filter stanford wikipedia to get subset stanford */
		filter_wp_stanford(Main.file_enid_mid_wid_argname_otherarg_relation_label, Main.file_wp_stanford_subset);

		/** get all candidate <nellstring, mid> */
		getCandidateNellstringMid(Main.file_enid_mid_wid_argname_otherarg_relation_label,
				Main.file_candidatemapping_nellstring_mid);

		/** for every pair of <nellstring, mid>, get a similarity score for it */
		getWeightEntitynameCosine(Main.file_gnid_mid_enurl_wid_title, Main.file_candidatemapping_nellstring_mid,
				Main.file_weight_nellstring_mid_cosine, Main.string_sim_smooth);

		/**
		 * Get a subset of fbtype infomation, the whole set is in
		 * /projects/pardosa/s5/clzhang/ontologylink/fb_mid_type_argname
		 * */

	}

}
