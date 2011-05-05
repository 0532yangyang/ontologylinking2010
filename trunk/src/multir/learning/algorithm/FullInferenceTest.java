package multir.learning.algorithm;

import multir.learning.data.MILDocument;


public class FullInferenceTest {

	public static Parse infer(MILDocument doc,
			Scorer parseScorer, CRFParameters params) {
		Parse parse = new Parse();
		parse.doc = doc;
		parse.Z = new int[doc.numMentions];
		
		parseScorer.setParameters(params);
		
		Viterbi viterbi = new Viterbi(params.model, parseScorer);
		
		double[] scores = new double[params.model.numRelations];
		double[][] allScores = new double[doc.numMentions][params.model.numRelations];
		//int[] scoresC = new int[scores.length];
		for (int i=0; i < scores.length; i++) scores[i] = Double.NEGATIVE_INFINITY;
		boolean[] binaryYs = new boolean[params.model.numRelations];		
		int numYs = 0;
		for (int m = 0; m < doc.numMentions; m++) {
			Viterbi.Parse p = viterbi.parse(doc, m);
			
			/*
			// find highest scoring relation which is not NA
			// new score is its score minus the score of NA
			int max = 1;
			for (int j=0; j < p.scores.length; j++)
				if (p.scores[j] > p.scores[max]) max = j;
			p.state = max;
			p.score = scores[max] - scores[0];
			*/
			parse.Z[m] = p.state;
			if (p.state > 0 && !binaryYs[p.state]) {
				binaryYs[p.state] = true;
				numYs++;
			}
			
			//parse.score += p.score;
			//scores[parse.Z[m]] += p.score;
			//scoresC[parse.Z[m]] ++;
			if (p.score > scores[parse.Z[m]])
				scores[parse.Z[m]] = p.score;
			
			for (int i=0; i < params.model.numRelations; i++)
				allScores[m][i] = p.scores[i];
		}

		parse.Y = new int[numYs];
		int pos = 0;
		for (int i=1; i < binaryYs.length; i++)
			if (binaryYs[i]) {
				parse.Y[pos++] = i;
				if (pos == numYs) break;
			}
		
		parse.scores = scores;
		parse.allScores = allScores;
		
		//for (int i=0; i < scores.length; i++)
		//	if (scoresC[i] > 0) scores[i] /= scoresC[i];
		
		
		// It's important to ignore the _NO_RELATION_ type here, so
		// need to start at 1!
		// final value is avg of maxes
		int sumNum = 0;
		double sumSum = 0;
		for (int i=1; i < scores.length; i++)
			if (scores[i] > Double.NEGATIVE_INFINITY) { 
				sumNum++; sumSum += scores[i]; 
			}
		if (sumNum ==0) parse.score = Double.NEGATIVE_INFINITY;
		else parse.score = sumSum / sumNum;
		
		
		/*
		// determine single best prediction
		parse.scores = scores;
		double bestS = Double.NEGATIVE_INFINITY;
		int bestI = 1;
		for (int i=1; i < scores.length; i++)
			if (scores[i] > bestS) { bestS = scores[i]; bestI = i; }
		// set all others to false
		for (int i=1; i < parse.Y.length; i++)
			if (i != bestI) parse.Y[i] = false;
		parse.score = bestS;
		// revisit Z's: set each Z to N/A or the highest scoring rel
		for (int m = 0; m < doc.m.length; m++) {
			MILMentionDocument mDoc = doc.m[m];
			parseScorer.setMentionDocument(mDoc);
			Viterbi.Parse p = viterbi.parse();
			if (p.scores[bestI] > p.scores[0])
				parse.Z[m] = bestI; else parse.Z[m] = 0;
		}
		*/
		return parse;		
	}
}
