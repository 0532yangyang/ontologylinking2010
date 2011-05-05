package multir.eval;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.FullInferenceTest;
import multir.learning.algorithm.Parse;
import multir.learning.algorithm.Scorer;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;

public class PrecisionRecallCurve {

	static class Prediction {
		int rel;
		boolean trueRel;
		boolean predRel;
		double score;
		Prediction(int rel, boolean trueRel, boolean predRel, double score) {
			this.trueRel = trueRel;
			this.predRel = predRel;
			this.score = score;
		}
	}
	
	public static void eval(Dataset test, CRFParameters params,
			PrintStream ps) throws IOException {
		System.out.println("eval");
		Scorer scorer = new Scorer();

		
		// this could also be a file
		List<Prediction> predictions = new ArrayList<Prediction>();
		MILDocument doc = new MILDocument();
		int numRelationInst = 0;
		test.reset();
		while (test.next(doc)) {
			numRelationInst += doc.Y.length;
			Parse parse = FullInferenceTest.infer(doc, scorer, params);
			int[] Yt = doc.Y;
			int[] Yp = parse.Y;
			
			// NA is empty array
			if (Yt.length == 0 && Yp.length == 0) continue;
				// true negative, we ignore that
					
			int i = 0, j = 0;
			while (i < Yt.length || j < Yp.length) {
				if (i==Yt.length) {
					predictions.add(new Prediction
						(Yp[j], false, true, parse.scores[Yp[j]]));
					j++;
				} else if (j==Yp.length) {
					predictions.add(new Prediction
						(Yt[i], true, false, parse.scores[Yt[i]]));
					i++;
				} else {
					if (Yt[i] < Yp[j]) {
						predictions.add(new Prediction
							(Yt[i], true, false, parse.scores[Yt[i]]));
						i++;
					} else if (Yt[i] > Yp[j]) {
						predictions.add(new Prediction
							(Yp[j], false, true, parse.scores[Yp[j]]));
						j++;
					} else {
						predictions.add(new Prediction
								(Yt[i], true, true, parse.scores[Yp[j]]));
						i++; j++;
					}
				}
			}
		}
		
		Collections.sort(predictions, new Comparator<Prediction>() {
			public int compare(Prediction p1, Prediction p2) {
				if (p1.score > p2.score) return -1;
				else return +1;
			} });

		/*
		for (int i=0; i < predictions.size(); i++) {
			Prediction p = predictions.get(i);
			// find sentence with that score
			if (p.trueRel != p.predRel) {
				System.out.println(p.rel + "\t" + p.trueRel + "\t" + p.predRel + "\t" + 
						p.score + "\t" + p.doc.arg1 + "\t" + p.doc.arg2);
				int m = 0;
				for (int j=0; j < p.parse.mntScores.length; j++)
					if (p.parse.mntScores[j] == p.score) m = j;
				System.out.println(p.doc.m[m].debug);
				System.out.println(guid2name.get(p.doc.arg1) + "\t" + guid2name.get(p.doc.arg2));				
			}
		}*/
		
		PrecisionRecallTester prt = new PrecisionRecallTester();
		prt.reset();
		for (int i=0; i < predictions.size(); i++) {
			Prediction p = predictions.get(i);
			prt.handle(p.rel, p.predRel, p.trueRel, p.score);
			prt.numRelations = numRelationInst;
			ps.println(prt.recall() + "\t" + prt.precision());
		}
	}

		/*
	private static void print(boolean[] t, boolean[] p, Model model) {
		List<Integer> lp = new ArrayList<Integer>();
		for (int i=1; i < p.length; i++) if (p[i]) lp.add(i);

		List<Integer> lt = new ArrayList<Integer>();
		for (int i=1; i < t.length; i++) if (t[i]) lt.add(i);

		if (lp.isEmpty() && lt.isEmpty()) {}
		else if (lp.isEmpty()) {
			
		}
		else if (lt.isEmpty()) {
			
		} else {
			System.out.println("T   " + toString(lt, model));
			System.out.println("  P " + toString(lp, model));
		}
		
	}
	
	private static String toString(List<Integer> l, Model model) {
		StringBuilder sb = new StringBuilder();
		for (Integer i : l) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(model.relationNode.stateId2name.get(i));
		}
		return sb.toString();
	}
	*/
	
}
