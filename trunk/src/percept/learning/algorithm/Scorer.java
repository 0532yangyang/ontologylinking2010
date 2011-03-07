package percept.learning.algorithm;

import percept.learning.data.Example;
import percept.util.DenseVector;
import percept.util.SparseBinaryVector;

public class Scorer {
	private CRFParameters params;
	
	public Scorer() {}
	
	// scoring on mention documents, all 2*numRelation	
	public double scoreState(Example doc, int state) {
		double sum = 0;
		DenseVector p = params.stateParameters[state];
		sum += p.dotProduct(doc.features);
		return sum;
	}
	
	// need to consider additional features that are dependent on rel ...
	public SparseBinaryVector getFeatures(Example doc) {
		return doc.features;
	}
	
	public void setParameters(CRFParameters params) {
		this.params = params;
	}
}
