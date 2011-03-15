package multir.tmp;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

import javatools.filehandlers.DelimitedWriter;

import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.MILModel;
import multir.learning.algorithm.Scorer;
import multir.learning.algorithm.Viterbi;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;

public class TopFalse {

	DelimitedWriter w;
	
	public TopFalse(String file) throws IOException {
		w = new DelimitedWriter(file);
	}
	
	public void add(int iteration, 
			Scorer scorer, CRFParameters iterParameters,
			MILModel model, Dataset trainingData) throws IOException {
		class PQE {
			double score;
			int mentionID;
			int predicted;
			String arg1;
			String arg2;
			PQE(double score, int mentionID, int predicted, String arg1, String arg2) {
				this.score = score;
				this.mentionID = mentionID;
				this.predicted = predicted;
				this.arg1 = arg1;
				this.arg2 = arg2;
			}
		}
		
		PriorityQueue<PQE> pq = new PriorityQueue<PQE>(10, new Comparator<PQE>() {
			public int compare(PQE e1, PQE e2) {
				if (e1.score > e2.score) return -1; else return 1;
			}
		});
		
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
				
				boolean isGroundTruth = false;
				for (int i=0; i < doc.Y.length; i++)
					if (doc.Y[i] == p.state) isGroundTruth = true;
				
				if (!isGroundTruth) {
					pq.add(new PQE(p.score, doc.mentionIDs[m], p.state, doc.arg1, doc.arg2));
				}
			}
		}
		
		// print top5
		for (int j=0; j < 25 && !pq.isEmpty(); j++) {
			PQE pqe = pq.poll();
			System.out.println(pqe.arg1 + " " + pqe.arg2 + " " + pqe.mentionID + " " + pqe.predicted);
			w.write("" + iteration, pqe.arg1, pqe.arg2, "" + pqe.mentionID, "" + pqe.predicted);
		}

		
	}
	
	public void close() throws IOException {
		w.close();
	}
	
}
