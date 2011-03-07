package multir.learning.algorithm;

import multir.learning.data.MILDocument;

public class Parse {

	public int[] Y;
	public int[] Z;      // value equals Z_rel + numRelations*Z_rev
	public double score;
	public MILDocument doc;
	public double[] scores; // for each relation
	
	public Parse() {}
}
