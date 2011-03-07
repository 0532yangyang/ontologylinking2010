package multir.learning.algorithm;

import multir.learning.data.MILDocument;
import multir.util.DenseVector;
import multir.util.SparseBinaryVector;

public class Scorer {
	private CRFParameters params;
	
	public Scorer() {}
	
	// scoring on mention documents, all 2*numRelation	
	public double scoreMentionRelation(MILDocument doc, int m, int rel) {
		double sum = 0;
		DenseVector p = params.relParameters[rel];
		sum += p.dotProduct(doc.features[m]);
		return sum;
	}
	
	// need to consider additional features that are dependent on rel ...
	public SparseBinaryVector getMentionRelationFeatures(MILDocument doc, int m, int rel) {
		return doc.features[m];
	}
	
	public void setParameters(CRFParameters params) {
		this.params = params;
	}
}
