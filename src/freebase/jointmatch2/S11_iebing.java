package freebase.jointmatch2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import stanfordCoreWrapper.CoreNlpPipeline;

import cc.factorie.protobuf.DocumentProtos.Document.RelationMention;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.ml.rphacl2011extractor.RphExtractorWrapper;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;
import javatools.webapi.BingApi;

public class S11_iebing {

	public static void getSeedBingResult() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_seed2bing);
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] seed : nr.seedInstances) {
				//D.p(seed[0] + "\t" + seed[1]);
				String query = "\"" + seed[0] + "\" \"" + seed[1] + "\"";
				D.p(query);
				List<String[]> res = BingApi.getResult(query);
				for (String[] r : res) {
					String[] w = new String[r.length + 3];
					w[0] = nr.relation_name;
					w[1] = seed[0];
					w[2] = seed[1];
					System.arraycopy(r, 0, w, 3, r.length);
					dw.write(w);
				}
				dw.flush();
				//Thread.sleep(1000);
			}
		}
		dw.close();
	}

	public static void getExtendedPairBingResult(int samplesize) throws IOException {

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
			DelimitedWriter dw = new DelimitedWriter(Main.file_extendedpair2bing + ".preparepairs");
			for (String[] w : towrite) {
				dw.write(w);
			}
			dw.close();
		}
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_extendedpair2bing);
			for (String[] w : towrite) {
				String query = "\"" + w[2] + "\" \"" + w[3] + "\"";
				D.p(query);
				List<String[]> res = BingApi.getResult(query);
				for (String[] r : res) {
					String[] s = new String[r.length + w.length];
					System.arraycopy(w, 0, s, 0, w.length);
					System.arraycopy(r, 0, s, w.length, r.length);
					dw.write(s);
				}
				dw.flush();
			}
			dw.close();
			//Thread.sleep(1000);
		}
	}

	public static void getStanfordParsedResult(String file, int arg1index, int relindex, int titleindex)
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
				String title = l[titleindex].replace("Title:", "");
				String description = l[titleindex + 1].replace("Description:", "");
				String toparse = title + " " + description;
				List<String[]> sentences = cnp.parse2lines(toparse);
				StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder(), sb3 = new StringBuilder();
				for (String[] sen : sentences) {
					//					String[] towrite = new String[5];
					//					towrite[0] = arg1PlusArg2;
					//					towrite[1] = relation;
					sb1.append(sen[0] + " ");
					sb2.append(sen[1] + " ");
					sb3.append(sen[2] + " ");
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
					l[2] = l[2].replace("  ", " ");
					l[3] = l[3].replace("  ", " ");
					l[4] = l[4].replace("  ", " ");
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

	public static void CreateTestSetsByRelation(String filepb) throws IOException {
		Map<RelationMentionRef, Integer> mnt2mntID = new HashMap<RelationMentionRef, Integer>();
		Map<RelationMentionRef, Relation> map = new HashMap<RelationMentionRef, Relation>();
		Map<String, List<RelationMentionRef>> reltype2snt = new HashMap<String, List<RelationMentionRef>>();
		InputStream is = new GZIPInputStream(new FileInputStream(filepb));
		Relation r = null;

		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			String[] relTypes = r.getRelType().split(",");
			for (String relType : relTypes) {
				if (relType.equals("NA"))
					continue;
				for (int i = 0; i < r.getMentionCount(); i++) {
					RelationMentionRef rmf = r.getMention(i);
					mnt2mntID.put(rmf, i);
					map.put(rmf, r);
					List<RelationMentionRef> l = reltype2snt.get(relType);
					if (l == null) {
						l = new ArrayList<RelationMentionRef>();
						reltype2snt.put(relType, l);
					}
					l.add(rmf);
				}
			}
		}
		is.close();
		for (Map.Entry<String, List<RelationMentionRef>> e : reltype2snt.entrySet()) {
			System.out.println(e.getKey() + "\t" + e.getValue().size());
		}
		// write sample
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepb + ".byrelationraw"),
				"utf-8"));

		for (Map.Entry<String, List<RelationMentionRef>> e : reltype2snt.entrySet()) {

			List<Integer> ids = new ArrayList<Integer>();
			for (int i = 0; i < e.getValue().size(); i++)
				ids.add(i);
			Collections.shuffle(ids);

			// take up to 100 sentences for each relation
			for (int i = 0; i < Math.min(ids.size(), 1000); i++) {
				int id = ids.get(i);
				RelationMentionRef rmf = e.getValue().get(ids.get(i));
				r = map.get(rmf);

				String name1 = r.getSourceGuid();
				String name2 = r.getDestGuid();
				if (name1 == null)
					name1 = "";
				if (name2 == null)
					name2 = "";

				String snt = rmf.getSentence();

				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < Math.min(10, rmf.getFeatureList().size()); j++) {
					if (sb.length() > 0)
						sb.append(";");
					sb.append(rmf.getFeature(j));
				}
				int mntID = mnt2mntID.get(rmf);

				String sentence = snt;
				sentence = sentence.replaceAll(name1, "[" + name1 + "]1");
				sentence = sentence.replaceAll(name2, "[" + name2 + "]2");

				//w.write(id + "\t" + sentence + "\t" + "" + "\t" + name1 + "\t" + name2 + 
				//		"\t" + e.getKey() + "\t" + r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + snt + "\n" );
				w.write(name1 + "\t" + name2 + "\t" + mntID + "\t" + e.getKey() + "\t" + "" + "\t" + sentence + "\t"
						+ snt + "\t" + sb.toString() + "\n");
			}
		}
		w.close();
	}

	/**	static String labelsFile = "/projects/pardosa/data16/raphaelh/congle/byrelation_raw.unicode.txt";
	static String results1File = "/projects/pardosa/data16/raphaelh/congle/test1/results";

	static boolean indirect = true;*/
	public static void manualevalresult(String file_byrelationraw, String expdir) throws IOException {

		final boolean indirect = true;
		// put results into map
		// guid1, guid2, mtnID -> rel
		Map<String, String> results = new HashMap<String, String>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(expdir + "/results"),
					"utf-8"));
			r.readLine();
			r.readLine();
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				String guid1 = c[0];
				String guid2 = c[1];
				int mntID = Integer.parseInt(c[2]);

				String rel = c[3];
				results.put(guid1 + "\t" + guid2 + "\t" + mntID, rel);
			}
			r.close();
		}

		// read labels
		// relation --> (guid1, guid2, mntID -> y/n/indirect)
		Map<String, Map<String, String>> labels = new HashMap<String, Map<String, String>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file_byrelationraw),
					"utf-8"));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				if (c.length < 2) {
					System.out.println(l);
					continue;
				}
				String relation = c[3];
				String guid1 = c[0];
				String guid2 = c[1];
				String mntID = c[2];
				String label = c[4];

				if (label.equals(""))
					continue; // ignore unlabeled sentences

				Map<String, String> m = labels.get(relation);
				if (m == null) {
					m = new HashMap<String, String>();
					labels.put(relation, m);
				}
				m.put(guid1 + "\t" + guid2 + "\t" + mntID, label);
			}
			r.close();
		}

		// sort by their number of true labels
		List<Map.Entry<String, Map<String, String>>> l = new ArrayList<Map.Entry<String, Map<String, String>>>();
		l.addAll(labels.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Map<String, String>>>() {
			public int compare(Map.Entry<String, Map<String, String>> e1, Map.Entry<String, Map<String, String>> e2) {
				int n1 = 0;
				for (Map.Entry<String, String> e : e1.getValue().entrySet())
					if (e.getValue().equals("y") || (indirect && e.getValue().equals("indirect")))
						n1++;

				int n2 = 0;
				for (Map.Entry<String, String> e : e2.getValue().entrySet())
					if (e.getValue().equals("y") || (indirect && e.getValue().equals("indirect")))
						n2++;
				return n2 - n1;
			}
		});

		// evaluate
		for (Map.Entry<String, Map<String, String>> e : l) { //labels.entrySet()) {
			System.out.println(e.getKey());
			String fbRel = e.getKey();

			int TP = 0, FP = 0, TN = 0, FN = 0;

			int numFbIsTrue = 0;

			Map<String, String> m = e.getValue();
			for (Map.Entry<String, String> c : m.entrySet()) {
				//System.out.println("get result for key " + c.getKey());
				String prRel = results.get(c.getKey());
				boolean fbIsTrue = c.getValue().equals("y") || (indirect && c.getValue().equals("indirect"));
				if (fbRel.equals(prRel) && fbIsTrue)
					TP++;
				else if (fbRel.equals(prRel) && !fbIsTrue)
					FP++;
				else if (!fbRel.equals(prRel) && fbIsTrue)
					FN++;
				else if (!fbRel.equals(prRel) && !fbIsTrue)
					TN++;
				if (fbIsTrue)
					numFbIsTrue++;
			}
			if (TP + FP == 0)
				System.out.println("  precision\tNA (no positive predictions)");
			else
				System.out.println("  precision\t" + (double) TP / (double) (TP + FP));

			if (TP + FN == 0)
				System.out.println("  recall\tNA (no positive labels in test data)");
			else
				System.out.println("  recall\t" + (double) TP / (double) (TP + FN));
			System.out.println("  # fb annot \t" + m.size() + " (fb annotation precision "
					+ (numFbIsTrue / (double) m.size()) + ")");
		}
	}

	static void eval_baseline_seedtrain_extendtest() throws IOException {
		String expdir = Main.dir_bing + "/exp_seed_vs_extend";
		RphExtractorWrapper rew = new RphExtractorWrapper(Main.file_seed2bing_pbgz, Main.file_extendedpair2bing_pbgz,
				expdir);
		rew.learningThenTesting();
		//manualevalresult(Main.file_extendedpair2bing_pbgz + ".byrelationraw", expdir);
	}

	static void eval_baseline_extendtrain_seedtest() throws IOException {
		String expdir = Main.dir_bing + "/exp_extend_vs_seed";
		RphExtractorWrapper rew = new RphExtractorWrapper(Main.file_extendedpair2bing_pbgz, Main.file_seed2bing_pbgz,
				expdir);
		rew.learningThenTesting();
		//manualevalresult(Main.file_extendedpair2bing_pbgz + ".byrelationraw", expdir);
	}

	static void eval_seedextend80train_extend20test() throws IOException {
		String expdir = Main.dir_bing + "/exp2_seedextend80train_extend20test";
		(new File(expdir)).mkdir();
		String trainpb = expdir + "/trainpb";
		String testpb = expdir + "/testpb";
		{
			OutputStream ostrain = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(trainpb)));
			OutputStream ostest = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(testpb)));
			{
				InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(
						Main.file_seed2bing_pbgz)));
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
						Main.file_extendedpair2bing_pbgz)));
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

	public static void main(String[] args) throws IOException {
		{
			//getSeedBingResult();
			//getExtendedPairBingResult(Main.PAR_NUM_PAIRS_PER_RELATION);
		}
		{
			//			getStanfordParsedResult(Main.file_extendedpair2bing, 2, 4, 6);
			//			getStanfordParsedResult(Main.file_seed2bing, 1, 0, 3);
			//			convert2Pb(Main.file_extendedpair2bing, Main.file_extendedpair2bing_pbgz);
			//			convert2Pb(Main.file_seed2bing, Main.file_seed2bing_pbgz);
			//			CreateTestSetsByRelation(Main.file_extendedpair2bing_pbgz);
		}
		{
			//eval_baseline_seedtrain_extendtest();
			//eval_baseline_extendtrain_seedtest();
			eval_seedextend80train_extend20test();
		}
		{
			//			RphExtractorWrapper rew = new RphExtractorWrapper(Main.file_extendedpair2bing_pbgz,
			//					Main.file_seed2bing_pbgz, Main.dir_bing + "/exp_extend_vs_seed");
			//			rew.learningThenTesting();
			//CreateTestSetsByRelation(Main.file_seed2bing_pbgz);
		}
	}
}
