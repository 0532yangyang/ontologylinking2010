package multir.eval.congle;

//import cc.factorie.distant.Condition;
//import cc.factorie.distant.ReadProtoBuf;
//import cc.factorie.distant.SizeCondition;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import multir.util.delimited.DelimitedWriter;
import cc.factorie.protobuf.DocumentProtos.Relation;

/**
* Created by IntelliJ IDEA. User: xiaoling Date: 12/12/10 Time: 4:03 AM To
* change this template use File | Settings | File Templates.
*/
public class GetPredictions {

	static String input0 = "/projects/pardosa/s5/clzhang/tmp/seb/acl_predictions.txt";
	// static String input1 =
	// "/projects/pardosa/s5/clzhang/tmp/seb/test-all-newlabel-Multiple.pb";
	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/testPositive.pb";
	static String input2 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/testNegative.pb";

	static String input3 = "/projects/pardosa/s5/clzhang/liminyao/labelsentencelevel500";

	static String output1 = Main.dir+"/umassresultTestMultiple";
	static String output_pr = Main.dir+"/umassresultTestMultiple.highprc";
	static String output2 = Main.dir+"/umassresultTestMultiple.pair";
	static String output3 = Main.dir+"/umassAlignTo500sentence";

	public static ArrayList<Relation> readData() throws FileNotFoundException {

		ArrayList<Relation> list0 = new ArrayList<Relation>();
		{
			String file = input1;
			InputStream input = new FileInputStream(file);
			Relation r = null;
			int count = 0;
			HashSet<String> relationTypes = new HashSet<String>();

			int numPos = 0, numNeg = 0;
			// it works like an iterator
			while ((r = Iterators.readRelation(input)) != null) {
				list0.add(r);
			}
		}
		{
			String file = input2;
			InputStream input = new FileInputStream(file);
			Relation r = null;
			int count = 0;
			HashSet<String> relationTypes = new HashSet<String>();

			int numPos = 0, numNeg = 0;
			// it works like an iterator
			while ((r = Iterators.readRelation(input)) != null) {
				list0.add(r);
			}
		}
		return list0;
	}

	//
	// public static void main2(String[] args) throws FileNotFoundException {
	//
	// readData();
	// }

	public static void main(String[] args) throws IOException {
		// getPredictions(args[0]);
		ArrayList<Relation> data = readData();
		ArrayList<Prediction> predictions = getPredictions(input0);
		// order matters!
		outputRaphaelStype(predictions, data, output2, output1);
		//outputRaphaelStype_ScoreThresh(predictions, output2, output1);
		System.out.println("Number of predictions\t" + predictions.size());
		String[] dataFiles = new String[] { "", "" };
		String sampleFile = input3;
		evalSentenceLabels(predictions, dataFiles, sampleFile);
	}

	private static void outputRaphaelStype(List<Prediction> preds,
			List<Relation> data, String outputPair, String outputSen)
			throws IOException {
		DelimitedWriter dw1 = new DelimitedWriter(outputPair);
		DelimitedWriter dw2 = new DelimitedWriter(outputSen);
		Iterator<Relation> it = data.iterator();
		for (Prediction p : preds) {
			Relation r = it.next();

			dw1.write(p.srcId, p.dstId, p.pred, p.score);
			System.out.println(p.score);
			boolean threshold = p.score > 20;
			for (int i = 0; i < p.mentions.size(); i++) {
				String sen = r.getMention(i).getSentence();
				MentionPrediction mp = p.mentions.get(i);
				dw2.write(p.srcId, p.dstId, i, mp.pred, p.score, sen);
			}
		}
		dw2.close();
		dw1.close();
	}

	private static void outputRaphaelStype_ScoreThresh(List<Prediction> preds,
			String outputPair, String outputSen, double score)
			throws IOException {
		DelimitedWriter dw1 = new DelimitedWriter(outputPair);
		DelimitedWriter dw2 = new DelimitedWriter(outputSen);
		for (Prediction p : preds) {
			dw1.write(p.srcId, p.dstId, p.pred);
			for (int i = 0; i < p.mentions.size(); i++) {
				MentionPrediction mp = p.mentions.get(i);
				if (score > 0) {
					dw2.write(p.srcId, p.dstId, i, mp.pred);
				} else {
					dw2.write(p.srcId, p.dstId, i, "NA");
				}
			}
		}
		dw2.close();
		dw1.close();
	}

	public static void evalSentenceLabels(ArrayList<Prediction> predictions,
			String[] dataFiles, String sampleFile) throws IOException {
		// read data
		ArrayList<Relation> data = readData();
		// for (int i = 0; i < dataFiles.length; i++){
		// scala.collection.Iterator iterator =
		// ReadProtoBuf.readRelations(dataFiles[i], new
		// SizeCondition(100000)).iterator();
		// while (iterator.hasNext()){
		// data.add(((Relation)iterator.next()));
		// }
		// }

		// read sampleFile
		// in the format of "[guid1]\t[guid2]\t[sent]\t[label]"
		DelimitedWriter dw = new DelimitedWriter(output3);
		Hashtable<String, String> truthSet = new Hashtable<String, String>();
		try {

			BufferedReader reader = new BufferedReader(new FileReader(
					sampleFile));
			String line = null;
			while ((line = reader.readLine()) != null) {

				String[] items = line.split("\t");
				truthSet.put(items[0] + "\t" + items[1] + "\t" + items[2],
						items[3]);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// scan data
		int countCorrect = 0, countAll = 0;
		for (int i = 0; i < data.size(); i++) {
			Relation rel = data.get(i);
			Prediction pred = predictions.get(i);
			// assert(3==2);

			if (rel.getMentionCount() != pred.mentions.size())

				System.err.println("ssss");
			for (int j = 0; j < rel.getMentionCount(); j++) {
				if (truthSet.containsKey(rel.getSourceGuid() + "\t"
						+ rel.getDestGuid() + "\t"
						+ rel.getMention(j).getSentence())) {
					countAll++;
					if (pred.mentions.get(j).pred.equals(truthSet.get(rel
							.getSourceGuid()
							+ "\t"
							+ rel.getDestGuid()
							+ "\t"
							+ rel.getMention(j).getSentence()))) {
						countCorrect++;
					}
					String x = truthSet.get(rel.getSourceGuid() + "\t"
							+ rel.getDestGuid() + "\t"
							+ rel.getMention(j).getSentence());
					// System.out.println(rel.getSourceGuid() + "\t"
					// + rel.getDestGuid() + "\t"
					// + rel.getMention(j).getSentence() + "\t"
					// + pred.mentions.get(j).pred + "\t" + x);
					dw.write(rel.getSourceGuid(), rel.getDestGuid(), rel
							.getMention(j).getSentence(),
							pred.mentions.get(j).pred, x);
				}
			}
		}

		// print out

		System.err.println("accuracy = " + (double) countCorrect / countAll);
		dw.close();
	}

	public static ArrayList<Prediction> getPredictions(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = null;
			ArrayList<Prediction> predictions = new ArrayList<Prediction>();
			Prediction pred = null;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("==")) {
					// start of a relation
					if (pred != null) {
						predictions.add(pred);
					}
					pred = new Prediction();
					StringTokenizer st = new StringTokenizer(line);
					if (st.countTokens() >= 5) {
						pred.srcId = st.nextToken();
						pred.dstId = st.nextToken();
					}
					pred.pred = st.nextToken();
					pred.truth = st.nextToken();
					pred.score = Double.parseDouble(st.nextToken());
				} else {
					StringTokenizer st = new StringTokenizer(line);
					MentionPrediction mp = new MentionPrediction();
					if (line.charAt(2) == '\t') {
						st.nextToken();
						mp.pred = st.nextToken().equals("Yes") ? pred.pred
								: "NA";
						mp.score = Double.parseDouble(st.nextToken());
					} else {
						mp.pred = st.nextToken().equals("==Yes:") ? pred.pred
								: "NA";
					}
					pred.mentions.add(mp);
					count++;
				}
			}
			predictions.add(pred);
			System.err.println(predictions.size());
			System.err.println(count);
			reader.close();
			return predictions;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

class Prediction {
	public String dstId, srcId, pred, truth;
	public double score = 0;
	public ArrayList<MentionPrediction> mentions = new ArrayList<MentionPrediction>();
}

class MentionPrediction {
	public String pred = null;
	public double score = 0;
}