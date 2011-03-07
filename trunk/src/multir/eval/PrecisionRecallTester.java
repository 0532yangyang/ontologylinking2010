package multir.eval;


public class PrecisionRecallTester {
	public double numCorrect, numPredictions, numRelations;
	
	public void handle(String[] tokens, boolean[] predictedLabels, 
			boolean[] trueLabels, double score) {
		boolean[] p = predictedLabels;
		boolean[] t = trueLabels;
		
		for (int i=1; i < p.length; i++) {
			if (p[i] && !t[i]) numPredictions++;
			else if (!p[i] && t[i]) numRelations++;
			else if (p[i] && t[i]) {
				numCorrect++;
				numPredictions++;
				numRelations++;
			}
		}
	}
	
	public void handle(int rel, boolean p, boolean t, double score) {
		if (p && !t) numPredictions++;
		else if (!p && t) numRelations++;
		else if (p && t) {
			numCorrect++;
			numPredictions++;
			numRelations++;
		}
	}
	
	public void reset() { numCorrect = numPredictions = numRelations = 0; }
	public double precision() {
		if (numPredictions == 0) return 1;
		return numCorrect / numPredictions; 
	}
	public double recall() { 
		return numCorrect / numRelations; 
	}
}
