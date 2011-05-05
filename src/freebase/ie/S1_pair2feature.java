package freebase.ie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import percept.main.LogLinear;

import freebase.typematch.RecordWpSenToken;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;
import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

public class S1_pair2feature {

	//nellOrFb relname isNegSeed arg1Wid arg2Wid
	static void step0_getIdInput() {
		try {
			NellOntology no = new NellOntology();
			DelimitedWriter dw = new DelimitedWriter(Main.file_instance_widpair);
			HashMap<String, Integer> nellstr2wid = new HashMap<String, Integer>();
			List<String[]> nellstrMidPred = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_cleanno1))
					.readAll();
			for (String[] a : nellstrMidPred) {
				nellstr2wid.put(a[3], Integer.parseInt(a[2]));
			}
			D.p(nellstr2wid.size());
			List<String[]> towrite = new ArrayList<String[]>();
			for (NellRelation nr : no.nellRelationList) {
				for (String[] a : nr.seedInstances) {
					if (nellstr2wid.containsKey(a[0]) && nellstr2wid.containsKey(a[1])) {
						int wid1 = nellstr2wid.get(a[0]);
						int wid2 = nellstr2wid.get(a[1]);
						towrite.add(new String[] { "" + wid1, "" + wid2, nr.relation_name, "nell_pos" });
						//dw.write(wid1, wid2, nr.relation_name, "nell_pos");
					}
				}
				for (String[] a : nr.known_negatives) {
					if (nellstr2wid.containsKey(a[0]) && nellstr2wid.containsKey(a[1])) {
						int wid1 = nellstr2wid.get(a[0]);
						int wid2 = nellstr2wid.get(a[1]);
						towrite.add(new String[] { wid1 + "", wid2 + "", nr.relation_name, "nell_neg" });
						//dw.write(wid1, wid2, nr.relation_name, "nell_neg");
					}
				}
			}

			/**Put query result pairs into consideration*/
			{
				HashMap<Integer, Integer> gnid2wid = new HashMap<Integer, Integer>();
				{
					DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_gnid_mid_wid_title);
					String[] l;
					while ((l = dr.read()) != null) {
						int guid = Integer.parseInt(l[0]);
						int wid = Integer.parseInt(l[2]);
						gnid2wid.put(guid, wid);
					}
					dr.close();
				}
				HashSet<String> used = new HashSet<String>();
				{
					List<String[]> predict_relmatch = (new DelimitedReader(freebase.relmatch.Main.file_predict)).readAll();
					for (String[] a : predict_relmatch) {
						String[] s = a[0].split("::");
						used.add(s[2]);
					}
				}
				DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_queryresult_sample);
				String[] l;
				while ((l = dr.read()) != null) {
					if (!l[0].equals(l[1])) {
						if (used.contains(l[4])) {
							int guid1 = Integer.parseInt(l[0]);
							int guid2 = Integer.parseInt(l[1]);
							int wid1 = gnid2wid.get(guid1);
							int wid2 = gnid2wid.get(guid2);
							towrite.add(new String[] { wid1 + "", wid2 + "", l[4], "fbcand_" + l[2] });
						}
					}
					//dw.write(l[0],l[1],l[4],"fbcand_"+l[2]);
				}
				dr.close();
			}
			StringTable.sortByIntColumn(towrite, new int[] { 0, 1 });
			for (String[] a : towrite) {
				dw.write(a);
			}
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void ste1_sample() {
		try {
			HashSet<Integer> used = new HashSet<Integer>();
			{
				DelimitedReader dr1 = new DelimitedReader(Main.file_instance_widpair);
				String[] l;

				while ((l = dr1.read()) != null) {
					used.add(Integer.parseInt(l[0]));
					used.add(Integer.parseInt(l[1]));
				}
				dr1.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_wp_stanford);
				DelimitedWriter dw = new DelimitedWriter(Main.file_wiki_sample);
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
			DelimitedWriter dw = new DelimitedWriter(Main.file_instance_featurize);

			HashSet<String> patternsTakeintoConsider = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fout_mid_wid_title);
				String[] l;
				while ((l = dr.read()) != null) {
					int wid = Integer.parseInt(l[1]);
					String[] s = l[2].split(" ");
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
			featurize_help(0, wid2names, patternsTakeintoConsider, dw);
			featurize_help(1, wid2names, patternsTakeintoConsider, dw);
			//			List<String[]> widpair = (new DelimitedReader(Main.file_instance_widpair)).readAll();
			//			DelimitedReader dr2 = new DelimitedReader(Main.file_wiki_sample);
			//			List<RecordWpSenToken> list_rwst = RecordWpSenToken.readByArticleId(dr2, true);
			//			List<List<String[]>> widblocks = StringTable.toblock(widpair, 0);
			//			int totalbad = 0, totalgood = 0;
			//			for (List<String[]> b1 : widblocks) {
			//				int wid1 = Integer.parseInt(b1.get(0)[0]);
			//				if (list_rwst == null)
			//					break;
			//				while (list_rwst.get(0).articleId < wid1 && (list_rwst = RecordWpSenToken.readByArticleId(dr2, false)) != null) {
			//					//wait;
			//				}
			//				if (list_rwst.get(0).articleId == wid1) {
			//					//iterate over b1 and b2
			//					for (String[] l1 : b1) {
			//
			//						List<String> wid2name = wid2names.get(Integer.parseInt(l1[1]));
			//						if (wid2name == null) {
			//							System.err.println("Missing\t" + l1[1]);
			//							totalbad++;
			//							continue;
			//						}
			//						totalgood++;
			//						for (RecordWpSenToken rwst : list_rwst) {
			//							//D.p(l1,l2);
			//							List<String> wl2 = new ArrayList<String>();
			//							{
			//								for (String t : rwst.token) {
			//									wl2.add(t);
			//								}
			//							}
			//							for (String n : wid2name) {
			//								List<String> wl1 = new ArrayList<String>();
			//
			//								{
			//									String[] s = n.split("_");
			//									for (String s0 : s)
			//										wl1.add(s0);
			//								}
			//								int same = StringUtil.numOfShareWords(wl1, wl2, new boolean[] { true, true, true });
			//								if (same == wl1.size()) {
			//									StringBuilder sb = new StringBuilder();
			//									//all words fo the name are finding something in the sentence
			//									Set<String> patterns = new HashSet<String>();
			//									S0_background.patternize(rwst.token, rwst.pos, rwst.ner, patterns, 3);
			//									for (String p : patterns) {
			//										if (patternsTakeintoConsider.contains(p)) {
			//											sb.append(p + " ");
			//										}
			//									}
			//									dw.write(l1[0], l1[1], l1[2], l1[3], n, sb.toString());
			//									//D.p(l1, n, rwst.text);
			//								}
			//							}
			//						}
			//					}
			//					//D.p(wid1);
			//					//get the sentences of that wid1, the idea is, if the sentence contains the arg2, then featurize this sentence~~~
			//					//do something!!!
			//				}
			//			}
			//D.p("Total good and bad", totalbad, totalgood);
			dw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block`
			e.printStackTrace();
		}
	}

	//0 : wid1
	//1: wid2
	private static void featurize_help(int wid1Orwid2,
			HashMap<Integer, List<String>> wid2names,
			HashSet<String> patternsTakeintoConsider,
			DelimitedWriter dw) throws Exception {
		List<String[]> widpair = (new DelimitedReader(Main.file_instance_widpair)).readAll();
		StringTable.sortByIntColumn(widpair, new int[] { wid1Orwid2 });
		List<List<String[]>> widblocks = StringTable.toblock(widpair, wid1Orwid2);
		DelimitedReader dr = new DelimitedReader(Main.file_wiki_sample);
		List<RecordWpSenToken> list_rwst = RecordWpSenToken.readByArticleId(dr, true);
		for (List<String[]> b1 : widblocks) {
			int wid = Integer.parseInt(b1.get(0)[wid1Orwid2]);
			if (list_rwst == null)
				break;
			while (list_rwst.get(0).articleId < wid && (list_rwst = RecordWpSenToken.readByArticleId(dr, false)) != null) {
				//wait;
			}
			if (list_rwst.get(0).articleId == wid) {
				//iterate over b1 and b2
				for (String[] l1 : b1) {
					List<String> wid2name = wid2names.get(Integer.parseInt(l1[1 - wid1Orwid2]));
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
								S0_background.patternize(rwst.token, rwst.pos, rwst.ner, patterns, 3);
								for (String p : patterns) {
									if (patternsTakeintoConsider.contains(p)) {
										sb.append(p + " ");
									}
								}
								dw.write(l1[0], l1[1], l1[2], l1[3], n, rwst.sentenceId, sb.toString());
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

	public static void main(String[] args) {
		step0_getIdInput();
		ste1_sample();
		featurize();

	}
}
