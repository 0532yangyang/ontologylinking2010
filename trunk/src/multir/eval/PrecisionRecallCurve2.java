package multir.eval;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.FullInference;
import multir.learning.algorithm.Parse;
import multir.learning.algorithm.Scorer;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;

public class PrecisionRecallCurve2 {

	public static List<Prediction> eval(Dataset test, CRFParameters params, PrintStream ps) throws IOException {
		System.out.println("eval");
		Scorer scorer = new Scorer();

		// this could also be a file
		List<Prediction> predictions = new ArrayList<Prediction>();
		MILDocument doc = new MILDocument();
		int numRelationInst = 0;
		test.reset();
		while (test.next(doc)) {
			//numRelationInst += doc.Y.length;
			Parse parse = FullInference.infer(doc, scorer, params);
			int[] Yt = doc.Y;
			int[] Yp = parse.Y;

			// NA is empty array
			if (Yt.length == 0 && Yp.length == 0)
				continue;
			// true negative, we ignore that

			boolean[] binaryYt = new boolean[100];
			boolean[] binaryYp = new boolean[100];
			for (int i = 0; i < Yt.length; i++)
				binaryYt[Yt[i]] = true;
			for (int i = 0; i < Yp.length; i++)
				binaryYp[Yp[i]] = true;

			for (int i = 1; i < binaryYt.length; i++) {
				if (binaryYt[i] || binaryYp[i]) {
					predictions.add(new Prediction(i, binaryYt[i], binaryYp[i], parse.scores[i], doc, parse));
				}
			}

			for (int i = 1; i < binaryYt.length; i++)
				if (binaryYt[i])
					numRelationInst++;
			doc = new MILDocument();
		}

		Collections.sort(predictions, new Comparator<Prediction>() {
			public int compare(Prediction p1, Prediction p2) {
				if (p1.score > p2.score)
					return -1;
				else
					return +1;
			}
		});

		PrecisionRecallTester prt = new PrecisionRecallTester();
		prt.reset();
		for (int i = 0; i < predictions.size(); i++) {
			Prediction p = predictions.get(i);
			prt.handle(p.rel, p.predRel, p.trueRel, p.score);
			prt.numRelations = numRelationInst;
			ps.println(prt.recall() + "\t" + prt.precision());
		}
		return predictions;
	}

	
	public static List<Prediction> eval_na(int narelation_id, Dataset test, CRFParameters params, PrintStream ps) throws IOException {
		System.out.println("eval");
		Scorer scorer = new Scorer();

		// this could also be a file
		List<Prediction> predictions = new ArrayList<Prediction>();
		MILDocument doc = new MILDocument();
		int numRelationInst = 0;
		test.reset();
		while (test.next(doc)) {
			//numRelationInst += doc.Y.length;
			Parse parse = FullInference.infer(doc, scorer, params);
			int[] Yt = doc.Y;
			int[] Yp = parse.Y;

			// NA is empty array
			if (Yt.length == 0 && Yp.length == 0)
				continue;
			// true negative, we ignore that

			boolean[] binaryYt = new boolean[100];
			boolean[] binaryYp = new boolean[100];
			for (int i = 0; i < Yt.length; i++)
				binaryYt[Yt[i]] = true;
			for (int i = 0; i < Yp.length; i++)
				binaryYp[Yp[i]] = true;

			for (int i = 0; i < binaryYt.length; i++) {
				if(i==narelation_id){
					continue;
				}
				if (binaryYt[i] || binaryYp[i]) {
					predictions.add(new Prediction(i, binaryYt[i], binaryYp[i], parse.scores[i], doc, parse));
				}
			}

			for (int i = 1; i < binaryYt.length; i++)
				if (binaryYt[i])
					numRelationInst++;
			doc = new MILDocument();
		}

		Collections.sort(predictions, new Comparator<Prediction>() {
			public int compare(Prediction p1, Prediction p2) {
				if (p1.score > p2.score)
					return -1;
				else
					return +1;
			}
		});

		PrecisionRecallTester prt = new PrecisionRecallTester();
		prt.reset();
		for (int i = 0; i < predictions.size(); i++) {
			Prediction p = predictions.get(i);
			prt.handle(p.rel, p.predRel, p.trueRel, p.score);
			prt.numRelations = numRelationInst;
			ps.println(prt.recall() + "\t" + prt.precision()+"\t"+prt.numCorrect+"\t"+prt.numPredictions+"\t"+prt.numRelations);
		}
		return predictions;
	}

	//	static class Prediction {
	//		int rel;
	//		boolean trueRel;
	//		boolean predRel;
	//		double score;
	//		MILDocument doc;
	//		Parse parse;
	//		Prediction(int rel, boolean trueRel, boolean predRel, double score, 
	//				MILDocument doc, Parse parse) {
	//			this.rel = rel;
	//			this.trueRel = trueRel;
	//			this.predRel = predRel;
	//			this.score = score;
	//			this.doc = doc;
	//			this.parse = parse;
	//		}
	//	}
}
