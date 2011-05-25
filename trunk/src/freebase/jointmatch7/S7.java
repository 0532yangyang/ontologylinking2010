package freebase.jointmatch7;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.PbReader;
import javatools.ml.rphacl2011extractor.RphExtractorWrapper;
import javatools.mydb.StringTable;
import multir.preprocess.RelationECML;
import multir.util.delimited.Sort;

public class S7 {
	//	static void s0_give_article_id() throws IOException {
	//		{
	//			String dir = "/projects/pardosa/s5/clzhang/ontologylink/wikidump/";
	//			String output = "/projects/pardosa/s5/clzhang/ontologylink/wikidump/sid_wid_tokens_pos";
	//			DelimitedReader dr = new DelimitedReader(dir + "/wexsections_wid_sectionid_title_section");
	//
	//			DelimitedReader rpos = new DelimitedReader(dir + "/sentences.pos");
	//			DelimitedWriter wpos = new DelimitedWriter(dir + "/sentences.pos.wid");
	//			DelimitedReader rner = new DelimitedReader(dir + "/sentences.ner");
	//			DelimitedWriter wner = new DelimitedWriter(dir + "/sentences.ner.wid");
	//			DelimitedReader rdeps = new DelimitedReader(dir + "/sentences.deps");
	//			DelimitedWriter wdeps = new DelimitedWriter(dir + "/sentences.deps.wid");
	//			int[] sid2wid = new int[25000000];
	//			{
	//				String[] l;
	//				int largestSid = 0;
	//				while ((l = dr.read()) != null) {
	//					int wid = Integer.parseInt(l[0]);
	//					int sid = Integer.parseInt(l[1]);
	//					sid2wid[sid] = wid;
	//					if (sid > largestSid) {
	//						largestSid = sid;
	//					}
	//				}
	//				D.p(largestSid);
	//			}
	//			s0_give_article_id_help(sid2wid, dir + "/sentences.tokens");
	//			s0_give_article_id_help(sid2wid, dir + "/sentences.pos");
	//			s0_give_article_id_help(sid2wid, dir + "/sentences.ner");
	//			s0_give_article_id_help(sid2wid, dir + "/sentences.deps");
	//
	//			wpos.close();
	//			wner.close();
	//			wdeps.close();
	//		}
	//	}
	//
	//	static void s0_give_article_id_help(int[] sid2wid, String in) throws IOException {
	//		{
	//			DelimitedReader rtoken = new DelimitedReader(in);
	//			DelimitedWriter wtoken = new DelimitedWriter(in + ".wid");
	//			String[] l;
	//			while ((l = rtoken.read()) != null) {
	//				int sid = Integer.parseInt(l[0]);
	//				int wid = sid2wid[sid];
	//				String[] w = new String[l.length + 1];
	//				System.arraycopy(l, 0, w, 0, l.length);
	//				w[l.length] = wid + "";
	//				wtoken.write(w);
	//			}
	//			wtoken.close();
	//		}
	//	}

	public static void splitFeaturizedpbToTrainTest(String in, String out) throws IOException {
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		OutputStream ostrain = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out + ".train")));
		OutputStream ostest = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out + ".test")));
		Relation r = null;

		Random ran = new Random();
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			Builder relBuilder1 = Relation.newBuilder();
			Builder relBuilder2 = Relation.newBuilder();

			relBuilder1.setRelType(r.getRelType());
			relBuilder1.setSourceGuid(r.getSourceGuid());
			relBuilder1.setDestGuid(r.getDestGuid());
			relBuilder2.setRelType(r.getRelType());
			relBuilder2.setSourceGuid(r.getSourceGuid());
			relBuilder2.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				//int filename =
				double a = ran.nextDouble();
				if (a > 0.7) {//test 30%
					relBuilder2.addMention(rmf);
				} else {
					relBuilder1.addMention(rmf);
				}
			}
			if (relBuilder1.getMentionList() != null && relBuilder1.getMentionCount() > 0) {
				relBuilder1.build().writeDelimitedTo(ostrain);
			}
			if (relBuilder2.getMentionList() != null && relBuilder2.getMentionCount() > 0) {
				relBuilder2.build().writeDelimitedTo(ostest);
			}
		}
		ostrain.close();
		ostest.close();
	}

	static void getpositivepairs(String pairfile, String file_matches_uniqpairs, String output_positive_pairs)
			throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output_positive_pairs);
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[3]);
				String[] names = l[4].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		HashMap<Integer, List<String>> arg1ToRel = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> arg2ToRel = new HashMap<Integer, List<String>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(pairfile)));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				int arg1 = Integer.parseInt(c[0]);
				int arg2 = Integer.parseInt(c[1]);
				c[2] = c[2].replace("_inverse", "");
				String rel = c[0] + "\t" + c[1] + "\t" + c[2];
				List<String> l1 = arg1ToRel.get(arg1);
				if (l1 == null) {
					l1 = new ArrayList<String>(1);
					arg1ToRel.put(arg1, l1);
				}
				l1.add(rel);
				List<String> l2 = arg2ToRel.get(arg2);
				if (l2 == null) {
					l2 = new ArrayList<String>(1);
					arg2ToRel.put(arg2, l2);
				}
				l2.add(rel);
			}
			r.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(file_matches_uniqpairs);
			String[] l;
			while ((l = dr.read()) != null) {
				String arg1 = l[0].toLowerCase().trim().replaceAll(" ", "_");
				String arg2 = l[1].toLowerCase().trim().replaceAll(" ", "_");
				if (name2wid.containsKey(arg1) && name2wid.containsKey(arg2)) {
					int wid1 = name2wid.get(arg1);
					int wid2 = name2wid.get(arg2);
					List<String> arg1rels = arg1ToRel.get(wid1);
					List<String> arg2rels = arg2ToRel.get(wid2);
					if (arg1rels == null || arg2rels == null) {
						continue;
					}
					arg1rels.retainAll(arg2rels);

					HashSet<String> relTypes = new HashSet<String>();
					for (String rel : arg1rels) {
						String[] c = rel.split("\t");
						relTypes.add(c[2]);
					}
					StringBuilder sb = new StringBuilder();
					for (String rt : relTypes) {
						if (sb.length() > 1)
							sb.append(",");
						sb.append(rt);
					}
					if (wid1 != wid2 && sb.length() > 0) {
						dw.write(l[0], l[1], wid1, wid2, sb);
					}
				}
			}
			dr.close();
		}
		dw.close();
	}

	static void getSectionsContainingSomeFact(String seedfile, String pairfile, String sentencener,
			String output_wid_sid_contain_relation) throws IOException {
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_goldmapping);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				relationsToConsider.add(l[0]);
		//			}
		//		}
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[3]);
				String[] names = l[4].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		HashMap<String, String> facts = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(pairfile);
			String[] l;
			while ((l = dr.read()) != null) {
				int c1 = Integer.parseInt(l[0]);
				int c2 = Integer.parseInt(l[1]);
				String rel = l[2];
				//				if (!relationsToConsider.contains(rel)) {
				//					continue;
				//				}
				if (!rel.contains("_inverse")) {
					facts.put(c1 + "\t" + c2, rel);
				} else {
					rel = rel.replace("_inverse", "");
					facts.put(c2 + "\t" + c1, rel);
				}
			}
		}
		HashMap<String, String> seedfacts = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(seedfile);
			String[] l;
			while ((l = dr.read()) != null) {
				String c1 = l[0];
				String c2 = l[1];
				String rel = l[2];
				//				if (!relationsToConsider.contains(rel)) {
				//					continue;
				//				}
				seedfacts.put(c1 + "\t" + c2, rel);
			}
		}
		D.p("facts", facts.size());
		D.p("seed facts", seedfacts.size());
		DelimitedReader dr = new DelimitedReader(sentencener);
		List<String[]> b;
		DelimitedWriter dw = new DelimitedWriter(output_wid_sid_contain_relation);
		int count = 0;
		while ((b = dr.readBlock(0)) != null) {
			//			if (count++ % 100000 == 0) {
			//				D.p(count, worthconsiderwid.size());
			//			}
			int sid = Integer.parseInt(b.get(0)[0]);
			int sectionId = Integer.parseInt(b.get(0)[2]);
			for (int i = 0; i < b.size(); i++) {
				for (int j = i + 1; j < b.size(); j++) {
					String arg1 = b.get(i)[5]; //is it really 4?
					String arg2 = b.get(j)[5];
					String arg1check = arg1.replaceAll(" ", "_").toLowerCase();
					String arg2check = arg2.replaceAll(" ", "_").toLowerCase();
					if (name2wid.containsKey(arg1check) && name2wid.containsKey(arg2check)) {
						int c1 = name2wid.get(arg1check);
						int c2 = name2wid.get(arg2check);
						//sid, wid, arg1, arg2, arg1pos[0], arg1pos[1], arg2pos[0],arg2pos[1],arg1ner, arg2ner
						if (facts.containsKey(c1 + "\t" + c2)) {
							String rel = facts.get(c1 + "\t" + c2);
							dw.write(sid, sectionId, rel, arg1, arg2, b.get(i)[3], b.get(i)[4], b.get(j)[3],
									b.get(j)[4], b.get(i)[6], b.get(j)[6], "e");
						}
						if (facts.containsKey(c2 + "\t" + c1)) {
							String rel = facts.get(c2 + "\t" + c1);
							dw.write(sid, sectionId, rel, arg2, arg1, b.get(j)[3], b.get(j)[4], b.get(i)[3],
									b.get(i)[4], b.get(j)[6], b.get(i)[6], "e");
						}
						if (seedfacts.containsKey(arg1 + "\t" + arg2)) {
							String rel = seedfacts.get(arg1 + "\t" + arg2);
							dw.write(sid, sectionId, rel, arg1, arg2, b.get(i)[3], b.get(i)[4], b.get(j)[3],
									b.get(j)[4], b.get(i)[6], b.get(j)[6], "s");
						}
						if (seedfacts.containsKey(arg2 + "\t" + arg1)) {
							String rel = seedfacts.get(arg2 + "\t" + arg1);
							dw.write(sid, sectionId, rel, arg2, arg1, b.get(j)[3], b.get(j)[4], b.get(i)[3],
									b.get(i)[4], b.get(j)[6], b.get(i)[6], "s");
						}
					}
				}
			}
		}
		dw.close();
	}

	//
	//	static void getArticlesContainingSomeFact2(String file_positive_pairs, String sentencener,
	//			String output_sentenceid_contain_relation) throws IOException {
	//		HashMap<String, String[]> argpair_relation = new HashMap<String, String[]>();
	//		{
	//			List<String[]> all = (new DelimitedReader(file_positive_pairs)).readAll();
	//			for (String[] a : all) {
	//				argpair_relation.put(a[0] + "\t" + a[1], a);
	//			}
	//		}
	//		{
	//			DelimitedWriter dwdebug = new DelimitedWriter(output_sentenceid_contain_relation + ".debug");
	//			DelimitedReader dr = new DelimitedReader(sentencener);
	//			List<String[]> b;
	//			HashSet<Integer> worthconsiderwid = new HashSet<Integer>();
	//			int count = 0;
	//			while ((b = dr.readBlock(0)) != null) {
	//				if (count++ % 100000 == 0) {
	//					D.p(count, worthconsiderwid.size());
	//				}
	//				int sid = Integer.parseInt(b.get(0)[0]);
	//				int wid = Integer.parseInt(b.get(0)[1]);
	//				for (int i = 0; i < b.size(); i++) {
	//					for (int j = i + 1; j < b.size(); j++) {
	//						String arg1 = b.get(i)[4];
	//						String arg2 = b.get(j)[4];
	//						if (argpair_relation.containsKey(arg1 + "\t" + arg2)
	//								|| argpair_relation.containsKey(arg2 + "\t" + arg1)) {
	//							worthconsiderwid.add(wid);
	//							dwdebug.write(wid, arg1, arg2);
	//							//							if (worthconsidersid.size() > 100)
	//							//								break;
	//						}
	//					}
	//				}
	//			}
	//			dwdebug.close();
	//			DelimitedWriter dw = new DelimitedWriter(output_sentenceid_contain_relation);
	//			for (int x : worthconsiderwid) {
	//				dw.write(x);
	//			}
	//			dw.close();
	//		}
	//
	//	}

	static void getSectionStuff(String file_sid_secid_contain_relation, String in, String out) throws IOException {
		HashSet<Integer> usedsectionId = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(file_sid_secid_contain_relation);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[1]);
				usedsectionId.add(wid);
			}
		}
		getSectionStuff_help(usedsectionId, in + ".tokens", out + ".tokens");
		getSectionStuff_help(usedsectionId, in + ".ner", out + ".ner");
		getSectionStuff_help(usedsectionId, in + ".pos", out + ".pos");
		getSectionStuff_help(usedsectionId, in + ".deps", out + ".deps");
	}

	static void getSectionStuff_help(HashSet<Integer> usedwid, String in, String out) throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(in);
			DelimitedWriter dw = new DelimitedWriter(out);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				if (usedwid.contains(wid)) {
					dw.write(l);
				}
			}
			dw.close();
		}
	}

	static void createAllPairToConsider(String nerfile, String factpair_mention, String output_pairs)
			throws IOException {
		HashMap<String, String> pair2fact = new HashMap<String, String>();
		HashMap<String, String> pair2fact_seed = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(factpair_mention);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[11].equals("s")) {
					pair2fact_seed.put(l[3] + "\t" + l[4], l[2]);
				} else {
					pair2fact.put(l[3] + "\t" + l[4], l[2]);
				}
			}
			dr.close();
		}
		{
			String[] ner2consider = new String[] { "PERSON", "LOCATION", "ORGANIZATION", "MISC" };
			HashSet<String> ner2consider_set = new HashSet<String>();
			for (String a : ner2consider) {
				ner2consider_set.add(a);
			}

			DelimitedReader dr = new DelimitedReader(nerfile);
			DelimitedWriter dw = new DelimitedWriter(output_pairs);
			List<String[]> b;
			int possize = 0, allsize = 0;
			while ((b = dr.readBlock(0)) != null) {
				int sid = Integer.parseInt(b.get(0)[0]);
				int wid = Integer.parseInt(b.get(0)[1]);
				for (int i = 0; i < b.size(); i++) {
					String[] l1 = b.get(i);
					String arg1 = l1[5];
					String arg1ner = l1[6];
					// I will consider arg2 arg1 when j> i
					for (int j = 0; j < b.size(); j++) {
						String[] l2 = b.get(j);
						String arg2 = l2[5];
						String arg2ner = l2[6];
						if (i != j && !arg1.equals(arg2) && ner2consider_set.contains(arg1ner)
								&& ner2consider_set.contains(arg2ner)) {
							//sid, wid, rel,arg1, arg2, arg1pos[0], arg1pos[1], arg2pos[0],arg2pos[1],arg1ner, arg2ner
							{
								String fact12 = "NA";
								if (pair2fact.containsKey(arg1 + "\t" + arg2)) {
									fact12 = pair2fact.get(arg1 + "\t" + arg2);
									dw.write(sid, wid, fact12, arg1, arg2, l1[3], l1[4], l2[3], l2[4], arg1ner,
											arg2ner, "e");
									possize++;
								} else if (pair2fact_seed.containsKey(arg1 + "\t" + arg2)) {
									fact12 = pair2fact_seed.get(arg1 + "\t" + arg2);
									dw.write(sid, wid, fact12, arg1, arg2, l1[3], l1[4], l2[3], l2[4], arg1ner,
											arg2ner, "s");
								} else {
									dw.write(sid, wid, fact12, arg1, arg2, l1[3], l1[4], l2[3], l2[4], arg1ner,
											arg2ner, "NA");
								}
								allsize++;
							}
						}
					}
				}
			}
			D.p("createAllPairToConsider", "pos vs all", possize, allsize);
			dw.close();
			dr.close();
		}
	}

	static void cheatCreateAllPairToConsider(String nerfile, String file_wid_sid_contain_relation, String output_pairs)
			throws IOException {
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[3]);
				String[] names = l[4].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		HashMap<String, String> pair2fact = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_wid_sid_contain_relation);
			String[] l;
			while ((l = dr.read()) != null) {
				pair2fact.put(l[3] + "\t" + l[4], l[2]);
			}
			dr.close();
		}
		{
			String[] ner2consider = new String[] { "PERSON", "LOCATION", "ORGANIZATION", "MISC" };
			HashSet<String> ner2consider_set = new HashSet<String>();
			for (String a : ner2consider) {
				ner2consider_set.add(a);
			}

			DelimitedReader dr = new DelimitedReader(nerfile);
			DelimitedWriter dw = new DelimitedWriter(output_pairs);
			List<String[]> b;
			int possize = 0, allsize = 0;
			while ((b = dr.readBlock(0)) != null) {
				int sid = Integer.parseInt(b.get(0)[0]);
				int wid = Integer.parseInt(b.get(0)[1]);
				for (int i = 0; i < b.size(); i++) {
					String[] l1 = b.get(i);
					String arg1 = l1[5];
					String arg1ner = l1[6];
					// I will consider arg2 arg1 when j> i
					for (int j = 0; j < b.size(); j++) {
						String[] l2 = b.get(j);
						String arg2 = l2[5];
						String arg2ner = l2[6];
						if (name2wid.containsKey(arg1.replace(" ", "_"))
								&& name2wid.containsKey(arg2.replace(" ", "_"))) {
							if (i != j && !arg1.equals(arg2) && ner2consider_set.contains(arg1ner)
									&& ner2consider_set.contains(arg2ner)) {
								//sid, wid, rel,arg1, arg2, arg1pos[0], arg1pos[1], arg2pos[0],arg2pos[1],arg1ner, arg2ner
								{
									String fact12 = "NA";
									if (pair2fact.containsKey(arg1 + "\t" + arg2)) {
										fact12 = pair2fact.get(arg1 + "\t" + arg2);
										possize++;
									}
									dw.write(sid, wid, fact12, arg1, arg2, l1[3], l1[4], l2[3], l2[4], arg1ner, arg2ner);
									allsize++;
								}
							}
						}
					}
				}
			}
			D.p("createAllPairToConsider", "pos vs all", possize, allsize);
			dw.close();
			dr.close();
		}
	}

	static void eval(String file_pairmention) throws IOException {
		String file_uniqpair_label_cnt = file_pairmention + ".uniqpair_label_cnt";
		String file_cnt_negsize_possize = file_pairmention + ".cnt_negsize_possize";
		//uniq pairs and their label
		//eval_getUniqParis2CountLabel(file_pairmention, file_uniqpair_label_cnt);
		/**my hope is, those NA appears very few*/
		//eval_is_napair_cnt_is_low(file_uniqpair_label_cnt, file_cnt_negsize_possize);
		/**what is the negative ratio of Seb's data?*/
		eval_neg_ratio_seb();
		eval_fact_mention_uniqpairs(file_pairmention);

	}

	static void eval_fact_mention_uniqpairs(String file_factpair_mentions) throws IOException {
		HashSet<String> all = new HashSet<String>();
		HashSet<String> positive = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(file_factpair_mentions);
			String[] l;
			while ((l = dr.read()) != null) {
				String key = l[3] + "\t" + l[4];
				all.add(key);
				if (!l[2].equals("NA")) {
					positive.add(key);
				}
			}
		}
		D.p("number of uniq pairs", all.size());
		D.p("number of positive uniq pairs", positive.size());
	}

	static void getUniqParis2CountLabel(String file_pairmention, String file_uniqpair_label_cnt) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_uniqpair_label_cnt);
		HashMap<String, String> pair2label = new HashMap<String, String>(10000000);
		HashCount<String> paircount = new HashCount<String>();
		{
			DelimitedReader dr = new DelimitedReader(file_pairmention);
			String[] l;

			while ((l = dr.read()) != null) {
				String pair = l[3] + "\t" + l[4] + "\t" + l[11];
				String label = l[2];
				pair2label.put(pair, label);
				paircount.add(pair);
			}
			dr.close();
		}
		{
			for (Entry<String, String> e : pair2label.entrySet()) {
				String pair = e.getKey();
				String[] arg12 = pair.split("\t");
				String label = e.getValue();
				int cnt = paircount.see(pair);
				dw.write(arg12[0], arg12[1], label, cnt, arg12[2]);
			}
		}
		dw.close();
	}

	static void eval_is_napair_cnt_is_low(String file_uniqpair_label_cnt, String file_cnt_negsize_possize)
			throws IOException {
		HashMap<Integer, int[]> cnt_posVSneg = new HashMap<Integer, int[]>();
		{
			DelimitedReader dr = new DelimitedReader(file_uniqpair_label_cnt);
			String[] l;
			while ((l = dr.read()) != null) {
				int cnt = Integer.parseInt(l[3]);
				if (!cnt_posVSneg.containsKey(cnt)) {
					cnt_posVSneg.put(cnt, new int[] { 0, 0 });
				}
				String label = l[2];
				int[] xy = cnt_posVSneg.get(cnt);
				if (label.equals("NA")) {
					xy[0]++;
				} else {
					xy[1]++;
				}
			}
		}
		List<String[]> towrite = new ArrayList<String[]>();
		{
			for (Entry<Integer, int[]> e : cnt_posVSneg.entrySet()) {
				int cnt = e.getKey();
				int[] xy = e.getValue();
				towrite.add(new String[] { cnt + "", xy[0] + "", xy[1] + "" });
			}
			DelimitedWriter dw = new DelimitedWriter(file_cnt_negsize_possize);
			for (String[] w : towrite) {
				dw.write(w);
			}
			dw.close();
		}
	}

	static void eval_neg_ratio_seb() throws IOException {
		String in = "/projects/pardosa/s5/clzhang/ontologylink/jointmatch2/nyt/extend_vs_extend_0507/testpb";
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;

		int count = 0;
		int negcount = 0, poscount = 0;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {

			// need to iterate over mentions, keep only those in the range

			String rel = r.getRelType();
			if (rel.equals("NA")) {
				negcount++;
			} else {
				poscount++;
			}
		}
		D.p("neg vs pos", negcount, poscount);
		is.close();
	}

	public static void splitPairmention2traintest(String file_uniqpair_label_cnt, String file_pairmentions,
			boolean takeExtended, double train_neg_ratio, int traintotal, int testtotal, String output)
			throws IOException {
		HashSet<String> testpairs = new HashSet<String>();
		HashSet<String> trainpairs = new HashSet<String>();
		DelimitedWriter wtrain = new DelimitedWriter(output + ".train");
		DelimitedWriter wtest = new DelimitedWriter(output + ".test");
		{
			List<String[]> all = new ArrayList<String[]>(10000000);
			DelimitedReader dr = new DelimitedReader(file_uniqpair_label_cnt);
			String[] l;
			//Mashriqi	Oudh	NA	1
			int allnegsize = 0;
			while ((l = dr.read()) != null) {
				int cnt = Integer.parseInt(l[3]);
				if (l[4].equals("s")) {
					trainpairs.add(l[0] + "\t" + l[1]);
				} else if (cnt > 1) {
					all.add(l);
					if (l[2].equals("NA")) {
						allnegsize++;
					}
				}
			}
			D.p("all size", all.size());
			D.p("all neg size", allnegsize);
			Collections.shuffle(all);
			int s = 0;
			/**for the training, I keep at most traintotal pairs; the negative data is at most train_neg_ratio */
			int trainnegsize = 0;
			for (; s < all.size() * 0.7 && trainpairs.size() < traintotal; s++) {
				l = all.get(s);
				String rel = l[2];
				if (rel.equals("NA")) {
					if (trainnegsize * 1.0 / trainpairs.size() < train_neg_ratio) {
						trainnegsize++;
						trainpairs.add(l[0] + "\t" + l[1]);
					}
				} else {
					if (takeExtended) {
						trainpairs.add(l[0] + "\t" + l[1]);
					}
				}
			}
			/**for testing, I keep at most testtotal pairs*/
			int testnegsize = 0;
			for (; s < all.size() && testpairs.size() < testtotal; s++) {
				l = all.get(s);
				testpairs.add(l[0] + "\t" + l[1]);
				if (l[2].equals("NA")) {
					testnegsize++;
				}
			}
			D.p("training negative", trainnegsize);
			D.p("training pairs", trainpairs.size());
			D.p("test pairs", testpairs.size());
			D.p("test negative", testnegsize);
		}
		{
			DelimitedReader dr = new DelimitedReader(file_pairmentions);
			String[] l;
			//223	24083124	NA	Aashirvad Cinemas	Antony Perumbavoor	0	2	9	11	LOCATION	PERSON
			while ((l = dr.read()) != null) {
				String pair = l[3] + "\t" + l[4];
				if (testpairs.contains(pair)) {
					wtest.write(l);
				} else if (trainpairs.contains(pair)) {
					wtrain.write(l);
				}
			}
		}
		wtrain.close();
		wtest.close();
	}

	public static void splitPairmention2traintest(String file_uniqpair_label_cnt, String file_pairmentions,
			double train_neg_ratio, int traintotal, int testtotal) throws IOException {
		HashSet<String> testpairs = new HashSet<String>();
		HashSet<String> trainpairs = new HashSet<String>();
		DelimitedWriter wtrain = new DelimitedWriter(file_pairmentions + ".train");
		DelimitedWriter wtest = new DelimitedWriter(file_pairmentions + ".test");
		{
			List<String[]> all = new ArrayList<String[]>(10000000);
			DelimitedReader dr = new DelimitedReader(file_uniqpair_label_cnt);
			String[] l;
			//Mashriqi	Oudh	NA	1
			int allnegsize = 0;
			while ((l = dr.read()) != null) {
				int cnt = Integer.parseInt(l[3]);
				if (cnt > 0) {
					all.add(l);
					if (l[2].equals("NA")) {
						allnegsize++;
					}
				}
			}
			D.p("all size", all.size());
			D.p("all neg size", allnegsize);
			Collections.shuffle(all);
			int s = 0;
			/**for the training, I keep at most traintotal pairs; the negative data is at most train_neg_ratio */
			int trainnegsize = 0;
			for (; s < all.size() * 0.7 && trainpairs.size() < traintotal; s++) {
				l = all.get(s);
				String rel = l[2];
				if (rel.equals("NA")) {
					if (trainnegsize * 1.0 / trainpairs.size() < train_neg_ratio) {
						trainnegsize++;
						trainpairs.add(l[0] + "\t" + l[1]);
					}
				} else {
					trainpairs.add(l[0] + "\t" + l[1]);
				}
			}
			/**for testing, I keep at most testtotal pairs*/
			int testnegsize = 0;
			for (; s < all.size() && testpairs.size() < testtotal; s++) {
				l = all.get(s);
				testpairs.add(l[0] + "\t" + l[1]);
				if (l[2].equals("NA")) {
					testnegsize++;
				}
			}
			D.p("training negative", trainnegsize);
			D.p("training pairs", trainpairs.size());
			D.p("test pairs", testpairs.size());
			D.p("test negative", testnegsize);
		}
		{
			DelimitedReader dr = new DelimitedReader(file_pairmentions);
			String[] l;
			//223	24083124	NA	Aashirvad Cinemas	Antony Perumbavoor	0	2	9	11	LOCATION	PERSON
			while ((l = dr.read()) != null) {
				String pair = l[3] + "\t" + l[4];
				if (testpairs.contains(pair)) {
					wtest.write(l);
				} else if (trainpairs.contains(pair)) {
					wtrain.write(l);
				}
			}
		}
		wtrain.close();
		wtest.close();
	}

	public static void relabelSql2InstanceUnionByGoldMatching(String file_my_relationmatch,
			String file_gold_relationmatch, String file_sql2instance_union, String file_gold_sql2union)
			throws IOException {
		//first get a selected aftermatch
		DelimitedWriter dw = new DelimitedWriter(file_gold_sql2union);
		HashSet<String> gold = new HashSet<String>();
		{
			List<String[]> all = (new DelimitedReader(file_gold_relationmatch)).readAll();
			for (String[] l : all) {
				if (l[0].equals("1")) {
					gold.add(l[1] + "\t" + l[2]);
				}
			}
		}
		HashMap<Integer, String> urid2nellrel = new HashMap<Integer, String>();
		{
			List<String[]> all = (new DelimitedReader(file_my_relationmatch)).readAll();
			for (String[] l : all) {
				if (gold.contains(l[0] + "\t" + l[3])) {
					urid2nellrel.put(Integer.parseInt(l[1]), l[0]);
				}
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_sql2instance_union);
			String[] l;
			while ((l = dr.read()) != null) {
				int urid = Integer.parseInt(l[4]);
				if (urid2nellrel.containsKey(urid)) {
					dw.write(l[0], l[1], urid2nellrel.get(urid), urid);
				}
			}
			dr.close();
		}
		dw.close();
	}

	public static void relabelTestByGoldMatching(String file_gold_sql2instanceunion,//gold sql2instance
			String test_pairmention_oldlabel,//test pairs with my ontology matching label
			String test_pairmention_gold//test pairs with gold ontology matching label
	) throws IOException {
		HashMap<String, String> goldfacts = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_gold_sql2instanceunion);
			String[] l;
			while ((l = dr.read()) != null) {
				goldfacts.put(l[0] + "\t" + l[1], l[2]);
			}
			dr.close();
		}
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[3]);
				String[] names = l[4].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(test_pairmention_oldlabel);
			DelimitedWriter dw = new DelimitedWriter(test_pairmention_gold);
			DelimitedWriter dwdebug = new DelimitedWriter(test_pairmention_gold + ".debug");
			String[] l;
			while ((l = dr.read()) != null) {
				String oldrel = l[2];
				String name1 = l[3];
				String name2 = l[4];
				Integer id1 = name2wid.get(name1.toLowerCase().replace(" ", "_"));
				Integer id2 = name2wid.get(name2.toLowerCase().replace(" ", "_"));
				if (goldfacts.containsKey(id1 + "\t" + id2)) {
					String newrel = goldfacts.get(id1 + "\t" + id2);
					l[2] = newrel;
				} else {
					l[2] = "NA";
				}
				dw.write(l);
				if (!l[2].equals(oldrel)) {
					dwdebug.write("bad", oldrel, l[2], name1, name2);
				} else if (!l[2].equals("NA")) {
					//dwdebug.write("good", oldrel, l[2], name1, name2);
				}
			}
			dr.close();
			dw.close();
			dwdebug.close();
		}

	}

	public static void createpb(String file_sentence, String file_pairmentions, String tempdir, String output)
			throws IOException {

		// iterate over data, keep only if in target

		{
			RelationECML ecml = new RelationECML();

			DelimitedReader isTokens = new DelimitedReader(file_sentence + ".tokens");
			DelimitedReader isPos = new DelimitedReader(file_sentence + ".pos");
			DelimitedReader isDeps = new DelimitedReader(file_sentence + ".deps");
			DelimitedReader isNer = new DelimitedReader(file_sentence + ".ner");

			String[] tTokens = isTokens.read();
			String[] tPos = isPos.read();
			String[] tDeps = isDeps.read();
			String[] tNer = isNer.read();

			DelimitedWriter w = new DelimitedWriter(output);
			//sid, wid, rel,arg1, arg2, arg1pos[0], arg1pos[1], arg2pos[0],arg2pos[1],arg1ner, arg2ner
			DelimitedReader r = new DelimitedReader(file_pairmentions);
			String[] l = null;
			int count = 0;

			//HashMap<String, String> nerm = new HashMap<String, String>();
			int nermSentenceId = -1;

			while ((l = r.read()) != null) {
				int sentenceId = Integer.parseInt(l[0]);
				int wid = Integer.parseInt(l[1]);
				//				if (++count % 1000000 == 0)
				//					System.out.println(count + "\t" + sentenceId);

				//if (sentenceId < startSentenceId ||
				//	sentenceId >= endSentenceId) continue;

				// align tokens, postags, deps
				while (tTokens != null && Integer.parseInt(tTokens[0]) < sentenceId)
					tTokens = isTokens.read();
				while (tPos != null && Integer.parseInt(tPos[0]) < sentenceId)
					tPos = isPos.read();
				while (tDeps != null && Integer.parseInt(tDeps[0]) < sentenceId)
					tDeps = isDeps.read();

				// check if all iterators are aligned
				if (!(Integer.parseInt(tTokens[0]) == sentenceId && Integer.parseInt(tPos[0]) == sentenceId && Integer
						.parseInt(tDeps[0]) == sentenceId)) {
					System.out.println("iterators not aligned: shouldn't happen!");
					System.exit(-1);
				}

				// create features
				int[] arg1Pos = new int[] { Integer.parseInt(l[5]), Integer.parseInt(l[6]) };
				int[] arg2Pos = new int[] { Integer.parseInt(l[7]), Integer.parseInt(l[8]) };

				String rel = l[2];
				String arg1 = l[3];
				String arg2 = l[4];
				String arg1ner = l[9];
				String arg2ner = l[10];

				// convert to arrays
				String[] tokens = tTokens[3].split(" ");
				String[] pos = tPos[3].split(" ");
				String[] depParentsStr = tDeps[4].split(" ");
				String[] depTypes = tDeps[3].split(" ");

				int[] depParents = new int[depParentsStr.length];
				for (int i = 0; i < depParents.length; i++)
					depParents[i] = Integer.parseInt(depParentsStr[i]);

				//System.out.println(iTokenized.tokens.length + " " + iPostags.postags.length + " ");
				if (tokens.length != pos.length) {
					System.out.println(tTokens[3]);
					//System.out.println(iDeps.deps);
					//System.exit(0);
					System.out.println("IGNORING SENTENCE " + sentenceId);
					continue;
				}

				List<String> fts = ecml.getFeatures(sentenceId, tokens, pos, depParents, depTypes, arg1Pos, arg2Pos,
						arg1ner, arg2ner);
				StringBuilder sb = new StringBuilder();
				for (String ft : fts)
					sb.append(ft + "\n");

				// arg1, arg2, 
				w.write(arg1, arg2, rel, sentenceId, wid, sb.toString());
			}
			r.close();
			w.close();

			isTokens.close();
			isPos.close();
			isDeps.close();
			isNer.close();
		}

		// sort by arg1, arg2
		Sort.sort(output, output + ".sorted", tempdir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				return S7.compare(t1, t2);
			}
		});

		// convert to protobuf
		{
			OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(output + ".pb")));

			DelimitedReader r = new DelimitedReader(output + ".sorted");
			Builder relBuilder = null;
			List<String[]> b = null;
			while ((b = r.readBlock(new int[] { 0, 1 })) != null) {
				String arg1 = b.get(0)[0];
				String arg2 = b.get(0)[1];
				String rel = b.get(0)[2];
				relBuilder = Relation.newBuilder();
				relBuilder.setSourceGuid(arg1);
				relBuilder.setDestGuid(arg2);
				relBuilder.setRelType(rel);
				for (String[] s : b) {
					relBuilder.addMention(createMention(s));
				}
				if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
					relBuilder.build().writeDelimitedTo(os);
			}
			r.close();
			os.close();
		}

	}

	private static RelationMentionRef createMention(String[] l) {
		RelationMentionRef.Builder mntBuilder = RelationMentionRef.newBuilder();
		mntBuilder.setFilename(l[3] + "\t" + l[4]);
		mntBuilder.setSourceId(0);
		mntBuilder.setDestId(0);
		String[] fts = l[5].split("\n");
		for (String ft : fts)
			mntBuilder.addFeature(ft);
		return mntBuilder.build();
	}

	private static int compare(String[] a, String[] b) {
		return compare(a[0], a[1], b[0], b[1]);
	}

	private static int compare(String a1, String a2, String b1, String b2) {
		int c1 = a1.compareTo(b1);
		if (c1 != 0)
			return c1;
		int c2 = a2.compareTo(b2);
		return c2;
	}

	//	static HashSet<String> relationsToConsider = new HashSet<String>();

	static void getSeedPairs(String file_seedwidpairs) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_seedwidpairs);
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] s : nr.seedInstances) {
				dw.write(s[0], s[1], nr.relation_name);
			}
		}
		dw.close();
	}

	static void get_relabel_sql2instance(String file_relmatchres,//relation match result
			String file_sql2instance_union,//sql 2 instance union
			String file_relabel_sql2instance) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_relabel_sql2instance);
		HashMap<Integer, String> urid2nellrel = new HashMap<Integer, String>();
		{
			List<String[]> all = (new DelimitedReader(file_relmatchres)).readAll();
			for (String[] l : all) {
				urid2nellrel.put(Integer.parseInt(l[1]), l[0]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_sql2instance_union);
			String[] l;
			while ((l = dr.read()) != null) {
				int urid = Integer.parseInt(l[4]);
				if (urid2nellrel.containsKey(urid)) {
					dw.write(l[0], l[1], urid2nellrel.get(urid), urid);
				}
			}
			dr.close();
		}
		dw.close();
	}

	public static void oneFigure(String dir_globaldata, String dir_local, String name_dataset) throws IOException {

		String file_relation_match = dir_local + "/afterrelmatch_relmatchres";
		String file_sql2instance_union = dir_local + "/sql2instance_union";
		String file_seedwidpairs = dir_local + "/seedwidpairs";
		String file_relabel_sql2instance = dir_local + "/relabel_sql2instance";

		String file_gold_relation_match = Main.dir + "/mycreateontology/gold_relmatchres";
		String file_gold_sql2instance = dir_local + "/gold_sql2instance";

		String dir_localdata = dir_local + "/" + name_dataset;
		(new File(dir_localdata)).mkdir();

		String file_fact_pairmentions = dir_localdata + "/factpair_mentions";
		String file_localsentences = dir_localdata + "/sentences";
		String file_pairmentions = dir_localdata + "/pair_mentions";
		String file_uniqpair_label_cnt = file_pairmentions + ".uniqpair_label_cnt";

		String file_fact = dir_localdata + "/fact";
		String file_trainraw = dir_localdata + "/sampletrain";
		String file_testraw = dir_localdata + "/sampletest";

//		getSeedPairs(file_seedwidpairs);
//		get_relabel_sql2instance(file_relation_match, file_sql2instance_union, file_relabel_sql2instance);
//		relabelSql2InstanceUnionByGoldMatching(file_relation_match, file_gold_relation_match, file_sql2instance_union,
//				file_gold_sql2instance);
//		getSectionsContainingSomeFact(file_seedwidpairs, file_relabel_sql2instance, dir_globaldata + ".ner",
//				file_fact_pairmentions);
//		getSectionStuff(file_fact_pairmentions, dir_globaldata, file_localsentences);
//		createAllPairToConsider(file_localsentences + ".ner", file_fact_pairmentions, file_pairmentions);
//		getUniqParis2CountLabel(file_pairmentions, file_uniqpair_label_cnt);
		{
			/**test on ontological smoothed*/
			String expdir = dir_localdata + "/exp1_ontologicalsmooth";
			(new File(expdir)).mkdir();
			String traintestpairmention = expdir + "/pairmention";
			String trainpb = expdir + "/trainpb";
			String testpb = expdir + "/testpb";
			splitPairmention2traintest(file_uniqpair_label_cnt, file_pairmentions, true, 0.9, 100000, 100000,
					traintestpairmention);
			relabelTestByGoldMatching(file_gold_sql2instance, traintestpairmention + ".test", traintestpairmention
					+ ".goldtest");
			createpb(file_localsentences, traintestpairmention + ".train", expdir, trainpb);
			createpb(file_localsentences, traintestpairmention + ".goldtest", expdir, testpb);
			//			PbReader.analyzePbData(file_trainraw + ".pb");
			//			PbReader.analyzePbData(file_testraw + ".pb");
			RphExtractorWrapper rew = new RphExtractorWrapper(trainpb + ".pb", testpb + ".pb", expdir);
			rew.learningThenTesting();
		}
		{
			/**test on base line*/
			String expdir = dir_localdata + "/exp2_baseline";
			(new File(expdir)).mkdir();
			String traintestpairmention = expdir + "/pairmention";
			String trainpb = expdir + "/trainpb";
			String testpb = expdir + "/testpb";
			splitPairmention2traintest(file_uniqpair_label_cnt, file_pairmentions, false, 0.9, 100000, 100000,
					traintestpairmention);
			relabelTestByGoldMatching(file_gold_sql2instance, traintestpairmention + ".test", traintestpairmention
					+ ".goldtest");
			createpb(file_localsentences, traintestpairmention + ".train", expdir, trainpb);
			createpb(file_localsentences, traintestpairmention + ".test", expdir, testpb);
			RphExtractorWrapper rew = new RphExtractorWrapper(trainpb + ".pb", testpb + ".pb", expdir);
			rew.learningThenTesting();
		}
	}

	public static void main(String[] args) throws IOException {
		//oneFigure(Main.dirwikidump + "/sentences", Main.dir + "/iter1", "wikidata");
		oneFigure(Main.dir_nytdump + "/sentences", Main.dir + "/iter1", "nytdata");
	}

	public static void main2(String[] args) throws IOException {
		//String dirwikidump = "/projects/pardosa/s5/clzhang/ontologylink/wikidump";
		//String dir = "/projects/pardosa/s5/clzhang/ontologylink/jointmatch2/wikisection";

		String dir_wikisection = Main.dir_wikisection;
		(new File(dir_wikisection)).mkdir();

		String file_relmatchres = Main.file_afterrelmatch_relmatchres;

		String file_sql2instance_union = Main.file_sql2instance_union;

		String file_seedwidpairs = dir_wikisection + "/seedwidpairs";
		String file_relabel_sql2instance = dir_wikisection + "/relabel_sql2instance";
		String file_fact_pairmentions = dir_wikisection + "/factpair_mentions";
		String file_localsentences = dir_wikisection + "/sentences";
		String file_pairmentions = dir_wikisection + "/pair_mentions";
		String file_fact = dir_wikisection + "/fact";
		String file_trainraw = dir_wikisection + "/sampletrain";
		String file_testraw = dir_wikisection + "/sampletest";
		String file_uniqpair_label_cnt = file_pairmentions + ".uniqpair_label_cnt";

		getSeedPairs(file_seedwidpairs);
		get_relabel_sql2instance(file_relmatchres, file_sql2instance_union, file_relabel_sql2instance);

		getSectionsContainingSomeFact(file_seedwidpairs, file_relabel_sql2instance, Main.file_globalsentences + ".ner",
				file_fact_pairmentions);
		getSectionStuff(file_fact_pairmentions, Main.file_globalsentences, file_localsentences);
		createAllPairToConsider(Main.dir_wikisection + "/sentences.ner", file_fact_pairmentions, file_pairmentions);
		getUniqParis2CountLabel(file_pairmentions, file_uniqpair_label_cnt);
		{
			/**test on ontological smoothed*/
			String expdir = Main.dir_wikisection + "/exp1_ontologicalsmooth";
			(new File(expdir)).mkdir();
			String traintestpairmention = expdir + "/pairmention";
			String trainpb = expdir + "/trainpb";
			String testpb = expdir + "/testpb";
			splitPairmention2traintest(file_uniqpair_label_cnt, file_pairmentions, true, 0.9, 100000, 100000,
					traintestpairmention);
			createpb(file_localsentences, traintestpairmention + ".train", Main.dir, trainpb);
			createpb(file_localsentences, traintestpairmention + ".test", Main.dir, testpb);
			//			PbReader.analyzePbData(file_trainraw + ".pb");
			//			PbReader.analyzePbData(file_testraw + ".pb");
			RphExtractorWrapper rew = new RphExtractorWrapper(trainpb + ".pb", testpb + ".pb", expdir);
			rew.learningThenTesting();
		}
		{
			/**test on base line*/
			String expdir = Main.dir_wikisection + "/exp2_baseline";
			(new File(expdir)).mkdir();
			String traintestpairmention = expdir + "/pairmention";
			String trainpb = expdir + "/trainpb";
			String testpb = expdir + "/testpb";
			splitPairmention2traintest(file_uniqpair_label_cnt, file_pairmentions, false, 0.9, 100000, 100000,
					traintestpairmention);
			createpb(file_localsentences, traintestpairmention + ".train", Main.dir, trainpb);
			createpb(file_localsentences, traintestpairmention + ".test", Main.dir, testpb);
			RphExtractorWrapper rew = new RphExtractorWrapper(trainpb + ".pb", testpb + ".pb", expdir);
			rew.learningThenTesting();
		}
		{
			/**Test on gold data, i.e. no NA*/
			//		createpb(file_localsentences, file_fact_pairmentions, dir, file_fact);
			//			splitFeaturizedpbToTrainTest(file_fact + ".pb", file_fact + ".pb");
			//			String expdir = dir + "/exp3";
			//			(new File(expdir)).mkdir();
			//			RphExtractorWrapper rew = new RphExtractorWrapper(file_fact + ".pb.train", file_fact + ".pb.test", expdir);
			//			rew.learningThenTesting();
		}
		//eval(file_pairmentions);

	}
}
