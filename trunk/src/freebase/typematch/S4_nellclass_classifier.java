package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import multir.util.delimited.Sort;

import percept.main.LogLinear;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S4_nellclass_classifier {

	/**
	 * @param args
	 */

	static List<Integer> notNAwid = new ArrayList<Integer>();
	static List<Integer> NAwid = new ArrayList<Integer>();

	public static void nelltype_nellstring_mid_wid() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.fout_nelltype_mid_mainwid + ".temp");
			HashMap<String, String> nellstr_mid_sbweight = new HashMap<String, String>();
			HashMap<String, String> map_mid2mainwid = (new DelimitedReader(Main.fout_mid_artid_sbmid)).readAll2Hash(0,
					1);
			{
				List<String[]> all = (new DelimitedReader(Main.fout_weight_nellstring_mid_cosine)).readAll();
				StringTable.sortByColumn(all, new int[] { 1, 2 }, new boolean[] { false, true });
				for (String[] x : all) {
					if (!nellstr_mid_sbweight.containsKey(x[1])) {
						if (map_mid2mainwid.containsKey(x[0])) {
							nellstr_mid_sbweight.put(x[1], x[0]);
						}
					}
					// nellstr_mid_sbweight.get(x[1]).add(x);
				}
			}

			DelimitedReader dr = new DelimitedReader(Main.fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid);
			List<String[]> interesting = new ArrayList<String[]>();

			String[] line;
			int nellclasstypenull = 0;
			HashSet<String> avoiddup = new HashSet<String>();
			while ((line = dr.read()) != null) {

				String argname = line[3];
				String topmid = nellstr_mid_sbweight.get(argname.toLowerCase());
				String label = line[6];
				if (label.equals("-1"))
					continue;
				if (avoiddup.contains(argname)) {
					continue;
				}
				avoiddup.add(argname);
				HashSet<String> nellclass = Main.nellontology.entity2class.get(argname);
				{
					if (nellclass == null || topmid == null) {
						nellclasstypenull++;
						// D.p("nell class type is null:", argname);
						continue;
					}
				}
				for (String nt : nellclass) {
					interesting.add(new String[] { nt, topmid, argname, "-1" });
				}
				// D.p(argname, topmid, nellclass);
			}
			StringTable.sortUniq(interesting);
			StringTable.sortByColumn(interesting, new int[] { 1 });

			{
				/** Join mid_mainwid with interesting */
				int missingwid = 0;
				DelimitedReader drw = new DelimitedReader(Main.fout_mid_artid_sbmid);
				String[] l = drw.read();
				for (String[] t : interesting) {
					String topmid = t[1];
					while (l[0].compareTo(topmid) < 0 && (l = drw.read()) != null)
						;
					if (l[0].equals(topmid)) {
						t[3] = l[1];
					} else {
						missingwid++;
					}
				}
				D.p("missing wid ", missingwid);
			}

			{
				StringTable.sortByColumn(interesting, new int[] { 3 }, new boolean[] { true }, new boolean[] { true });
				for (String[] t : interesting) {
					dw.write(t);
					notNAwid.add(Integer.parseInt(t[3]));
				}
			}
			D.p("Missing topmid ", nellclasstypenull);

			{
				/** get NA examples */
				dr = new DelimitedReader(Main.fout_wp_mainidlist);
				String[] l;
				List<Integer> all = new ArrayList<Integer>();
				while ((l = dr.read()) != null) {
					int x = Integer.parseInt(l[0]);
					if (x < 1000000) {
						all.add(x);
					}
				}
				dr.close();
				Collections.shuffle(all);
				for (int i = 0; i < notNAwid.size() * 3; i++) {
					int wid = all.get(i);
					if (!notNAwid.contains(wid)) {
						dw.write("NA", "dont_care_mid", "dont_care_name", all.get(i));
					}
				}
			}
			dw.close();
			{
				Sort.sort(Main.fout_nelltype_mid_mainwid + ".temp", Main.fout_nelltype_mid_mainwid, Main.dir,
						new Comparator<String[]>() {

							@Override
							public int compare(String[] o1, String[] o2) {
								// TODO Auto-generated method stub
								int a0 = Integer.parseInt(o1[3]);
								int a1 = Integer.parseInt(o2[3]);
								return a0 - a1;
							}

						});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void subset_stanfordwiki() {
		try {
			HashSet<Integer> usedArtid = new HashSet<Integer>();
			List<String[]> all = (new DelimitedReader(Main.fout_nelltype_mid_mainwid)).readAll();
			for (String[] l : all) {
				usedArtid.add(Integer.parseInt(l[3]));
			}
			DelimitedReader dr = new DelimitedReader(Main.fin_wp_stanford);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_wp_stanford_s4subset);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[1]);
				if (usedArtid.contains(wid)) {
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void subset_category() {
		try {
			HashSet<Integer> usedArtid = new HashSet<Integer>();
			List<String[]> all = (new DelimitedReader(Main.fout_nelltype_mid_mainwid)).readAll();
			for (String[] l : all) {
				usedArtid.add(Integer.parseInt(l[3]));
			}
			DelimitedReader dr = new DelimitedReader(Main.fout_wid_categorywiki);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_wid_categorywiki_subset);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[0]);
				if (usedArtid.contains(wid)) {
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Featurize */
	public static void featurize() {
		try {
			HashCount<String> hc_nelltype = new HashCount<String>();
			{

				List<String[]> preprocess = (new DelimitedReader(Main.fout_nelltype_mid_mainwid)).readAll();
				for (String[] a : preprocess) {
					hc_nelltype.add(a[0]);
				}
			}

			DelimitedWriter dw = new DelimitedWriter(Main.fout_nelltype_featurized);
			DelimitedReader dr = new DelimitedReader(Main.fout_nelltype_mid_mainwid);
			DelimitedReader drwk = new DelimitedReader(Main.fout_wp_stanford_s4subset);
			DelimitedReader drcat = new DelimitedReader(Main.fout_wid_categorywiki_subset);
			List<String[]> catblock = drcat.readBlock(0);
			List<RecordWpSenToken> wkreader = RecordWpSenToken.readByArticleId(drwk);
			String[] l;
			Random r = new Random();
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[3]);
				String nelltype = l[0];
				int nelltypefreq = hc_nelltype.see(nelltype);
				if (nelltypefreq < Main.MIN_TRAINING_INSTANCE)
					continue;
				if (nelltype.equals("NA")) {
					double ran = r.nextDouble();
					if (ran > Main.NATakeRatio) {
						continue;
					}
				}
				StringBuilder sb = new StringBuilder();
				// sb.append(nelltype + " ");

				{
					/** Sentence feature */
					while (wkreader.get(0).articleId < wid
							&& (wkreader = RecordWpSenToken.readByArticleId(drwk)) != null) {
						// wait
					}
					if (wkreader.get(0).articleId == wid) {
						for (int i = 0; i < Main.TOPKSentenceInWkarticle && i < wkreader.size(); i++) {
							//only take noun
//							String []tokens = wkreader.get(i).token;
//							String []pos = wkreader.get(i).pos;
//							for(int k=0;k<tokens.length;k++){
//								if(pos[k].startsWith("N")){
//									sb.append("W_" + tokens[k] + " ");
//								}
//							}
							for (String t : wkreader.get(i).token) {
								sb.append("W_" + t + " ");
							}
						}
					} else {
						D.p("Missing one wkarticle:", wid);
					}
				}

				{
					/** Category feature */
					while (Integer.parseInt(catblock.get(0)[0]) < wid && (catblock = drcat.readBlock(0)) != null) {
						// wait, do nothing
					}
					if (Integer.parseInt(catblock.get(0)[0]) == wid) {
						for (String c[] : catblock) {
							sb.append(c[1] + " ");
						}
						for (String c[] : catblock) {
							String s = c[1].replace("Category:", "");
							List<String> sl = StringUtil.tokenize(s, new char[] { '_', ' ' });
							for (String a : sl) {
								sb.append("CW_" + a + " ");
							}
						}
					}
				}
				dw.write(nelltype, sb.toString());
			}
			// hc_nelltype.printAll();
			dw.close();
			dr.close();
			drcat.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void binaryTrain() {
		try {
			List<String[]> all = (new DelimitedReader(Main.fout_nelltype_featurized)).readAll();
			HashSet<String> types = new HashSet<String>();
			for (String[] a : all) {
				String nelltype = a[0].split(" ")[0];
				types.add(nelltype);
			}
			for (String t : types) {
				List<String[]> binaryCases = new ArrayList<String[]>();
				String neg_t = "NEG_" + t;
				/** training & testing */
				for (String[] a : all) {
					if (a[0].equals(t)) {
						binaryCases.add(a);
					} else {
						binaryCases.add(new String[] { neg_t, a[1] });
					}
				}
				{
					String temptrain = Main.fout_nelltype_binary + "_" + t + "_train";
					String temptest = Main.fout_nelltype_binary + "_" + t + "_test";
					DelimitedWriter dw1 = new DelimitedWriter(temptrain);
					DelimitedWriter dw2 = new DelimitedWriter(temptest);
					Collections.shuffle(binaryCases);
					StringTable.sortByColumn(binaryCases, new int[] { 0 });

					for (int i = 0; i < binaryCases.size(); i++) {
						if (i % 4 == 0) {
							dw2.write(binaryCases.get(i));
						} else {
							dw1.write(binaryCases.get(i));
						}
					}
					dw1.close();
					dw2.close();
					LogLinear ll = new LogLinear(Main.dir_nellclassifier, temptrain);
					ll.binarypredict(temptest);
				}
			}
			D.p("a");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void selftest() {
		// random split to training and test
		try {
			List<String[]> all = (new DelimitedReader(Main.fout_nelltype_featurized)).readAll();

			Collections.shuffle(all);
			DelimitedWriter dw1 = new DelimitedWriter(Main.fout_nelltype_featurized + ".train");
			DelimitedWriter dw2 = new DelimitedWriter(Main.fout_nelltype_featurized + ".test");
			for (int i = 0; i < all.size(); i++) {
				if (i % 5 == 0) {
					dw2.write(all.get(i));
				} else {
					dw1.write(all.get(i));
				}
			}
			dw1.close();
			dw2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogLinear ll = new LogLinear(Main.dir_nellclassifier, Main.fout_nelltype_featurized + ".train");
		ll.predict(Main.fout_nelltype_featurized + ".test", Main.fout_nelltype_featurized + ".debug");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/**
		 * nelltype_nellstring_mid_wid, then sample subset of the Wikipedia
		 * article
		 */
		// nelltype_nellstring_mid_wid();
		// subset_stanfordwiki();
		// subset_category();

		/** featurize */
		featurize();

		/** Train */
		selftest();
	}

}
