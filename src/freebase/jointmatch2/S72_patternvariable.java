package freebase.jointmatch2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import freebase.typematch.RecordWpSenToken;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S72_patternvariable {

	private static void sampleSql2isntance(int SAMPLE_NUM, int throwaway_threshold) throws IOException {

		HashMap<Integer, String> wid2title = new HashMap<Integer, String>();
		{
			//load wid 2 title
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);

			String[] l;
			while ((l = dr.read()) != null) {
				String[] names = l[3].split(" ");
				if (names != null && names.length > 0) {
					wid2title.put(Integer.parseInt(l[2]), names[0]);
				}
			}
		}
		HashCount<String> hc = new HashCount<String>();
		List<String[]> towrite = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_shuffle);
			DelimitedWriter dw = new DelimitedWriter(Main.file_sql2instance_patternvariable);
			String[] l;
			while ((l = dr.read()) != null) {
				String key = l[2] + "\t" + l[3];
				int a = hc.see(key);
				if (a < SAMPLE_NUM) {
					int wid1 = Integer.parseInt(l[0]);
					int wid2 = Integer.parseInt(l[1]);
					String title1 = wid2title.get(wid1);
					String title2 = wid2title.get(wid2);
					if (title1 != null && title2 != null) {
						towrite.add(new String[] { wid1 + "", wid2 + "", title1, title2, l[2], l[3] });
						//dw.write(wid1, wid2, title1, title2, l[2], l[3]);
						hc.add(key);
					}
				}
			}
			StringTable.sortByColumn(towrite, new int[] { 4, 5 });
			for (String[] w : towrite) {
				String key = w[4] + "\t" + w[5];
				int a = hc.see(key);
				if (a > throwaway_threshold) {
					dw.write(w);
				}
			}
			dw.close();
			dr.close();
		}
	}

	static void getSubsetStanford() {
		try {
			HashSet<Integer> used = new HashSet<Integer>();
			{
				DelimitedReader dr1 = new DelimitedReader(Main.file_sql2instance_patternvariable);
				String[] l;

				while ((l = dr1.read()) != null) {
					used.add(Integer.parseInt(l[0]));
					used.add(Integer.parseInt(l[1]));
				}
				dr1.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_wp_stanford);
				DelimitedWriter dw = new DelimitedWriter(Main.file_stanford_subsetforpattern);
				String[] l;
				while ((l = dr.read()) != null) {
					int wid = Integer.parseInt(l[1]);
					if (used.contains(wid)) {
						dw.write(l);
					}
				}
				dw.close();
				dr.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**S1_pair2feature
	 * featurize the pair*/
	static void featurize() {
		try {
			HashMap<Integer, List<String>> wid2names = new HashMap<Integer, List<String>>();
			DelimitedWriter dw = new DelimitedWriter(Main.file_widpair_patterns);

			HashSet<String> patternsTakeintoConsider = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
				String[] l;
				while ((l = dr.read()) != null) {
					int wid = Integer.parseInt(l[2]);
					String[] s = l[3].split(" ");
					List<String> sl = new ArrayList<String>();
					for (String s0 : s)
						sl.add(s0);
					wid2names.put(wid, sl);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_background_pattern_uniqc_10);
				String[] l;
				while ((l = dr.read()) != null) {
					patternsTakeintoConsider.add(l[0]);
				}
				dr.close();
			}

			featurize_help(0, Main.file_sql2instance_patternvariable, Main.file_stanford_subsetforpattern, wid2names,
					patternsTakeintoConsider, dw);
			featurize_help(1, Main.file_sql2instance_patternvariable, Main.file_stanford_subsetforpattern, wid2names,
					patternsTakeintoConsider, dw);

			dw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block`
			e.printStackTrace();
		}
	}

	//0 : wid1
	//1: wid2
	private static void featurize_help(int wid1Orwid2, String file_widpair, String file_wikipedia,
			HashMap<Integer, List<String>> wid2names, HashSet<String> patternsTakeintoConsider, DelimitedWriter dw)
			throws Exception {
		D.p("featurize", wid1Orwid2);
		List<String[]> widpair = (new DelimitedReader(file_widpair)).readAll();
		StringTable.sortByIntColumn(widpair, new int[] { wid1Orwid2 });
		List<List<String[]>> widblocks = StringTable.toblock(widpair, wid1Orwid2);
		DelimitedReader dr = new DelimitedReader(file_wikipedia);
		List<RecordWpSenToken> list_rwst = RecordWpSenToken.readByArticleId(dr, true);
		for (List<String[]> b1 : widblocks) {
			int wid = Integer.parseInt(b1.get(0)[wid1Orwid2]);
			if (list_rwst == null)
				break;
			while (list_rwst.get(0).articleId < wid
					&& (list_rwst = RecordWpSenToken.readByArticleId(dr, false)) != null) {
				//wait;
			}
			if (list_rwst.get(0).articleId == wid) {
				//iterate over b1 and b2
				D.p(wid);
				for (String[] l1 : b1) {
					int anotherwid = Integer.parseInt(l1[1 - wid1Orwid2]);
					if (wid == anotherwid)
						break;
					List<String> wid2name = wid2names.get(anotherwid);
					if (wid2name == null) {
						System.err.println("Missing\t" + l1[1]);
						continue;
					}
					for (RecordWpSenToken rwst : list_rwst) {
						//D.p(l1,l2);
						List<String> wl2 = new ArrayList<String>();
						{
							for (String t : rwst.token) {
								wl2.add(t);
							}
						}
						for (String n : wid2name) {
							List<String> wl1 = new ArrayList<String>();

							{
								String[] s = n.split("_");
								for (String s0 : s)
									wl1.add(s0);
							}
							int same = StringUtil.numOfShareWords(wl1, wl2, new boolean[] { true, true, true });
							if (same == wl1.size()) {
								StringBuilder sb = new StringBuilder();
								//all words fo the name are finding something in the sentence
								Set<String> patterns = new HashSet<String>();
								Pattern.patternize(rwst.token, rwst.pos, rwst.ner, patterns, 3);
								for (String p : patterns) {
									if (patternsTakeintoConsider.contains(p)) {
										sb.append(p + " ");
									}
								}
								dw.write(l1[0], l1[1], l1[2], l1[3], l1[4], l1[5], n, rwst.sentenceId, sb.toString());
								//D.p(l1, n, rwst.text);
							}
						}
					}
				}
				//D.p(wid1);
				//get the sentences of that wid1, the idea is, if the sentence contains the arg2, then featurize this sentence~~~
				//do something!!!
			}
		}
		dr.close();
	}

	public static void main(String[] args) throws IOException {
		StringTable.shuffleLargeFile(Main.file_sql2instance, Main.dir, Main.file_sql2instance_shuffle);
		sampleSql2isntance(Main.SAMPLE_NUM, Main.throwaway_threshold);
		List<String[]> widpairs = (new DelimitedReader(Main.file_sql2instance_patternvariable)).readAll();
		Pattern.widpair2feature(widpairs, 1, Main.file_patternize_sql2pair);
	}
}
