package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.parsers.PlingStemmer;
import javatools.string.StringUtil;
import javatools.webapi.FBSearchEngine;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class S2_variable_nellent_fbmid {

	/**
	 * @param args
	 */
	static HashMap<String, List<String>> store = new HashMap<String, List<String>>();

	private static String[] doit(String arg1, String arg2) {
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
		try {
			HashSet<Integer> usedArtId = new HashSet<Integer>();
			{
				DelimitedReader dr = new DelimitedReader(Main.fout_fbsearchresult_clean);
				String[] line;
				while ((line = dr.read()) != null) {
					int artid = Integer.parseInt(line[2]);
					usedArtId.add(artid);
				}
				dr.close();
			}
			D.p(usedArtId);
			DelimitedReader dr = new DelimitedReader(Main.fin_wp_stanford);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_wp_stanford_subset + ".temp");
			String[] line;
			while ((line = dr.read()) != null) {
				int artid = Integer.parseInt(line[1]);
				if (usedArtId.contains(artid)) {
					dw.write(line);
				}
			}
			dw.close();
			dr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Sort.sort(Main.fout_wp_stanford_subset + ".temp", Main.fout_wp_stanford_subset, Main.dir,
					new Comparator<String[]>() {

						@Override
						public int compare(String[] o1, String[] o2) {
							// TODO Auto-generated method stub
							return Integer.parseInt(o1[1]) - Integer.parseInt(o2[1]);
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getWeightEntitynameCosine() {
		// create a subset of mid_fbnamealias

		try {

			List<String[]> candidates = (new DelimitedReader(Main.fout_candidatemapping_nellstring_mid)).readAll();

			if (true) {
				/** take a subset of mid_fbname to speed up everything */
				HashSet<String> usedMids = new HashSet<String>();
				for (String[] l : candidates) {
					usedMids.add(l[0]);
				}
				DelimitedReader dr = new DelimitedReader(Main.fin_fbnamealias);
				DelimitedWriter dw = new DelimitedWriter(Main.fin_fbnamealias_subset);
				String[] l;
				while ((l = dr.read()) != null) {
					if (usedMids.contains(l[0])) {
						dw.write(l);
					}
				}
				dr.close();
				dw.close();
			}

			HashMap<String, List<String>> mid_map_names = new HashMap<String, List<String>>();
			{
				DelimitedReader dr = new DelimitedReader(Main.fin_fbnamealias_subset);
				String[] l;
				while ((l = dr.read()) != null) {
					String mid = l[0];
					if (!mid_map_names.containsKey(mid))
						mid_map_names.put(mid, new ArrayList<String>());
					mid_map_names.get(mid).add(l[1]);
				}
				dr.close();
			}
			{
				DelimitedWriter dw = new DelimitedWriter(Main.fout_weight_nellstring_mid_cosine);
				for (String[] c : candidates) {
					String mid = c[0];
					if (mid.equals("/m/058b6")) {
						D.p("a");
					}
					String nellstring = c[1];
					List<String> names = mid_map_names.get(mid);
					double similarity = 0;
					String nell0 = PlingStemmer.stem(nellstring);
					for (String n : names) {
						int[] par_return = new int[2];
						String n0 = PlingStemmer.stem(n);
						int share = StringUtil.numOfShareWords(n0, nell0, par_return);
						double s = Math.min(share * 1.0 / par_return[0], share * 1.0 / par_return[1]);
						if (s > similarity)
							similarity = s;
					}
					dw.write(mid, nellstring, similarity);
				}
				dw.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getCandidateNellstringMid() {
		try {
			List<String[]> raw = (new DelimitedReader(Main.fout_fbsearchresult_clean)).readAll();
			DelimitedWriter dw = new DelimitedWriter(Main.fout_candidatemapping_nellstring_mid);
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

	public static void nellstringMidScore() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.fout_temp_mid_wid_argA_argB_appearInWiki);
			List<String[]> raw = (new DelimitedReader(Main.fout_fbsearchresult_clean)).readAll();
			DelimitedReader dr = new DelimitedReader(Main.fout_wp_stanford_subset);
			List<RecordWpSenToken> rl = RecordWpSenToken.readByArticleId(dr);
			HashSet<String> wkappear_midnellstr = new HashSet<String>();
			for (String[] line : raw) {
				int artid = Integer.parseInt(line[2]);
				while (rl != null && rl.get(0).articleId < artid && (rl = RecordWpSenToken.readByArticleId(dr)) != null)
					;
				// if we find some match
				if (rl != null && rl.size() > 0 && rl.get(0).articleId == artid) {
					// D.p("match "+artid);
					String mid = line[1];
					String arg = line[3];
					String otherarg = line[4];
					String relationname = line[5];
					String label = line[6];
					if (label.equals("-1"))
						continue; // sometimes -1 examples has some problem

					List<String> otherargtoken = StringUtil.tokenize(otherarg);
					List<String> relationnametoken = StringUtil.tokenize(relationname);

					for (RecordWpSenToken r : rl) {
						List<String> tokens = new ArrayList<String>();
						for (String t : r.token)
							tokens.add(t);
						int sharwords = StringUtil.numOfShareWords(tokens, otherargtoken);
						if (sharwords == otherargtoken.size()) {
							dw.write(mid, artid, arg, otherarg);
							wkappear_midnellstr.add(mid + "\t" + arg);
						}
					}
				}
			}
			dw.close();

			Collections.sort(raw, new Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					String a = o1[1] + "\t" + o1[3];
					String b = o2[1] + "\t" + o2[3];
					return a.compareTo(b);
				}
			});

			DelimitedWriter dw2 = new DelimitedWriter(Main.fout_candidatemapping_nellstring_mid);
			HashSet<String> written = new HashSet<String>();
			for (String[] l : raw) {
				String key = l[1] + "\t" + l[3];
				double weight = 0;
				if (wkappear_midnellstr.contains(key)) {
					weight = 1;
				} else {
					weight = 0.1;
				}

				if (!written.contains(key)) {
					dw2.write(l[1], l[3], weight);
					written.add(key);
				}
			}

			dw2.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void nellstringMidScore2() {
		try {
			List<String[]> raw = (new DelimitedReader(Main.fout_fbsearchresult_clean)).readAll();
			Collections.sort(raw, new Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					String a = o1[1] + "\t" + o1[3];
					String b = o2[1] + "\t" + o2[3];
					return a.compareTo(b);
				}
			});

			int s = 0;
			List<String[]> block;
			while ((block = readNextPair(raw, s)) != null && block.size() > 0) {
				D.p("a");
				s += block.size();
			}
		} catch (Exception e) {

		}
	}

	private static List<String[]> readNextPair(List<String[]> raw, int start) {
		D.p(start);
		List<String[]> result = new ArrayList<String[]>();
		if (start < 0 || start >= raw.size())
			return null;
		String key = raw.get(start)[1] + "\t" + raw.get(start)[3];
		for (int i = start; i < raw.size(); i++) {
			String key0 = raw.get(i)[1] + "\t" + raw.get(i)[3];
			if (key.equals(key0)) {
				result.add(raw.get(i));
			}
		}
		return result;
	}

	public static void subsetfin_freebase_type_sortMid() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_freebase_type_sortMid_subset);
			String[] line;
			HashSet<String> usedMid = new HashSet<String>();
			while ((line = dr.read()) != null) {
				String mid = line[1];
				usedMid.add(mid);
			}
			dr.close();

			dr = new DelimitedReader(Main.fin_freebase_type_sortMid);
			while ((line = dr.read()) != null) {
				String mid = line[0];
				if (usedMid.contains(mid)) {
					dw.write(line);
				}
			}
			dr.close();
			dw.close();
		} catch (Exception e) {
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
		// filter_wp_stanford();

		/** get all candidate <nellstring, mid> */
		// getCandidateNellstringMid();

		/** for every pair of <nellstring, mid>, get a similarity score for it */
		getWeightEntitynameCosine();

		
		/**Get a subset of fbtype infomation, the whole set is in
		 * /projects/pardosa/s5/clzhang/ontologylink/fb_mid_type_argname
		 * */
		//subsetfin_freebase_type_sortMid();
	}

}
