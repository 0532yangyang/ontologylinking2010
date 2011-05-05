package freebase.jointmatch2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javatools.ml.rphacl2011extractor.RphExtractorWrapper;

import multir.eval.PrecisionRecallCurve2;
import multir.eval.ResultWriter;
import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.CollinsTraining2;
import multir.learning.algorithm.MILModel;
import multir.learning.data.Dataset;
import multir.learning.data.MemoryDataset;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.Mappings;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class S10_ie {

	//static String entities = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/gnid_mid_wid_title";
	//static String relations = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/sql2instance.subset";

	//	static String in  = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";
	//	static String out = "/projects/pardosa/data16/raphaelh/congle/subset07.100.pb.gz";

	//static String in  = "/projects/pardosa/data14/raphaelh/t/data/subset05-06.100.pb.gz";
	//static String out = "/projects/pardosa/data16/raphaelh/congle/subset05-06.100.pb.gz";
	public static void relabel0(String in, String out) throws NumberFormatException, IOException {
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;

		int count = 0;
		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);

			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range

			String name1 = r.getSourceGuid();
			String name2 = r.getDestGuid();

			StringBuilder sb = new StringBuilder();

			relBuilder.setRelType(sb.toString());

			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0) {
				//relBuilder.build().writeDelimitedTo();
			}
		}
		is.close();
		//os.close();
	}

	public static void relabel(String in, String out) throws NumberFormatException, IOException {
		HashMap<String, List<Integer>> name2id = new HashMap<String, List<Integer>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
					Main.file_gnid_mid_wid_title)));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				int id = Integer.parseInt(c[2]);

				String[] names = c[3].split(" ");
				for (String name : names) {
					name = name.replace("_", " ");
					List<Integer> ids = name2id.get(name);
					if (ids == null) {
						ids = new ArrayList<Integer>(1);
						name2id.put(name, ids);
						//System.out.println("adding '" + name + "'");
					}
					ids.add(id);
				}
			}
			r.close();
		}

		HashMap<Integer, List<String>> arg1ToRel = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> arg2ToRel = new HashMap<Integer, List<String>>();
		{
			BufferedReader r = new BufferedReader(
					new InputStreamReader(new FileInputStream(Main.file_extendedwidpairs)));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				int arg1 = Integer.parseInt(c[0]);
				int arg2 = Integer.parseInt(c[1]);
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

		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;

		int count = 0;
		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);

			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range

			String name1 = r.getSourceGuid();
			String name2 = r.getDestGuid();

			StringBuilder sb = new StringBuilder();

			List<Integer> id1 = name2id.get(name1);
			List<Integer> id2 = name2id.get(name2);
			if (id1 != null && id2 != null) {
				//System.out.println(name1 + "\t" + name2);

				HashSet<String> rels1 = new HashSet<String>();
				for (Integer id : id1) {
					List<String> l = arg1ToRel.get(id);
					if (l != null)
						rels1.addAll(l);
				}

				HashSet<String> rels2 = new HashSet<String>();
				for (Integer id : id2) {
					List<String> l = arg2ToRel.get(id);
					if (l != null)
						rels2.addAll(l);
				}
				//if (rels1.size() > 0 && rels2.size() > 0)
				//System.out.println(rels1.size() + "\t" + rels2.size());

				rels1.retainAll(rels2);
				//if (rels1.size() != 0) {
				//	System.out.println(name1 + "\t" + name2);
				//	System.out.println(rels1.size());
				//}

				HashSet<String> relTypes = new HashSet<String>();
				for (String rel : rels1) {
					String[] c = rel.split("\t");
					relTypes.add(c[2]);
				}

				for (String rt : relTypes) {
					if (sb.length() > 1)
						sb.append(",");
					sb.append(rt);
				}
			}
			if (sb.length() == 0)
				sb.append("NA");

			relBuilder.setRelType(sb.toString());

			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
				relBuilder.build().writeDelimitedTo(os);
		}
		is.close();
		os.close();

	}

	public static void reduceNegativeTraining(String in, String out) throws IOException {
		Random random = new Random();
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;
		int count = 0;
		int pos = 0, neg = 0;
		int newNeg = 0;
		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range 
			relBuilder.setRelType(r.getRelType());
			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			if (r.getRelType().equals("NA"))
				neg++;
			else
				pos++;

			if (r.getRelType().equals("NA"))
				if (random.nextDouble() < .99)
					continue;
			if (r.getRelType().equals("NA"))
				newNeg++;
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
				relBuilder.build().writeDelimitedTo(os);
		}
		is.close();
		os.close();
		System.out.println("before neg: " + neg);
		System.out.println("before pos: " + pos);
		System.out.println("new neg: " + newNeg);
		System.out.println("new pos pos: " + pos);
	}

	public static void writeFeatures(String input1, String output) throws IOException {
		{
			DelimitedWriter w = new DelimitedWriter(output, "utf-8", true);

			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(input1)));
			Relation r = null;

			int count = 0;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				if (++count % 10000 == 0)
					System.out.println(count);

				for (int m = 0; m < r.getMentionCount(); m++) {
					RelationMentionRef rmf = r.getMention(m);
					for (int f = 0; f < rmf.getFeatureCount(); f++) {
						//w.write(rmf.getFeature(f));
						//w.write('\n');
						w.write(rmf.getFeature(f));
					}
				}
			}
			w.close();
			is.close();
		}
	}

	public static void count(String input, String output) throws IOException {

		// initial runs
		int runs = 0;
		int stage = 0;
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		{
			DelimitedReader r = new DelimitedReader(input, "utf-8", true);
			String[] t = null;
			while ((t = r.read()) != null) {
				if (hm.size() > Main.MAP_SIZE) {
					writeMap(hm, output + stage + "-" + runs++);
					hm.clear();
					System.gc();
				}
				Integer i = hm.get(t[0]);
				if (i == null)
					i = 0;
				i++;
				hm.put(t[0], i);
			}
			writeMap(hm, output + stage + "-" + runs++);
			r.close();
		}

		merge(output + stage + "-", 0, runs, output);
	}

	private static void merge(String inPrefix, int from, int to, String out) throws IOException {
		DelimitedWriter w = new DelimitedWriter(out, 128 * 1024 * 1024);
		DelimitedReader[] r = new DelimitedReader[to - from];
		for (int i = from; i < to; i++)
			r[i - from] = new DelimitedReader(inPrefix + i);
		String[][] t = new String[to - from][];
		int nonNull = 0;
		for (int i = 0; i < t.length; i++) {
			t[i] = r[i].read();
			if (t[i] != null)
				nonNull++;
		}
		String curKey = "";
		int curCount = 0;
		while (nonNull > 0) {
			int minIndex = -1;
			for (int i = 0; i < t.length; i++) {
				if (t[i] == null)
					continue;
				if (minIndex < 0 || t[i][0].compareTo(t[minIndex][0]) < 0)
					minIndex = i;
			}
			if (t[minIndex][0].equals(curKey)) {
				curCount += Integer.parseInt(t[minIndex][1]);
			} else {
				if (curCount > 0) {
					w.write(curKey, curCount);
				}
				curKey = t[minIndex][0];
				curCount = Integer.parseInt(t[minIndex][1]);
			}
			t[minIndex] = r[minIndex].read();
			if (t[minIndex] == null)
				nonNull--;
		}
		if (curCount > 0) {
			w.write(curKey, curCount);
		}
		w.close();
		for (int i = 0; i < r.length; i++)
			r[i].close();
	}

	private static void writeMap(HashMap<String, Integer> hm, String file) throws IOException {
		List<Map.Entry<String, Integer>> m = new ArrayList<Map.Entry<String, Integer>>(hm.entrySet());
		Collections.sort(m, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
				return e1.getKey().compareTo(e2.getKey());
			}
		});
		DelimitedWriter w = new DelimitedWriter(file);
		for (Map.Entry<String, Integer> e : m)
			w.write(e.getKey(), e.getValue());
		w.close();
	}

	/**	static String exp = "/test1";
	static String input0 = dir + "/ftsSubset05-06.1.gz.counts";
	static String input1 = dir + "/subset05-06.1.pb.gz";
	static String input2 = dir + "/subset07.100.pb.gz";
	static int MIN_FTS = 1;*/
	public static void step2b(String expdir) throws IOException {
		new File(expdir).mkdir();

		String mappingFile = expdir + "/mapping";
		String modelFile = expdir + "/model";

		{
			Mappings m = new Mappings();
			DelimitedReader r = new DelimitedReader(Main.file_nyt05countfeatures_count);
			String[] t = null;
			while ((t = r.read()) != null) {
				if (Integer.parseInt(t[1]) >= Main.MIN_FTS)
					m.getFeatureID(t[0], true);
			}
			r.close();
			m.write(mappingFile);
		}

		{
			String output1 = expdir + "/train";
			ConvertProtobufToMILDocument.convert(Main.file_nyt05pbrelabelreduceneg, output1, mappingFile, false, true); //was true
		}

		{
			String output2 = expdir + "/test";
			ConvertProtobufToMILDocument.convert(Main.file_nyt07pbrelabel, output2, mappingFile, false, false);
		}

		{
			MILModel m = new MILModel();
			Mappings mappings = new Mappings();
			mappings.read(mappingFile);
			m.numRelations = mappings.numRelations();
			m.numFeaturesPerRelation = new int[m.numRelations];
			for (int i = 0; i < m.numRelations; i++)
				m.numFeaturesPerRelation[i] = mappings.numFeatures();
			m.write(modelFile);
		}
	}

	public static void step3(String expdir) throws IOException {

		//Random random = new Random(7);
		Random random = new Random(1);

		MILModel model = new MILModel();
		model.read(expdir + "/model");

		//CollinsTraining ct = new CollinsTraining(model, random);
		CollinsTraining2 ct = new CollinsTraining2(model);

		Dataset train = new MemoryDataset(random, expdir + "/train");
		Dataset test = new MemoryDataset(random, expdir + "/test");

		System.out.println("starting training");

		long start = System.currentTimeMillis();
		CRFParameters params = ct.train(train);
		long end = System.currentTimeMillis();
		System.out.println("training time " + (end - start) / 1000.0 + " seconds");

		OutputStream os = new BufferedOutputStream(new FileOutputStream(expdir + "/params"));
		params.serialize(os);
		os.close();

		//		long startTest = System.currentTimeMillis();
		//		MILDocument doc = new MILDocument();
		//		Scorer scorer = new Scorer();
		//		test.reset();
		//		while (test.next(doc))
		//			FullInference.infer(doc, scorer, params);
		//		long endTest = System.currentTimeMillis();
		//		System.out.println("testing time " + (endTest-startTest)/1000.0 + " seconds");

		PrintStream psc = new PrintStream(expdir + "/curve2");
		PrecisionRecallCurve2.eval(test, params, psc);
		psc.close();

		test.reset();
		PrintStream ps = new PrintStream(expdir + "/results");
		ResultWriter.eval(expdir + "/mapping", test, params, ps);
		ps.close();
	}

	public static void CreateTestSetsByRelation() throws IOException {
		int start2007 = 45961658;
		// read sentences for test set
		HashMap<Integer, String> sentences = new HashMap<Integer, String>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(Main.file_nytsentence),
					"utf-8"));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				int sentenceID = Integer.parseInt(c[0]);
				if (sentenceID > start2007)
					sentences.put(sentenceID, c[1]);
			}
			r.close();
		}

		Map<RelationMentionRef, Integer> mnt2mntID = new HashMap<RelationMentionRef, Integer>();
		Map<RelationMentionRef, Relation> map = new HashMap<RelationMentionRef, Relation>();
		Map<String, List<RelationMentionRef>> reltype2snt = new HashMap<String, List<RelationMentionRef>>();
		InputStream is = new GZIPInputStream(new FileInputStream(Main.file_nyt07pbrelabel));
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
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.file_byrelation_raw),
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

				// our prediction
				int sentenceID = Integer.parseInt(rmf.getFilename());

				//String snt = rmf.getSentence();
				String snt = sentences.get(sentenceID);

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
	public static void eval(String expdir) throws IOException {

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
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(Main.file_byrelation_raw),
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

	public static void main(String[] args) throws IOException {
		relabel0(Main.file_nyt05pb, Main.file_nyt05pbrelabel);
		{
			//create training and testing

			//relabel(Main.file_nyt05pb, Main.file_nyt05pbrelabel);
			//			relabel(Main.file_nyt07pb, Main.file_nyt07pbrelabel);
			//			reduceNegativeTraining(Main.file_nyt05pbrelabel, Main.file_nyt05pbrelabelreduceneg);
		}
		//		writeFeatures(Main.file_nyt05pbrelabelreduceneg, Main.file_nyt05countfeatures);
		//		count(Main.file_nyt05countfeatures, Main.file_nyt05countfeatures_count);
		//		step2b(Main.expdir0);
		//		step3(Main.expdir0);
		{
			//call my wrapper
			RphExtractorWrapper rew = new RphExtractorWrapper(Main.file_nyt05pbrelabelreduceneg,
					Main.file_nyt07pbrelabel, Main.expdir0);
			rew.learningThenTesting();
		}
		{
			//for manual labeling
			CreateTestSetsByRelation();
			eval(Main.expdir0);
		}
	}

}