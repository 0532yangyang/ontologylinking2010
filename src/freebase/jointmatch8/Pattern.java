package freebase.jointmatch8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import freebase.typematch.RecordWpSenToken;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.RemoveStopwords;
import javatools.string.Stemmer;
import javatools.string.StringUtil;

public class Pattern {

	public static void patternize(String[] token, String[] pos, String[] ner, Set<String> patterns, int WINSIZE) {
		List<String> l = new ArrayList<String>();
		List<String> lner = new ArrayList<String>();
		for (int i = 0; i < token.length; i++) {

			String s = token[i].toLowerCase();
			char[] c = pos[i].toCharArray();
			//is not a word!
			if (!Character.isLetter(c[0])) {
				continue;
			}
			//is a stop word!
			if (RemoveStopwords.isStop(s)) {
				continue;
			}

			//stemmer
			s = Stemmer.stem(s);

			if (!ner[i].equals("O")) {
				lner.add(ner[i]);
			} else {
				lner.add(s);
			}
			l.add(s);
		}

		for (int i = 0; i < l.size(); i++) {
			patterns.add(l.get(i));
		}
		for (int k = 2; k <= WINSIZE; k++) {
			for (int i = 0; i < l.size() - WINSIZE; i++) {
				StringBuilder sb = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				for (int j = i; j < i + k; j++) {
					sb.append(l.get(j) + "_");
					sb2.append(lner.get(j) + "_");
				}
				patterns.add(sb.toString());
				patterns.add(sb2.toString());
			}
		}
		l = null;
		lner = null;
	}

	public static void doit2() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern_raw);
			//HashCount<String> hc = new HashCount<String>();
			Set<String> patterns = new HashSet<String>();
			long sum = 0;
			RecordWpSenToken rwst = new RecordWpSenToken();
			DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_wp_stanford);
			while ((rwst = RecordWpSenToken.read(dr)) != null) {
				patternize(rwst.token, rwst.pos, rwst.ner, patterns, 3);
				//D.p(patterns);
				for (String p : patterns) {
					dw.write(p);
					sum++;
				}
				patterns.clear();
			}
			//			D.p(sum);
			//			dw.write("TOTAL",sum);
			//Iterator<Entry<String, Integer>> it = hc.iterator();
			//			while (it.hasNext()) {
			//				Entry<String, Integer> e = it.next();
			//				String p = e.getKey();
			//				int c = e.getValue();
			//				if (c > 1) {
			//					dw.write(p, c);
			//				}
			//			}
			dw.close();
		} catch (Exception e) {

		}
	}

	static void uniqc() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_background_pattern_sort);
			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern_uniqc);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				String str = b.get(0)[0];
				if (b.size() == 1)
					continue;

				char[] c = str.toCharArray();
				int numLetter = 0;
				for (int i = 0; i < c.length; i++) {
					if (c[i] >= 'a' && c[i] <= 'z' || c[i] >= 'A' && c[i] <= 'Z') {
						numLetter++;
					}
				}
				if (numLetter * 2 < c.length) {
					continue;
				}
				if (str.contains("www.") || str.contains("http:")) {
					continue;
				}
				dw.write(str, b.size());

			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void filterLessThanK(int k) {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_background_pattern_uniqc);
			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern_uniqc_10);
			String[] l;
			while ((l = dr.read()) != null) {
				int x = Integer.parseInt(l[1]);
				if (x > 10) {
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

	static void getSubsetStanford(HashSet<Integer> usedwids, String output_stanfordsubset) {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_wp_stanford);
			DelimitedWriter dw = new DelimitedWriter(output_stanfordsubset);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[1]);
				if (usedwids.contains(wid)) {
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

	/**List<String[]> every line, wid1, wid2 ... (comments, can be any long)*/
	public static void widpair2feature(List<String[]> pairs, String output) {
		HashSet<Integer> used = new HashSet<Integer>();
		String output_stanfordsubset = output + ".stanfordsubset";
		for (String[] l : pairs) {
			used.add(Integer.parseInt(l[0]));
			used.add(Integer.parseInt(l[1]));
		}
		getSubsetStanford(used, output_stanfordsubset);
		featurize(pairs, output_stanfordsubset, output);
	}

	public static void widpair2feature(List<String[]> pairs, String stanfordfile, String output) {
		String output_stanfordsubset = stanfordfile;
		featurize(pairs, output_stanfordsubset, output);
	}

	/**S1_pair2feature
	 * featurize the pair*/
	private static void featurize(List<String[]> pairs, String input_stanfordsubset, String output) {
		try {
			HashMap<Integer, List<String>> wid2names = new HashMap<Integer, List<String>>();
			DelimitedWriter dw = new DelimitedWriter(output);
			HashSet<String> patternsTakeintoConsider = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
				String[] l;
				while ((l = dr.read()) != null) {
					int wid = Integer.parseInt(l[3]);
					String[] s = l[4].split(" ");
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
			featurize_help(0, pairs, input_stanfordsubset, wid2names, patternsTakeintoConsider, dw);
			featurize_help(1, pairs, input_stanfordsubset, wid2names, patternsTakeintoConsider, dw);
			dw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block`
			e.printStackTrace();
		}
	}

	//0 : wid1
	//1: wid2
	private static void featurize_help(int wid1Orwid2, List<String[]> widpair, String file_wikipedia,
			HashMap<Integer, List<String>> wid2names, HashSet<String> patternsTakeintoConsider, DelimitedWriter dw)
			throws Exception {
		D.p("featurize", wid1Orwid2);
		//List<String[]> widpair = (new DelimitedReader(file_widpair)).readAll();
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
								String[] w = new String[l1.length + 3];
								System.arraycopy(l1, 0, w, 0, l1.length);
								w[l1.length] = n;
								w[l1.length + 1] = rwst.sentenceId + "";
								w[l1.length + 2] = sb.toString();
								dw.write(w);
							}
						}
					}
				}
			}
		}
		dr.close();
	}

	/**List<String[]> every line, wid1, wid2 ... (comments, can be any long)*/
	public static void widpair2feature(List<String[]> pairs, int K, String output) {
		HashSet<Integer> used = new HashSet<Integer>();
		String output_stanfordsubset = output + ".stanfordsubset";
		for (String[] l : pairs) {
			used.add(Integer.parseInt(l[0]));
			used.add(Integer.parseInt(l[1]));
		}
		getSubsetStanford(used, output_stanfordsubset);
		featurize(pairs, K, output_stanfordsubset, output);
	}

	/**S1_pair2feature
	 * featurize the pair*/
	private static void featurize(List<String[]> pairs, int K, String input_stanfordsubset, String output) {
		try {
			HashMap<Integer, List<String>> wid2names = new HashMap<Integer, List<String>>();
			DelimitedWriter dw = new DelimitedWriter(output);
			HashSet<String> patternsTakeintoConsider = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
				String[] l;
				while ((l = dr.read()) != null) {
					int wid = Integer.parseInt(l[3]);
					String[] s = l[4].split(" ");
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
			featurize_help(0, pairs, input_stanfordsubset, wid2names, patternsTakeintoConsider, K, dw);
			featurize_help(1, pairs, input_stanfordsubset, wid2names, patternsTakeintoConsider, K, dw);
			dw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block`
			e.printStackTrace();
		}
	}

	//0 : wid1
	//1: wid2
	private static void featurize_help(int wid1Orwid2, List<String[]> widpair, String file_wikipedia,
			HashMap<Integer, List<String>> wid2names, HashSet<String> patternsTakeintoConsider, int K,
			DelimitedWriter dw) throws Exception {
		D.p("featurize", wid1Orwid2);
		//List<String[]> widpair = (new DelimitedReader(file_widpair)).readAll();
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
				//D.p(wid);
				for (String[] l1 : b1) {
					int anotherwid = Integer.parseInt(l1[1 - wid1Orwid2]);
					if (wid == anotherwid)
						break;
					List<String> wid2name = wid2names.get(anotherwid);
					if (wid2name == null) {
						System.err.println("Missing\t" + l1[1]);
						continue;
					}
					for (int k = 0; k < K && k < list_rwst.size(); k++) {
						//D.p(l1,l2);
						RecordWpSenToken rwst = list_rwst.get(k);
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
								String[] w = new String[l1.length + 3];
								System.arraycopy(l1, 0, w, 0, l1.length);
								w[l1.length] = n;
								w[l1.length + 1] = rwst.sentenceId + "";
								w[l1.length + 2] = sb.toString();
								dw.write(w);
							}
						}
					}
				}
			}
		}
		dr.close();
	}

	/**I want a background frequency of the pattern in WK articles*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//doit2();

		/**sort it*/
		//uniqc();

		filterLessThanK(10);

	}

}
