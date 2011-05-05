package multir.tmp;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;

import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.MILModel;
import multir.learning.algorithm.Scorer;
import multir.learning.algorithm.Viterbi;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;
import multir.util.SparseBinaryVector;
import multir.util.delimited.DelimitedWriter;

public class TopFalseOld {

	DelimitedWriter w;
	
	public TopFalseOld(String file) throws IOException {
		w = new DelimitedWriter(file);
	}
	
	public boolean ignoreTopK = true;
	public HashSet<String> topK = new HashSet<String>();
	
	public void add(int iteration, 
			Scorer scorer, CRFParameters iterParameters,
			MILModel model, Dataset trainingData) throws IOException {
		
		class PQE {
			double score;
			int mentionID;
			int predicted;
			String arg1;
			String arg2;
			int[] ftIDs;
			double[] ftWeights;
			
			PQE(double score, int mentionID, int predicted, String arg1, String arg2,
					int[] ftIDs, double[] ftWeights) {
				this.score = score;
				this.mentionID = mentionID;
				this.predicted = predicted;
				this.arg1 = arg1;
				this.arg2 = arg2;
				this.ftIDs = ftIDs;
				this.ftWeights = ftWeights;
			}
		}
		
		Comparator<PQE> comp = new Comparator<PQE>() {
			public int compare(PQE e1, PQE e2) {
				if (e1.score > e2.score) return -1; else return 1; } };
		
		TreeSet<PQE> sTP = new TreeSet<PQE>(comp);
		TreeSet<PQE> sFP = new TreeSet<PQE>(comp);
		
		MILDocument doc = new MILDocument();
		
		scorer.setParameters(iterParameters);
		Viterbi v = new Viterbi(model, scorer);

		trainingData.reset();
		while (trainingData.next(doc)) {
			
			// identify mentions with high(est) score for which
			// we predict some relation, but ground truth says
			// no relation/different relation
			
			for (int m=0; m < doc.numMentions; m++) {

				Viterbi.Parse p = v.parse(doc, m);
				if (p.state == 0) continue;
				
				// determine top features
				SparseBinaryVector sbv = doc.features[m];
				int[] ftIDs = new int[sbv.num];
				double[] ftWeights = new double[sbv.num];
				System.arraycopy(sbv.ids, 0, ftIDs, 0, sbv.num);
				double[] allFeatureWeights = iterParameters.relParameters[p.state].vals;
				for (int i=0; i < sbv.num; i++)
					ftWeights[i] = allFeatureWeights[ftIDs[i]];
				
				boolean isGroundTruth = false;
				for (int i=0; i < doc.Y.length; i++)
					if (doc.Y[i] == p.state) isGroundTruth = true;
				
				PQE pqe = new PQE(p.score, doc.mentionIDs[m], p.state, doc.arg1, doc.arg2, 
						ftIDs, ftWeights);
				if (!isGroundTruth) {
					sFP.add(pqe);
					if (sFP.size() > 1000) sFP.pollLast();
				} else {
					sTP.add(pqe);
					if (sTP.size() > 1000) sTP.pollLast();
				}
			}
		}
		
		topK.clear();
		
		//int K = (iteration >=40)? 500 : 0;
		//int K = iteration*100;
		int K = 10;
		//int K=0;
		
		// print top25
		for (int j=0; j < K && !sTP.isEmpty(); j++) {
			PQE pqe = sTP.pollFirst();

			w.write("" + iteration, pqe.arg1, pqe.arg2, "" + pqe.mentionID, "" + pqe.predicted,
					toString(pqe.ftIDs), toString(pqe.ftWeights));
		}
	}
	
	public void close() throws IOException {
		w.close();
	}

	private static String toString(int[] a) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < a.length; i++) {
			if (i > 0) sb.append(" ");
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
	private static String toString(double[] a) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < a.length; i++) {
			if (i > 0) sb.append(" ");
			sb.append(a[i]);
		}
		return sb.toString();
	}
}
