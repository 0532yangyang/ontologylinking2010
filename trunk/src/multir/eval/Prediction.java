package multir.eval;

import multir.learning.algorithm.Parse;
import multir.learning.data.MILDocument;

public class Prediction {
	public int rel;
	public boolean trueRel;
	public boolean predRel;
	public double score;
	public MILDocument doc;
	public Parse parse;

	public Prediction(int rel, boolean trueRel, boolean predRel, double score, MILDocument doc, Parse parse) {
		this.rel = rel;
		this.trueRel = trueRel;
		this.predRel = predRel;
		this.score = score;
		this.doc = doc;
		this.parse = parse;
	}
}
