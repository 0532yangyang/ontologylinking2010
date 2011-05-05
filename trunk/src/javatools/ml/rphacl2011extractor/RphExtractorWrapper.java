package javatools.ml.rphacl2011extractor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

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
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;
import freebase.jointmatch2.Main;

public class RphExtractorWrapper {
	/**outside*/
	String file_train;
	String file_test;
	String dir;

	/**inside temp*/
	String file_fts;
	String file_ftscnt;
	int MIN_FTS = 1;
	int MAP_SIZE = 2000000;

	public RphExtractorWrapper(String train, String test, String dir) throws IOException {
		this.file_train = train;
		this.file_test = test;
		this.dir = dir;

		file_fts = file_train + ".fts";
		file_ftscnt = file_fts + ".count";
		writeFeatures();
		count();
	}

	public void learningThenTesting() throws IOException {
		step2b(dir);
		step3(dir);
	}

	void writeFeatures() throws IOException {
		{
			DelimitedWriter w = new DelimitedWriter(file_fts, "utf-8", true);

			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file_train)));
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

	void count() throws IOException {
		// initial runs
		int runs = 0;
		int stage = 0;
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		{
			DelimitedReader r = new DelimitedReader(this.file_fts, "utf-8", true);
			String[] t = null;
			while ((t = r.read()) != null) {
				if (hm.size() > MAP_SIZE) {
					writeMap(hm, file_ftscnt + stage + "-" + runs++);
					hm.clear();
					System.gc();
				}
				Integer i = hm.get(t[0]);
				if (i == null)
					i = 0;
				i++;
				hm.put(t[0], i);
			}
			writeMap(hm, file_ftscnt + stage + "-" + runs++);
			r.close();
		}

		merge(file_ftscnt + stage + "-", 0, runs, file_ftscnt);
	}

	void merge(String inPrefix, int from, int to, String out) throws IOException {
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

	private void writeMap(HashMap<String, Integer> hm, String file) throws IOException {
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

	void step2b(String expdir) throws IOException {
		new File(expdir).mkdir();

		String mappingFile = expdir + "/mapping";
		String modelFile = expdir + "/model";

		{
			Mappings m = new Mappings();
			DelimitedReader r = new DelimitedReader(file_ftscnt);
			String[] t = null;
			while ((t = r.read()) != null) {
				if (Integer.parseInt(t[1]) >= MIN_FTS)
					m.getFeatureID(t[0], true);
			}
			r.close();
			m.write(mappingFile);
		}

		{
			String output1 = expdir + "/train";
			ConvertProtobufToMILDocument.convert(this.file_train, output1, mappingFile, false, true); //was true
		}

		{
			String output2 = expdir + "/test";
			ConvertProtobufToMILDocument.convert(this.file_test, output2, mappingFile, false, false);
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

	void step3(String expdir) throws IOException {

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
}
