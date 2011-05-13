package freebase.jointmatch2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

import stanfordCoreWrapper.CoreNlpPipeline;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.ml.rphacl2011extractor.RphExtractorWrapper;
import javatools.mydb.StringTable;
import javatools.string.RemoveStopwords;
import javatools.string.StringUtil;
import javatools.webapi.BingApi;
import javatools.webapi.LuceneIndexFiles;
import javatools.webapi.LuceneSearch;

public class S122_wikiextractor {
	static String getHashWord(String a) {
		a = a.toLowerCase();
		String[] b = a.split(",| ");
		String longest = b[0];
		for (String b0 : b) {
			if (b0.length() > longest.length()) {
				longest = b0;
			}
		}
		return longest;
	}

	static List<Integer> checkStrInTokens(String str, String[] tokens) {
		String check = "";
		String[] s = str.split(",| ");
		List<Integer> starts = new ArrayList<Integer>();
		for (int i = 0; i < tokens.length; i++) {
			boolean match = true;
			for (int j = 0; j < s.length; j++) {
				if (i + j >= tokens.length) {
					match = false;
					break;
				}
				if (!tokens[i + j].equals(s[j])) {
					match = false;
					break;
				}
			}
			if (match) {
				starts.add(i);
			}
		}
		return starts;
	}

	//	static void getSentencesForSeedPairs() throws IOException {
	//		HashMap<String, String[]> hashword_seedpair = new HashMap<String, String[]>();
	//		HashCount<String> hc = new HashCount<String>();
	//		{
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_seedtext + ".debug");
	//			for (NellRelation nr : Main.no.nellRelationList) {
	//				for (String[] a : nr.seedInstances) {
	//					dw.write(a);
	//					String hashworda0 = getHashWord(a[0]);
	//					String hashworda1 = getHashWord(a[1]);
	//					hashword_seedpair.put(hashworda0, new String[] { a[0], a[1], nr.relation_name });
	//					hashword_seedpair.put(hashworda1, new String[] { a[0], a[1], nr.relation_name });
	//				}
	//			}
	//			dw.close();
	//		}
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_wp_stanford_subset);
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_seedtext);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				if (l[3].contains("Berkshire Hathaway") && l[3].contains("NetJets")) {
	//					int x = 0;
	//				}
	//				String[] tokens = l[3].split(" ");
	//				String[] tlower = new String[tokens.length];
	//				for (int i = 0; i < tokens.length; i++) {
	//					tlower[i] = tokens[i].toLowerCase();
	//				}
	//				for (String t : tlower) {
	//					if (hashword_seedpair.containsKey(t)) {
	//						String args[] = hashword_seedpair.get(t);
	//						List<Integer> arg1pos = checkStrInTokens(args[0].toLowerCase(), tlower);
	//						List<Integer> arg2pos = checkStrInTokens(args[1].toLowerCase(), tlower);
	//
	//						if (arg1pos.size() > 0 && arg2pos.size() > 0) {
	//							StringBuilder sb = new StringBuilder();
	//							for (int x : arg1pos) {
	//								sb.append(x + " ");
	//							}
	//							sb.append("::");
	//							for (int x : arg2pos) {
	//								sb.append(x + " ");
	//							}
	//							sb.append("::");
	//							dw.write(args[0], args[1], args[2], l[1], sb.toString(), l[3], l[4], l[5]);
	//							hc.add(args[2]);
	//							break;
	//						}
	//					}
	//				}
	//			}
	//			dw.close();
	//		}
	//		hc.printAll();
	//		//		for (Entry<String, String[]> e : hashword_seedpair.entrySet()) {
	//		//			String[] x = e.getValue();
	//		//			D.p(e.getKey(), x[0], x[1]);
	//		//		}
	//	}

	public static void indexWikiFileForSearch() {
		//		LuceneIndexFiles.indexDelimitedFile(Main.file_seedtext, 0, 5, Main.file_seedtext + ".luceneindex");
		//		List<String> queries = new ArrayList<String>();
		//		queries.add("Japan");
		//		LuceneIndexFiles.searchDelimitedFile(Main.file_seedtext + ".luceneindex", queries);
		LuceneIndexFiles.indexDelimitedFile(Main.file_wexwikifile, 3, new int[] { 0, 1, 2 },
				Main.file_wexwiki_luceneindex);
	}

	//	public static void searchWikiFile() {
	//		List<String> queries = new ArrayList<String>();
	//		queries.add("Action");
	//		LuceneIndexFiles.searchDelimitedFile(Main.file_wexwiki_luceneindex, queries);
	//	}


	public static void getSeedLuceneResultWholeArticle() throws IOException {
		//DelimitedWriter dw = new DelimitedWriter(Main.file_seed2bing);
		LuceneSearch ls = new LuceneSearch(Main.file_wikiwholearticle_luceneindex);
		DelimitedWriter dw = new DelimitedWriter(Main.file_seed2lucene_article);
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] seed : nr.seedInstances) {
				//D.p(seed[0] + "\t" + seed[1]);
				String query = "\"" + seed[0] + "\" \"" + seed[1] + "\"";
				List<String[]> searchresult = ls.search(query, 5);
				int num = 0;
				for (String[] r : searchresult) {
					String[] ab = r[0].split("\t");
					if (r[1].contains(seed[0]) && r[1].contains(seed[1])) {
						dw.write(0, 0, seed[0], seed[1], nr.relation_name, r[1]);
						if (num++ > 20) {
							break;
						}
					}
				}
				D.p(query, num);
			}
		}
		dw.close();
	}

	public static void getExtendedPairLuceneResult(int samplesize) throws IOException {

		HashMap<Integer, String> wid2name = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wex_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String name = l[1];
				int wid = Integer.parseInt(l[0]);
				wid2name.put(wid, name);
			}
		}
		HashCount<String> hc = new HashCount<String>();
		List<String[]> towrite = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_extendedwidpairs + ".shuffle");
			String[] l;
			//D.p("read extendedwidpairs shuffle");
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);

				String look = l[2] + "=====>" + l[3];
				if (hc.see(look) < samplesize && wid2name.containsKey(wid1) && wid2name.containsKey(wid2)) {
					String n1 = wid2name.get(wid1);
					String n2 = wid2name.get(wid2);
					n1 = StringUtil.removeParentheses(n1);
					n2 = StringUtil.removeParentheses(n2);
					towrite.add(new String[] { wid1 + "", wid2 + "", n1, n2, l[2], l[3] });
					hc.add(look);
				}
			}
			dr.close();
		}
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_extendedpair2lucene + ".preparepairs");
			for (String[] w : towrite) {
				dw.write(w);
			}
			dw.close();
		}
		{
			LuceneSearch ls = new LuceneSearch(Main.file_wexwiki_luceneindex);
			DelimitedWriter dw = new DelimitedWriter(Main.file_extendedpair2lucene);
			D.p("start search:");
			for (String[] w : towrite) {
				String query = "\"" + w[2] + "\" \"" + w[3] + "\"";
				List<String[]> searchresult = ls.search(query, 100);
				int num = 0;
				for (String[] r : searchresult) {
					String[] ab = r[0].split("\t");
					if (r[1].contains(w[2]) && r[1].contains(w[3])) {
						dw.write(w[0], w[1], w[2], w[3], w[4], r[1]);
						if (num++ > 20) {
							break;
						}
					}
				}
				D.p(query, num);
				//				String query = "\"" + w[2] + "\" \"" + w[3] + "\"";
				//				D.p(query);
				//				List<String[]> res = BingApi.getResult(query);
				//				for (String[] r : res) {
				//					String[] s = new String[r.length + w.length];
				//					System.arraycopy(w, 0, s, 0, w.length);
				//					System.arraycopy(r, 0, s, w.length, r.length);
				//					dw.write(s);
				//				}
				//				dw.flush();
			}
			dw.close();
			//Thread.sleep(1000);
		}
	}

	public static void parseArticleByTwoArgs(String article, String arg1, String arg2) {
		int firstArg1Pos = article.indexOf(arg1);
		int firstArg2Pos = article.indexOf(arg2);

	}

	public static void getStanfordParsedResult(String file, int arg1index, int relindex, int senindex)
			throws IOException {
		String output = file + ".stanford";
		CoreNlpPipeline cnp = new CoreNlpPipeline();
		DelimitedWriter dw = new DelimitedWriter(output);
		{
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {

				String arg1PlusArg2 = l[arg1index] + "::" + l[arg1index + 1];
				String relation = l[relindex].replace("_inverse", "");
				String sentence = l[senindex];
				//				String title = l[titleindex].replace("Title:", "");
				//				String description = l[titleindex + 1].replace("Description:", "");
				//String toparse = title + " " + description;
				List<String[]> sentences = cnp.parse2lines(sentence);
				StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder(), sb3 = new StringBuilder();
				for (String[] sen : sentences) {
					//					String[] towrite = new String[5];
					//					towrite[0] = arg1PlusArg2;
					//					towrite[1] = relation;
					sb1.append(sen[0]);
					sb2.append(sen[1]);
					sb3.append(sen[2]);
					//dw.write(towrite);
				}
				dw.write(arg1PlusArg2, relation, sb1.toString(), sb2.toString(), sb3.toString());
			}
		}
		dw.close();
	}

	public static void convert2Pb(String file, String output) throws FileNotFoundException, IOException {
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		{
			DelimitedReader dr = new DelimitedReader(file + ".stanford");
			List<String[]> b = new ArrayList<String[]>();
			Builder relBuilder = null;
			while ((b = dr.readBlock(0)) != null) {
				String arg1PlusArg2 = b.get(0)[0];
				String relationname = b.get(0)[1];
				String arg12[] = arg1PlusArg2.split("::");
				relBuilder = Relation.newBuilder();
				relBuilder.setRelType(relationname);
				relBuilder.setSourceGuid(arg12[0]);
				relBuilder.setDestGuid(arg12[1]);
				for (String[] l : b) {
					RelationMentionRef.Builder mntBuilder = RelationMentionRef.newBuilder();
					mntBuilder.setFilename(arg1PlusArg2);
					mntBuilder.setSourceId(0);
					mntBuilder.setDestId(0);
					Set<String> patterns = new HashSet<String>();
					//mntBuilder.setFilename(l[2] + " " + l[3]);
					mntBuilder.setSentence(l[2]);
					//					l[2] = l[2].replace("  ", " ");
					//					l[3] = l[3].replace("  ", " ");
					//					l[4] = l[4].replace("  ", " ");
					Pattern.patternize(l[2].split(" "), l[3].split(" "), l[4].split(" "), patterns, 5);
					for (String p : patterns) {
						mntBuilder.addFeature(p);
					}
					//mntBuilder.build();
					relBuilder.addMention(mntBuilder.build());
				}
				if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0) {
					relBuilder.build().writeDelimitedTo(os);
				}
			}
			dr.close();
		}
		os.close();

	}

	static void eval_baseline_seedtrain_extendtest() throws IOException {
		String expdir = Main.dir + "/wiki/exp_seed_vs_extend";
		RphExtractorWrapper rew = new RphExtractorWrapper(Main.file_seed2lucene_pbgz,
				Main.file_extendedpair2lucene_pbgz, expdir);
		rew.learningThenTesting();
		//manualevalresult(Main.file_extendedpair2bing_pbgz + ".byrelationraw", expdir);
	}

	static void eval_baseline_extendtrain_seedtest() throws IOException {
		String expdir = Main.dir + "/wiki/exp_extend_vs_seed";
		RphExtractorWrapper rew = new RphExtractorWrapper(Main.file_extendedpair2lucene_pbgz,
				Main.file_seed2lucene_pbgz, expdir);
		rew.learningThenTesting();
		//manualevalresult(Main.file_extendedpair2bing_pbgz + ".byrelationraw", expdir);
	}

	static void eval_seedextend80train_extend20test() throws IOException {
		String expdir = Main.dir + "/wiki/exp2_seedextend80train_extend20test";
		(new File(expdir)).mkdir();
		String trainpb = expdir + "/trainpb";
		String testpb = expdir + "/testpb";
		{
			OutputStream ostrain = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(trainpb)));
			OutputStream ostest = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(testpb)));
			{
				InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(
						Main.file_seed2lucene_pbgz)));
				Builder relBuilder = null;
				Relation r = null;
				while ((r = Relation.parseDelimitedFrom(is)) != null) {
					relBuilder = Relation.newBuilder();
					relBuilder.setRelType(r.getRelType());
					relBuilder.setSourceGuid(r.getSourceGuid());
					relBuilder.setDestGuid(r.getDestGuid());
					relBuilder.addAllMention(r.getMentionList());
					if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0) {
						relBuilder.build().writeDelimitedTo(ostrain);
					}
				}
				is.close();
			}
			{
				InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(
						Main.file_extendedpair2lucene_pbgz)));
				Builder relBuilder = null;
				Relation r = null;
				Random ran = new Random();
				while ((r = Relation.parseDelimitedFrom(is)) != null) {
					relBuilder = Relation.newBuilder();
					relBuilder.setRelType(r.getRelType());
					relBuilder.setSourceGuid(r.getSourceGuid());
					relBuilder.setDestGuid(r.getDestGuid());
					relBuilder.addAllMention(r.getMentionList());
					if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0) {
						double x = ran.nextDouble();
						if (x > 0.8) {
							relBuilder.build().writeDelimitedTo(ostest);
						} else {
							relBuilder.build().writeDelimitedTo(ostrain);
						}
					}
				}
				is.close();
			}
			ostrain.close();
			ostest.close();
		}
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, expdir);
		rew.learningThenTesting();
	}

	public static void oldmain() {
		//getSentencesForSeedPairs();
		//		List<String[]> widpairs = (new DelimitedReader(Main.file_extendedwidpairs)).readAll();
		//		Pattern.widpair2rawtext(widpairs, 100, Main.file_extendtext);

		//searchWikiFile();
	}

	public static void main(String[] args) throws IOException {
		{
			//freebase.preprocess2.SearchEngineWikiWholeArticle.main(null);
		}
		{
			getSeedLuceneResultWholeArticle();
			//getExtendedPairLuceneResult(100);
		}
		{
			//			getStanfordParsedResult(Main.file_extendedpair2lucene, 2, 4, 5);
			//			getStanfordParsedResult(Main.file_seed2lucene, 2, 4, 5);
			//			MaltiDep.getDep(Main.file_extendedpair2bing + ".stanford", 2);
			//			MaltiDep.getDep(Main.file_seed2bing + ".stanford", 2);
			//			convert2Pb(Main.file_extendedpair2lucene, Main.file_extendedpair2lucene_pbgz);
			//			convert2Pb(Main.file_seed2lucene, Main.file_seed2lucene_pbgz);
			//			convert2Pb_Rph(Main.file_extendedpair2bing, Main.file_extendedpair2bing_pbgz);
			//			convert2Pb_Rph(Main.file_seed2bing, Main.file_seed2bing_pbgz);
			//CreateTestSetsByRelation(Main.file_extendedpair2bing_pbgz, 10);

		}
		{
			//			eval_baseline_seedtrain_extendtest();
			//			eval_baseline_extendtrain_seedtest();
			//			eval_seedextend80train_extend20test();
		}

	}

}
