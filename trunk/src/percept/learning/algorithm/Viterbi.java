package percept.learning.algorithm;

import percept.learning.data.Example;

public class Viterbi {

	private Scorer parseScorer;
	private Model model;
	
	public Viterbi(Model model, Scorer parseScorer) {
		this.model = model;
		this.parseScorer = parseScorer;
	}
	
	public Parse parse(Example doc) {
		int numStates = model.numStates;

		double[] scores = new double[numStates];
				
		for (int s = 0; s < numStates; s++) {
			scores[s] = parseScorer.scoreState(doc, s);
		}

		int bestRel = 0;
		for (int r = 0; r < model.numStates; r++) {
			if (scores[r] > scores[bestRel]) {
				bestRel = r; }
		}

		Parse p = new Parse(bestRel, scores[bestRel]);
		p.scores = scores;
		return p;
	}
	
	public static class Parse {
		// MPE
		public int state;
		public double score;
		
		// scores of all assignments
		public double[] scores;
		
		Parse(int state, double score) {
			this.state = state;
			this.score = score;
		}
	}
}
